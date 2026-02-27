package dev.lando.community.intellij

import com.intellij.openapi.components.service
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.lando.community.intellij.services.LandoProjectService

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MyPluginTest : BasePlatformTestCase() {

    fun testProjectService() {
        val projectService = project.service<LandoProjectService>()

        assertNotNull(projectService)
    }
}
