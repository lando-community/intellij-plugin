package dev.lando.community.intellij.config

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import dev.lando.community.intellij.LandoBundle
import icons.LandoIcons
import javax.swing.Icon
import org.jetbrains.yaml.YAMLLanguage

/**
 * This class represents the file type for Landofile (.lando.yml) configuration files.
 * It extends [LanguageFileType] from the IntelliJ Platform SDK, which provides a mechanism
 * to define a new file type based on a specific language (in this case, YAML).
 * It also implements [FileTypeIdentifiableByVirtualFile], which allows the file type to be identified
 * based on the properties of a [VirtualFile].
 */
class LandofileFileType private constructor() : LanguageFileType(YAMLLanguage.INSTANCE, false),
    FileTypeIdentifiableByVirtualFile {

    override fun getName(): String = "Landofile"

    override fun getDescription(): String = LandoBundle.message("filetype.lando.landofile.description")
    override fun getDefaultExtension(): String = "yml"
    override fun getIcon(): Icon = LandoIcons.Lando
    override fun getDisplayName(): String = LandoBundle.message("filetype.lando.landofile.name.display")
    override fun getCharset(file: VirtualFile, content: ByteArray): String = "UTF-8"

    override fun isMyFileType(file: VirtualFile): Boolean {
        return file.extension == "yml" && file.name.startsWith(".lando.")
    }

    /**
     * This companion object holds the singleton instance of LandofileFileType.
     */
    companion object {
        val LANDOFILE: LandofileFileType = LandofileFileType()
    }
}