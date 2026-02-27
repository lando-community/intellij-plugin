package dev.lando.community.intellij

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.openapi.project.Project
import dev.lando.community.intellij.services.LandoProjectService
import dev.lando.community.intellij.ui.console.JeditermConsoleView
import com.pty4j.PtyProcessBuilder
import java.util.*

/**
 * Class for executing Lando commands.
 * @param command The Lando command to execute.
 */
class LandoExec(private val command: String) {
    val landoBin = "lando"

    private var directory: String = ""

    /**
     * The --format parameter for the Lando command.
     */
    private var format: Format? = null

    private var attachToConsole: Boolean = true

    var project: Project? = null

    /**
     * Sets the directory for the Lando command.
     * @param path The directory path.
     * @return The LandoExec instance.
     */
    fun directory(path: String): LandoExec {
        directory = path
        return this
    }

    /**
     * Sets the format for the Lando command.
     * @param format The format.
     * @return The LandoExec instance.
     */
    fun format(format: Format): LandoExec {
        this.format = format
        return this
    }

    /**
     * Sets the Lando command to fetch JSON.
     * @return The LandoExec instance.
     */
    fun fetchJson(): LandoExec {
        attachToConsole = false
        return format(Format.JSON)
    }

    /**
     * Runs the Lando command.
     * @return The ProcessHandler for the process running the Lando command.
     */
    fun run(): ProcessHandler {
        var cmd = arrayOf(landoBin, command)

        val processBuilder = PtyProcessBuilder()
        processBuilder.setRedirectErrorStream(true)
        processBuilder.setConsole(true)

        // Default to the project root as working directory if none is set
        if (directory.isEmpty() && project != null) {
            directory = LandoProjectService.getInstance(project!!).appRoot?.path!!
        } else if (directory.isEmpty()) {
            directory = System.getProperty("user.dir")
        }
        processBuilder.setDirectory(directory)

        // If a format is set, add it to the command
        if (format != null) {
            cmd += format.toString()
        }

        // Start the process
        val process = processBuilder.setCommand(cmd).start()

        // Attach process to console view to display output
        var landoConsoleView: JeditermConsoleView? = null
        if (attachToConsole && project != null) {
            // Create a new console view with the process
            landoConsoleView = JeditermConsoleView(project!!)
            landoConsoleView.connectToProcess(process)
        }

        val scanner = Scanner(process.inputStream).useDelimiter("\\n")
        while (scanner.hasNext()) {
            val line = scanner.next()
            landoConsoleView?.output((line + "\n").toByteArray())
        }

        // Create a new process handler from the process
        return KillableColoredProcessHandler(process, cmd.joinToString(" "), Charsets.UTF_8)
    }

    /**
     * Companion object for LandoExec class.
     */
    companion object {
        const val PROCESS_TIMEOUT = 30000L

        /**
         * Enum for the Lando --format parameter values.
         */
        enum class Format(val format: String) {
            JSON("--format json"),
        }
    }

    /**
     * Object for parsing ANSI escape codes.
     */
    object AnsiEscapeCodeParser {
        fun parse(text: String): String {
            // Basic ANSI escape code removal
            return text.replace(Regex("\\x1B\\[[;\\d]*m"), "")
        }
    }
}