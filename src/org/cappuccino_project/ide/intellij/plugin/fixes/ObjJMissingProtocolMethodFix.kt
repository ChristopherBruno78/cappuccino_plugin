package org.cappuccino_project.ide.intellij.plugin.fixes

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodHeader
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJProtocolDeclarationPsiUtil.ProtocolMethods
import org.jetbrains.annotations.Nls

class ObjJMissingProtocolMethodFix(private val methodHeaders: ProtocolMethods) : BaseIntentionAction() {

    override fun getText(): String {
        return "Implement missing protocol methodHeaders"
    }

    @Nls
    override fun getFamilyName(): String {
        return "Protocol methodHeaders"
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
