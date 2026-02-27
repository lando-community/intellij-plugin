package dev.lando.community.intellij.ui.console

import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.process.AnsiEscapeDecoder
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.jediterm.terminal.*
import com.jediterm.terminal.emulator.JediEmulator
import com.jediterm.terminal.model.JediTerminal
import com.pty4j.PtyProcess
import org.apache.commons.io.input.buffer.CircularByteBuffer
import org.jetbrains.plugins.terminal.JBTerminalSystemSettingsProvider
import org.jetbrains.plugins.terminal.ShellTerminalWidget
import java.io.InputStream
import java.io.Reader
import javax.swing.JComponent
import kotlin.math.min

private const val BUFFER_SIZE = 100000

class JeditermConsoleView(project: Project) : ConsoleView, AnsiEscapeDecoder.ColoredTextAcceptor {

    private val ansiEscapeDecoder = AnsiEscapeDecoder()

    val termWidget: ShellTerminalWidget

    private var ttyConnector: LandoTtyConnector? = null

    private var emulator: JediEmulator? = null

    private val bytesBuffer = CircularByteBuffer(BUFFER_SIZE)
    private val lock = Object()

    @Volatile
    private var bufferReader: Reader? = null

    @Volatile
    var paused: Boolean = false

    private val bytesStream = object : InputStream() {
        override fun read(): Int {
            synchronized(lock) {
                while (!Thread.interrupted() && !bytesBuffer.hasBytes()) {
                    lock.wait()
                }
                return if (bytesBuffer.hasBytes()) bytesBuffer.read().toInt() else -1
            }
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            synchronized(lock) {
                while (!Thread.interrupted() && !bytesBuffer.hasBytes()) {
                    lock.wait()
                }
                val toRead = min(length, bytesBuffer.currentNumberOfBytes)
                bytesBuffer.read(buffer, offset, toRead)
                return toRead
            }
        }

        override fun available(): Int {
            synchronized(lock) {
                return bytesBuffer.currentNumberOfBytes
            }
        }
    }

    init {
        termWidget = object : ShellTerminalWidget(project, JBTerminalSystemSettingsProvider(), this) {
            override fun createTerminalStarter(terminal: JediTerminal, connector: TtyConnector): TerminalStarter =
                object : TerminalStarter(
                    terminal, connector,
                    TtyBasedArrayDataStream(connector) { typeAheadManager.onTerminalStateChanged() },
                    typeAheadManager, executorServiceManager
                ) {
                    override fun createEmulator(dataStream: TerminalDataStream, terminal: Terminal): JediEmulator =
                        JediEmulator(dataStream, terminal).apply { emulator = this }
                }
        }
    }

    fun connectToProcess(process: PtyProcess) {
        ttyConnector = LandoTtyConnector(process, this)
        termWidget.start(ttyConnector)
    }

    override fun dispose() {
        ttyConnector?.close()
    }

    override fun getComponent(): JComponent = termWidget
    override fun getPreferredFocusableComponent(): JComponent = termWidget

    override fun print(text: String, contentType: ConsoleViewContentType) {
        termWidget.terminal.writeCharacters(text)
    }

    override fun clear() {
        termWidget.terminalTextBuffer.historyBuffer.clearAll()
        termWidget.terminal.clearScreen()
        termWidget.terminal.cursorPosition(0, 1)
    }

    override fun scrollTo(offset: Int) {
        termWidget.terminal.setScrollingRegion(offset, Int.MAX_VALUE)
    }

    override fun attachToProcess(processHandler: ProcessHandler) {
        throw IllegalArgumentException("Should not be called")
    }

    override fun setOutputPaused(value: Boolean) {
        paused = value
    }

    override fun isOutputPaused(): Boolean = paused

    override fun hasDeferredOutput(): Boolean {
        return false
    }

    override fun performWhenNoDeferredOutput(runnable: Runnable) {
        runnable.run()
    }

    override fun setHelpId(helpId: String) {}

    override fun addMessageFilter(filter: Filter) {
        throw NotImplementedError("Operation not supported")
    }

    override fun printHyperlink(hyperlinkText: String, info: HyperlinkInfo?) {
        print(hyperlinkText, ConsoleViewContentType.NORMAL_OUTPUT)
    }

    override fun getContentSize(): Int =
        with(termWidget.terminalTextBuffer) { screenLinesCount + termWidget.terminalTextBuffer.screenLinesCount }

    override fun canPause(): Boolean = true

    override fun createConsoleActions(): Array<AnAction> {
        return emptyArray()
    }

    override fun allowHeavyFilters() {}

    fun output(dataChunk: ByteArray) {
        if (!paused) {
            synchronized(lock) {
                val length = min(dataChunk.size, bytesBuffer.space)
                if (length > 0) {
                    bytesBuffer.add(dataChunk, 0, length)
                    addData(dataChunk.decodeToString(0, length), ProcessOutputTypes.STDOUT)
                    lock.notify()
                }
            }
        }
    }

    fun readChars(buf: CharArray, offset: Int, length: Int): Int {
        synchronized(lock) {
            while (true) {
                val currentReader = bufferReader
                if (currentReader?.ready() == true) {
                    return currentReader.read(buf, offset, length)
                }
                try {
                    lock.wait()
                } catch (_: InterruptedException) {
                }
            }
        }
    }

    private fun addData(message: String, outputType: Key<*>) {
        ansiEscapeDecoder.escapeText(message, outputType, this)
    }

    override fun coloredTextAvailable(text: String, attributes: Key<*>) {
        print(text, ConsoleViewContentType.getConsoleViewType(attributes))
    }
}