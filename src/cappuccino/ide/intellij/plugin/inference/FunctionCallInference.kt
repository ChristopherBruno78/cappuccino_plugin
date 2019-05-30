package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch

internal fun inferFunctionCallReturnType(functionCall:ObjJFunctionCall, level:Int) : InferenceResult? {
    val resolve = functionCall.reference?.resolve() ?: return null
    val functionAsVariableName = resolve as? ObjJVariableName
    val function = (when {
        functionAsVariableName != null -> getFunctionForVariableName(functionAsVariableName)
        resolve is ObjJFunctionName -> resolve.getParentOfType(ObjJFunctionDeclarationElement::class.java)
        else -> null
    }) ?: return null
    return inferFunctionDeclarationReturnType(function, level - 1)
}

internal fun inferFunctionDeclarationReturnType(function:ObjJFunctionDeclarationElement<*>, level:Int) : InferenceResult? {
    val returnStatementExpressions = function.block.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).mapNotNull { it.expr }
    return getInferredTypeFromExpressionArray(returnStatementExpressions, level)
}

internal fun getFunctionForVariableName(variableName:ObjJVariableName) : ObjJFunctionDeclarationElement<*>? {
    ProgressManager.checkCanceled()
    if (variableName.parent is ObjJGlobalVariableDeclaration)
        return (variableName.parent as ObjJGlobalVariableDeclaration).expr?.leftExpr?.getChildOfType(ObjJFunctionDeclarationElement::class.java)
    val usages = ReferencesSearch.search(variableName)
            .findAll()
            .map { it.element } + variableName

    val assignments = usages.mapNotNull{ getAssignedExpressions(it)?.leftExpr?.getChildOfType(ObjJFunctionDeclarationElement::class.java)}
    return assignments.getOrNull(0)

}

private fun getAssignedExpressions(element: PsiElement?) : ObjJExpr? {
    ProgressManager.checkCanceled()
    return if (element == null || element !is ObjJVariableName)
        null
    else if (element.parent is ObjJGlobalVariableDeclaration)
        (element.parent as ObjJGlobalVariableDeclaration).expr
    else if (element.parent !is ObjJQualifiedReference)
        null
    else if (element.parent.parent is ObjJVariableDeclaration)
        (element.parent.parent as ObjJVariableDeclaration).expr
    else
        null
}