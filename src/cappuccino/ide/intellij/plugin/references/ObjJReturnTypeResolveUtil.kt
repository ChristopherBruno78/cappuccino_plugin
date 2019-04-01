package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.contributor.ObjJVariableTypeResolver
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.ObjJHasContainingClassPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameAggregatorUtil
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project


/**
 * Attempts to get possible variable types from a selector
 */
fun getClassConstraints(element: ObjJSelector): List<String> {
    return getClassConstraints(element.getParentOfType( ObjJHasMethodSelector::class.java))
}


private fun getClassConstraints(element: ObjJHasMethodSelector?): List<String> {
    if (element !is ObjJMethodCall) {
        return emptyList()
    }
    val methodCall = element as ObjJMethodCall?
    val callTarget: ObjJCallTarget? = methodCall?.callTarget
    if (callTarget != null) {
        val out = getPossibleClassTypesForCallTarget(callTarget)
        if (out.isNotEmpty()) {
            return out.toList()
        }
    }
    return methodCall?.callTarget?.possibleCallTargetTypes ?: mutableListOf()
}

/**
 * Attempts to get possible class types for a call target
 */
fun getPossibleClassTypesForCallTarget(callTarget: ObjJCallTarget) : Set<String> {
    val qualifiedReference = callTarget.qualifiedReference ?: return setOf()
    return getPossibleClassTypesForQualifiedReference(qualifiedReference)
}