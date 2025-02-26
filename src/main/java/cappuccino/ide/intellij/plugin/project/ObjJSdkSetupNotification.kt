package cappuccino.ide.intellij.plugin.project

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFileType
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.lang.ObjJFileType
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.findFrameworkNameInPlist
import com.intellij.ProjectTopics
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications

class ObjJSdkSetupNotification : EditorNotifications.Provider<EditorNotificationPanel>() {

    override fun getKey(): Key<EditorNotificationPanel> {
        return KEY
    }

    override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor, project: Project): EditorNotificationPanel? {
        registerListener(project)
        if (file.fileType !is ObjJFileType && file.fileType !is JsTypeDefFileType)
            return null
        val psiFile = PsiManager.getInstance(project).findFile(file)
        if (psiFile == null || psiFile.language != ObjJLanguage.instance) return null

        //val module = file.getModule(project) ?: return null
        val needed = getNeeded(project)
        if (needed == NeededFrameworks.NONE) {
            return null
        }

        if (!canRegisterSourcesAsLibrary(needed.missing))
            return null

        return createPanel(psiFile, needed)
    }

    companion object {
        private fun createPanel(file: PsiFile, needed: NeededFrameworks): EditorNotificationPanel {
            val panel = EditorNotificationPanel()
            panel.setText(ObjJBundle.message("objj.module.frameworks.error.missing-frameworks-header", needed.textLabel))
            panel.createActionLabel(ObjJBundle.message("objj.module.frameworks.error.add-missing-frameworks-prompt")) createLabel@{
                //if (projectSdk.sdkType !is ObjJSDKType) {
                ApplicationManager.getApplication().runWriteAction {
                    val module: Module? = ModuleUtilCore.findModuleForPsiElement(file)
                    if (module != null) {
                        val libraryName = when (needed) {
                            NeededFrameworks.APPKIT -> "AppKit"
                            NeededFrameworks.FOUNDATION -> "Foundation"
                            NeededFrameworks.BOTH -> "Cappuccino"
                            NeededFrameworks.NONE -> "None"
                        }
                        val didRegister = registerSourcesAsLibrary(module, libraryName, needed.missing)
                        if (!didRegister)
                            LOGGER.severe("Failed to register bundled sources for ${needed.textLabel}")

                    }
                }
                //}
            }
            return panel
        }

        private val KEY: Key<EditorNotificationPanel> = Key.create("Setup ObjJ SDK")
    }
}

private fun getNeeded(project: Project): NeededFrameworks {
    var needed:NeededFrameworks? = NeededFrameworks.BOTH
    listOf(GlobalSearchScope.everythingScope(project)).forEach { searchScope ->
        needed = getNeeded(project, searchScope, needed)
        if (needed == null)
            return NeededFrameworks.NONE
    }
    return needed ?: NeededFrameworks.NONE
}

private fun getNeeded(project: Project, searchScope: GlobalSearchScope, needed: NeededFrameworks?): NeededFrameworks? {
    if (needed == null)
        return null
    var hasAppKit = true
    var hasFoundation = true
    when (needed) {
        NeededFrameworks.APPKIT -> hasAppKit = false
        NeededFrameworks.FOUNDATION -> hasFoundation = false
        else -> {
            hasAppKit = false
            hasFoundation = false
        }
    }
    return getNeeded(project, searchScope, hasFoundation, hasAppKit)
}

private fun getNeeded(project: Project, searchScope: GlobalSearchScope, hasFoundationIn: Boolean, hasAppKitIn: Boolean): NeededFrameworks? {
    var hasFoundation = hasFoundationIn
    var hasAppKit = hasAppKitIn
    if (hasFoundation && hasAppKit)
        return null
    FilenameIndex.getFilesByName(project, "Info.plist", searchScope).forEach {
        if (hasFoundation && hasAppKit)
            return@forEach
        val plistName = findFrameworkNameInPlist(it)?.toLowerCase()
            ?: return@forEach
        if (plistName == "foundation")
            hasFoundation = true
        else if (plistName == "appkit")
            hasAppKit = true
    }
    if (hasFoundation && hasAppKit)
        return null
    return when {
        hasFoundation -> NeededFrameworks.APPKIT
        hasAppKit -> NeededFrameworks.FOUNDATION
        else -> NeededFrameworks.BOTH
    }
}

private const val APPKIT = "AppKit"
private const val FOUNDATION = "Foundation"

internal enum class NeededFrameworks(val textLabel: String, val missing: List<String>) {
    APPKIT(cappuccino.ide.intellij.plugin.project.APPKIT, listOf(cappuccino.ide.intellij.plugin.project.APPKIT)),
    FOUNDATION(cappuccino.ide.intellij.plugin.project.FOUNDATION, listOf(cappuccino.ide.intellij.plugin.project.FOUNDATION)),
    BOTH("${cappuccino.ide.intellij.plugin.project.APPKIT} and ${cappuccino.ide.intellij.plugin.project.FOUNDATION}", listOf(cappuccino.ide.intellij.plugin.project.APPKIT, cappuccino.ide.intellij.plugin.project.FOUNDATION)),
    NONE("None", listOf());
}



private fun registerListener(project: Project) {
    if (project.getUserData(CONNECTION_KEY) == true)
        return
    project.putUserData(CONNECTION_KEY, true)
    val connection = project.messageBus.connect(project)
    connection.subscribe(ProjectTopics.PROJECT_ROOTS, object : ModuleRootListener {
        override fun rootsChanged(event: ModuleRootEvent) {
            if (project.isDisposed) {
                project.putUserData(CONNECTION_KEY, false)
                connection.disconnect()
                return
            }
            EditorNotifications.updateAll()
        }
    })
}

private val CONNECTION_KEY = Key<Boolean>("cappuccino.id.intellij.plugin.project.NOTIFICATION_PROJECT_CONNECTED")