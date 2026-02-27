package dev.lando.community.intellij.services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ConcurrencyUtil
import com.intellij.util.messages.Topic
import dev.lando.community.intellij.LandoExec
import dev.lando.community.intellij.ServiceData
import dev.lando.community.intellij.listeners.LandoProjectManagerListener
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

val logger = Logger.getInstance(LandoProjectService::class.java)

/**
 * Service class for managing Lando applications.
 * This class is responsible for fetching and maintaining the status of Lando applications.
 * @param project The IntelliJ Project this service is associated with.
 */
@Service(Service.Level.PROJECT)
class LandoProjectService(val project: Project) : Disposable {
    companion object {
        const val LANDOFILE_NAME = ".lando.yml"

        val TOPIC = Topic.create("LandoServiceTopic", LandoProjectManagerListener::class.java)

        /**
         * Retrieves the [LandoProjectService] instance associated with the given Project.
         *
         * @param project The IntelliJ Project this service is associated with.
         * @return The [LandoProjectService] instance associated with the given Project.
         */
        @JvmStatic
        fun getInstance(project: Project): LandoProjectService = project.getService(LandoProjectService::class.java)
    }

    // The root directory of the project. It's where we expect to find the Lando configuration file.
    var projectDir: VirtualFile? = project.guessProjectDir()

    // The Lando configuration file in the project directory. It's presence determines whether the project uses Lando.
    var landoFile: VirtualFile? = projectDir?.findChild(LANDOFILE_NAME)

    /**
     * Checks if the current project uses Lando.
     *
     * This is determined by the existence of a Lando configuration file (.lando.yml) in the project directory.
     * This method is important for enabling or disabling Lando-specific features based on whether the project uses Lando.
     *
     * @return True if the project uses Lando, false otherwise.
     */
    fun projectUsesLando(): Boolean {
        return landoFile?.exists()!!
    }

    // ScheduledExecutorService for periodically checking the status of the Lando application
    private val statusWatcher: ScheduledExecutorService = ConcurrencyUtil.newSingleScheduledThreadExecutor("Lando Status Watcher")
        .apply { scheduleWithFixedDelay({ fetchInfo() }, 0, 2000, TimeUnit.MILLISECONDS) }

    // Root directory of the Lando application
    var appRoot: VirtualFile? = this.projectDir

    // Map for storing the status of the services in the Lando application
    var services: MutableMap<String, ServiceData> = HashMap()

    // Indicate whether the Lando application has been started
    var started: Boolean = false
        set(value) {
            field = value
            notifyListeners()
        }
        get() {
            return services.isNotEmpty()
        }

    init {
        if (appRoot == null) {
            logger.debug("Could not find project root directory")
        }
    }

    /**
     * Fetches the information of the services in the Lando application.
     * @return [ProcessHandler] for the process fetching the service information.
     */
    private fun fetchLandoServiceInfo(): ProcessHandler {
        logger.debug("Fetching Lando service info...")
        val exec = LandoExec("info")
        exec.project = project
        return exec.fetchJson().run()
    }

    /**
     * Checks the status of the Lando application and updates the services map.
     */
    private fun fetchInfo() {
        logger.debug("Checking Lando status...")
        val serviceInfoHandler = fetchLandoServiceInfo()
        serviceInfoHandler.waitFor(30000)
        if (!serviceInfoHandler.isProcessTerminated) {
            serviceInfoHandler.destroyProcess()
            logger.warn("Timeout while fetching Lando service info")
        }
        val serviceInfoJson = serviceInfoHandler.processInput.toString()
        val serviceData = parseServiceData(serviceInfoJson)

        services = serviceData.associateBy { it.service }.toMutableMap()
    }

    /**
     * Parses the JSON data of the service information into a list of ServiceData objects.
     * @param jsonData JSON data of the service information.
     * @return List of ServiceData objects.
     */
    private fun parseServiceData(jsonData: String): List<ServiceData> {
        val gson = Gson()
        val serviceListType = object : TypeToken<List<ServiceData>>() {}.type
        return gson.fromJson(jsonData, serviceListType)
    }

    private fun notifyListeners() {
        ApplicationManager.getApplication().messageBus.syncPublisher(TOPIC)
    }

    /**
     * Disposes the LandoAppService, shutting down the status watcher.
     */
    override fun dispose() {
        statusWatcher.shutdown()
    }
}
