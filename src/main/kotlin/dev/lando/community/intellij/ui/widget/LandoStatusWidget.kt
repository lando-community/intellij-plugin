package dev.lando.community.intellij.ui.widget

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.util.Consumer
import dev.lando.community.intellij.LandoBundle
import dev.lando.community.intellij.services.LandoProjectService
import dev.lando.community.intellij.services.LandoStatusService
import icons.LandoIcons
import java.awt.event.MouseEvent
import javax.swing.Icon


/**
 * This class represents a widget for the status of Lando in the IntelliJ status bar.
 * It implements [StatusBarWidget] from the IntelliJ Platform SDK which provides a mechanism
 * to create a widget that can be added to the IntelliJ status bar.
 *
 * @property project The current IntelliJ project. This is used to access project-specific services and components.
 */
open class LandoStatusWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.IconPresentation {

    companion object {
        const val ID: String = "LandoStatusWidget"
    }

    override fun ID(): String = ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {}

    override fun dispose() {}

    override fun getTooltipText(): String {
        return if (LandoProjectService.getInstance(project).started) {
            LandoBundle.message("widget.lando.status.tooltip.started")
        } else {
            LandoBundle.message("widget.lando.status.tooltip.stopped")
        }
    }

    override fun getClickConsumer(): Consumer<MouseEvent> {
        return Consumer {
            val statusService = LandoStatusService.getInstance()
            if (statusService.fetchLandoAppStatus()) {
                // Display the status in a dialog or panel
                val statusMessage = statusService.appsCache.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                Messages.showMessageDialog(project, statusMessage, "Lando App Status", Messages.getInformationIcon())
            } else {
                Messages.showMessageDialog(project, "Failed to fetch Lando app status.", "Error", Messages.getErrorIcon())
            }
        }
    }

    override fun getIcon(): Icon {
        return if (checkLandoFileAndStatus()) LandoIcons.Status.Started else LandoIcons.Status.Stopped
    }

    private fun checkLandoFileAndStatus(): Boolean {
        LandoProjectService.getInstance(project).let { landoService ->
            if (landoService.projectUsesLando()){
                if (landoService.started) {
                    return true
                }
            }
        }
        return false
    }
}