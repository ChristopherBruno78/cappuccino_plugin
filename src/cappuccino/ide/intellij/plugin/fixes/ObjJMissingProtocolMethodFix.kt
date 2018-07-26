package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.utils.ObjJProtocolDeclarationPsiUtil.ProtocolMethods
import org.jetbrains.annotations.Nls

class ObjJMissingProtocolMethodFix(private val methodHeaders: ProtocolMethods) : BaseIntentionAction() {

    override fun getText(): String {
        return "Implement missing protocol methodHeaders"
    }

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME;
    }

    override fun isAvailable(
            project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        return true
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(
            project: Project, editor: Editor, psiFile: PsiFile) {

    }

    private fun addMethods(project: Project, file: VirtualFile, methodHeaders: List<ObjJMethodHeader>) {


    }
}
