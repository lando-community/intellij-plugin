package dev.lando.community.intellij.services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.messages.Topic
import com.pty4j.PtyProcessBuilder
import dev.lando.community.intellij.LandoExec
import dev.lando.community.intellij.listeners.LandoStatusListener
import dev.lando.community.intellij.ui.console.JeditermConsoleView
import java.util.*
import kotlinx.coroutines.sync.Mutex

@Service()
class LandoStatusService() : Disposable {

    private val ttl = 5000

    private var lastUpdated = 0L

    private val mutex: Mutex = Mutex()

    val appsCache = mutableMapOf<String, String>()

    var consoleView: JeditermConsoleView? = null

    /**
     * Fetches the status of Lando apps.
     *
     * @return ProcessHandler for the process fetching the status.
     */
    fun fetchLandoAppStatus(): Boolean {
        if (cacheIsValid()) {
            return true
        }
        if (!mutex.tryLock("fetchLandoAppStatus")) {
            for (i in 0..LandoExec.PROCESS_TIMEOUT step 1000) {
                if (cacheIsValid()) {
                    return true
                }
                Thread.sleep(1000)
            }
            logger.warn("Timeout waiting for fetchLandoAppStatus mutex to be released.")
            return false
        }

        try {
            logger.debug("Fetching Lando app status...")

            // Execute `lando list`
            val landoListCmd =
                arrayOf("lando", "list") //, "--format", "json")
            val processBuilder = PtyProcessBuilder(landoListCmd)
            processBuilder.setRedirectErrorStream(true)
            processBuilder.setConsole(true)

            val process = processBuilder.start()

            consoleView?.connectToProcess(process)

            val scanner = Scanner(process.inputStream).useDelimiter("\\n")

            while (scanner.hasNext()) {
                val line = scanner.next()
                consoleView?.output((line + "\n").toByteArray())
            }

//            val reader = BufferedReader(InputStreamReader(process.inputStream))
//
//            val stdout = StringBuilder()
//            val stderr = StringBuilder()
//            reader.forEachLine { line ->
//                consoleView?.output(line.toByteArray())

//                // Check if the line is JSON data
//                if (line.startsWith("{")) {
//                    stdout.append(line)
//                } else {
//                    stderr.append(line).append("\n")
//                    consoleView?.output((line + "\r\n").toByteArray()) // Ensure each line of stderr is output to consoleView
//                }
//            }
            process.waitFor()

            // Handle stdout (JSON data)
//            val jsonData = stdout.toString()
//            if (jsonData.isNotEmpty()) {
//                appsCache.putAll(parseLandoAppStatus(jsonData))
//                lastUpdated = System.currentTimeMillis()
//                // Notify listeners about status change
//                ApplicationManager.getApplication().messageBus.syncPublisher(TOPIC).statusChanged()
//                // Send output to JeditermConsoleView
//                consoleView?.output(jsonData.toByteArray())
//            }

            // Handle stderr (status updates)
            //updateTerminalPanel(stderr.toString())

        } catch (e: Exception) {
            logger.error("Error fetching Lando app status", e)
        } finally {
            mutex.unlock()
        }

        return appsCache.isNotEmpty()
    }

    /**
     * Checks if the cache is still valid.
     * @return True if the cache is still valid, false otherwise.
     */
    private fun cacheIsValid(): Boolean {
        return appsCache.isNotEmpty() && System.currentTimeMillis() - lastUpdated < ttl
    }

    /**
     * Parses the JSON data of the Lando app status into a map.
     * @param jsonData JSON data of the Lando app status.
     * @return Map of the Lando app status.
     */
    private fun parseLandoAppStatus(jsonData: String): Map<String, String> {
        val gson = Gson()
        val statusType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(jsonData, statusType)
    }

    override fun dispose() {
        consoleView?.dispose()
    }

    private fun updateTerminalPanel(errorOutput: String) {
        // TODO: This service shouldn't be responsible for updating the terminal panel
        val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return

        ApplicationManager.getApplication().invokeLater {
            consoleView?.output(errorOutput.toByteArray())
        }
    }

    companion object {
        val TOPIC = Topic.create("LandoStatusTopic", LandoStatusListener::class.java)

        val logger = Logger.getInstance(LandoStatusService::class.java)

        fun getInstance(): LandoStatusService =
            ApplicationManager.getApplication().getService(LandoStatusService::class.java)
    }
}