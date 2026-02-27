package dev.lando.community.intellij.ui.widget

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.WindowManager
import dev.lando.community.intellij.listeners.LandoStatusListener
import dev.lando.community.intellij.services.LandoStatusService

const val ID: String = LandoStatusWidget.ID

class LandoStatusWidgetFactory : StatusBarWidgetFactory {
    init {
        ApplicationManager.getApplication().messageBus.connect().subscribe(
            LandoStatusService.TOPIC,
            object : LandoStatusListener {
                override fun statusChanged() {
                    for (project in ProjectManager.getInstance().openProjects) {
                        val statusBar = WindowManager.getInstance().getStatusBar(project)
                        statusBar?.updateWidget(ID)
                    }
                }
            })
    }

    override fun getId(): String = ID

    override fun getDisplayName(): String = "Lando Status"

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget = LandoStatusWidget(project)

    override fun disposeWidget(widget: StatusBarWidget) {}

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}