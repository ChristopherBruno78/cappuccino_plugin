package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJFunctionDeclarationImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJFunctionDeclarationStubImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub

class ObjJFunctionDeclarationStubType internal constructor(
        debugName: String) : ObjJAbstractFunctionDeclarationStubType<ObjJFunctionDeclarationImpl, ObjJFunctionDeclarationStubImpl>(debugName, ObjJFunctionDeclarationImpl::class.java, ObjJFunctionDeclarationStubImpl::class.java) {

    override fun createPsi(
            stub: ObjJFunctionDeclarationElementStub<ObjJFunctionDeclarationImpl>): ObjJFunctionDeclarationImpl {
        return ObjJFunctionDeclarationImpl(stub, this)
    }

    internal override fun createStub(parent: StubElement<*>,
                                     fileName: String,
                                     fqName: String,
                                     paramNames: List<String>,
                                     returnType: String?,
                                     shouldResolve: Boolean): ObjJFunctionDeclarationElementStub<ObjJFunctionDeclarationImpl> {
        return ObjJFunctionDeclarationStubImpl(parent, fileName, fqName, paramNames, returnType, shouldResolve)
    }
}
