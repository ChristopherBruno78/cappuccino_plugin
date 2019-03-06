package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.IgnoreFlags
import cappuccino.ide.intellij.plugin.psi.utils.getChildrenOfType
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.util.IncorrectOperationException



class ObjJDisableUndeclaredVariableInspectionOnVariable(variableName:ObjJVariableName) : BaseIntentionAction(), LocalQuickFix {

    private val variableNamePointer:SmartPsiElementPointer<ObjJVariableName> = SmartPointerManager.createPointer(variableName)
    private val variableName:ObjJVariableName? get() {
        return variableNamePointer.element
    }

    override fun getText(): String {
        return "Disable undeclared variable inspection for variable \"${variableName?.text}\""
    }

    override fun isAvailable(project:Project, editor:Editor, file:PsiFile) : Boolean {
        return variableName != null
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        apply(project, file)
    }

    override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
        apply(project, problemDescriptor.psiElement.containingFile)
    }

    private fun apply(project: Project, file: PsiFile) {
        val variableName = this.variableName ?: return
        val writeAbove = getOutermostParentInEnclosingBlock(variableName)
        val ignoreComment = ObjJElementFactory.createIgnoreComment(project, IgnoreFlags.IGNORE_UNDECLARED_VAR, variableName.text)
        writeAbove.parent.addBefore(ignoreComment, writeAbove)
        DaemonCodeAnalyzer.getInstance(project).restart(file)
    }

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }

}