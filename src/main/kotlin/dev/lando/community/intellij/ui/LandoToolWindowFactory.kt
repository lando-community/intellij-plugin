package dev.lando.community.intellij.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi
import com.intellij.ui.content.ContentFactory
import dev.lando.community.intellij.LandoBundle
import dev.lando.community.intellij.services.LandoStatusService
import dev.lando.community.intellij.ui.console.JeditermConsoleView


class LandoToolWindowFactory : ToolWindowFactory {
    override fun init(toolWindow: ToolWindow) {
        toolWindow.component.putClientProperty(ToolWindowContentUi.ALLOW_DND_FOR_TABS, true)
        toolWindow.setToHideOnEmptyContent(false)
        toolWindow.stripeTitle = LandoBundle.message("stripe.lando.title")
        toolWindow.isAvailable = true
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val landoConsoleView = JeditermConsoleView(project)
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(landoConsoleView.component, LandoBundle.message("tab.lando.title"), false)
        toolWindow.contentManager.addContent(content)

        // Pass the ConsoleView instance to LandoStatusService
        LandoStatusService.getInstance().consoleView = landoConsoleView
    }
}