package cappuccino.ide.intellij.plugin.project

import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.roots.libraries.LibraryTable
import com.intellij.openapi.roots.libraries.LibraryTable.ModifiableModel
import java.util.logging.Logger

internal const val BUNDLE_DEFINITIONS_FOLDER = "definitions"
private val LOGGER:Logger = Logger.getLogger("#BundledLibraryUtil")

internal fun registerSourcesAsLibrary(module: Module, libraryName:String, directories:List<String>) : Boolean {
    val rootModel = ModuleRootManager.getInstance(module).modifiableModel
    val modifiableModel: ModifiableModel = rootModel.moduleLibraryTable.modifiableModel
    val library = getCreateLibrary(libraryName, modifiableModel)
    val libModel = library.modifiableModel
    directories.forEach {directory ->
        val libraryPath = ObjJFileUtil.getPluginResourceFile("${BUNDLE_DEFINITIONS_FOLDER}/$directory")
        if (libraryPath == null) {
            val pluginRoot = ObjJFileUtil.PLUGIN_HOME_DIRECTORY
            if (pluginRoot == null || !pluginRoot.exists()) {
                LOGGER.info("Failed to locate bundled files: Plugin root is invalid")
            } else {
                LOGGER.info("Failed to locate bundled files: Files in plugin root is <${pluginRoot.children?.map { it.name }}>")
            }
            return false
        }
        libModel.addRoot(libraryPath, OrderRootType.SOURCES)
    }
    libModel.commit()
    modifiableModel.commit()
    rootModel.commit()
    return true
}

private fun getCreateLibrary(libraryName:String, modifiableModel: ModifiableModel) : Library {
    return modifiableModel.getLibraryByName(libraryName)
            ?: modifiableModel.createLibrary(libraryName, ObjJLibraryType.LIBRARY)
}