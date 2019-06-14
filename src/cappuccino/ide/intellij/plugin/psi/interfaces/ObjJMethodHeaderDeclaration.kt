package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderDeclarationStub

interface ObjJMethodHeaderDeclaration<StubT:ObjJMethodHeaderDeclarationStub<*>>:ObjJStubBasedElement<StubT>, ObjJHasMethodSelector {

    val cachedTypes:InferenceResult?

    fun getReturnTypes(tag:Long): Set<String>

    val explicitReturnType:String

    val methodScope: ObjJMethodPsiUtils.MethodScope

    val isStatic: Boolean
        get() = methodScope == ObjJMethodPsiUtils.MethodScope.STATIC
}