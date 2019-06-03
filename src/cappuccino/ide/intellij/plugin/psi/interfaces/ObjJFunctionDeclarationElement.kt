package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJFormalParameterArg
import cappuccino.ide.intellij.plugin.psi.ObjJLastFormalParameterArg
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.inference.SPLIT_JS_CLASS_TYPES_LIST_REGEX as SPLIT_JS_CLASS_TYPES_LIST_REGEX1

interface ObjJFunctionDeclarationElement<StubT: ObjJFunctionDeclarationElementStub<*>> : ObjJHasFunctionName, ObjJStubBasedElement<StubT>, ObjJCompositeElement, ObjJResolveableElement<StubT> {
    val paramNames: List<String>

    fun getReturnType(tag:Long): String?

    val formalParameterArgList: List<ObjJFormalParameterArg>

    val lastFormalParameterArg: ObjJLastFormalParameterArg?

    val block: ObjJBlock?

    val functionScope:ObjJFunctionScope
}

fun ObjJFunctionDeclarationElement<*>.getReturnTypes(tag:Long):Set<String> {
    return getReturnType(tag)
            ?.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX1)
            ?.toSet()
            ?: emptySet()
}
