package dev.lando.community.intellij.listeners

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import dev.lando.community.intellij.services.LandoProjectService

class LandoProjectManagerListener : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        LandoProjectService.getInstance(project)
    }
}