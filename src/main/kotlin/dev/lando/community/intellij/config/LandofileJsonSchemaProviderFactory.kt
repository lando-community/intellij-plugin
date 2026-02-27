package dev.lando.community.intellij.config

import com.intellij.lang.javascript.EmbeddedJsonSchemaFileProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import dev.lando.community.intellij.LandoBundle

/**
 * This class is responsible for providing the JSON schema for Lando files.
 * It implements [JsonSchemaProviderFactory] and [DumbAware] from the IntelliJ Platform SDK.
 * [JsonSchemaProviderFactory] provides a mechanism to supply custom JSON schemas,
 * and [DumbAware] indicates that this provider does not require indexing and can work during indexing.
 */
class LandofileJsonSchemaProviderFactory : JsonSchemaProviderFactory, DumbAware {
    companion object {
        const val SCHEMA_FILE_NAME: String = "landofile-spec.json"
        const val SCHEMA_FILE_DIR: String = "/"
        const val SCHEMA_FILE_URL: String = "https://raw.githubusercontent.com/lando-community/intellij-plugin/src/main/resources/landofile-spec.json"
    }

    override fun getProviders(project: Project): List<JsonSchemaFileProvider> {
        // TODO: Eliminate the need for the bundled JavaScript plugin by moving away from the EmbeddedJsonSchemaFileProvider.
        val provider: EmbeddedJsonSchemaFileProvider = object : EmbeddedJsonSchemaFileProvider(
            SCHEMA_FILE_NAME,
            LandoBundle.message("filetype.lando.landofile.name.display"),
            SCHEMA_FILE_URL,
            LandofileJsonSchemaProviderFactory::class.java,
            SCHEMA_FILE_DIR
        ) {
            override fun isAvailable(file: VirtualFile): Boolean {
                // Only provide the schema for files with `.lando*.yml` pattern.
                return file.extension == "yml" && file.name.startsWith(".lando.", ignoreCase = true)
            }
        }

        return listOf(provider)
    }
}