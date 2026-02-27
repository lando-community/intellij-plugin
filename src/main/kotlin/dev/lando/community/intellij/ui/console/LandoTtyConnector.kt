package dev.lando.community.intellij.ui.console

import com.intellij.terminal.pty.PtyProcessTtyConnector
import com.jediterm.core.util.TermSize
import com.pty4j.PtyProcess
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

private var charset: Charset = StandardCharsets.UTF_8

class LandoTtyConnector(private val process: PtyProcess, private val consoleView: JeditermConsoleView) :
    PtyProcessTtyConnector(process, charset, consoleView.termWidget.shellCommand) {

    private var closed = false

    override fun read(buf: CharArray, offset: Int, length: Int): Int {
        if (!closed) {
            try {
                return myReader.read(buf, offset, length)
            } catch (e: IOException) {
                consoleView.output("Error reading from process input stream: ${e.message}".toByteArray(charset))
            }
        }
        return -1
    }

    override fun write(bytes: ByteArray) {
        if (!closed) {
            try {
                process.outputStream.write(bytes)
                process.outputStream.flush()
            } catch (e: IOException) {
                consoleView.output("Error writing to process output stream: ${e.message}".toByteArray(charset))
            }
        }
    }

    override fun write(string: String) {
        write(string.toByteArray(charset))
    }

    override fun isConnected(): Boolean = !closed

    override fun waitFor(): Int = 0

    override fun ready(): Boolean {
        return this.isConnected()
    }

    override fun getName(): String = process.pid().toString()

    override fun close() {
        if (!closed) {
            closed = true
            try {
                process.outputStream.close()
            } catch (e: IOException) {
                consoleView.output("Error closing process output stream: ${e.message}".toByteArray(charset))
            }
            consoleView.output("TTY connection closed.".toByteArray(charset))
        }
    }

    override fun resize(termSize: TermSize) {}
}