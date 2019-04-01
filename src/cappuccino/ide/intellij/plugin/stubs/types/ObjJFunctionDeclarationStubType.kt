package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.ObjJFunctionDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionLiteral
import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJFunctionDeclarationImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJFunctionDeclarationStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiFile

class ObjJFunctionDeclarationStubType internal constructor(
        debugName: String) : ObjJAbstractFunctionDeclarationStubType<ObjJFunctionDeclarationImpl>(debugName, ObjJFunctionDeclarationImpl::class.java) {

    override fun createPsi(
            stub: ObjJFunctionDeclarationElementStub<ObjJFunctionDeclarationImpl>): ObjJFunctionDeclarationImpl {
        return ObjJFunctionDeclarationImpl(stub, this)
    }

    override fun createStub(parent: StubElement<*>,
                            fileName: String,
                            fqName: String,
                            paramNames: List<String>,
                            returnType: String?,
                            shouldResolve: Boolean,
                            scope: ObjJFunctionScope): ObjJFunctionDeclarationElementStub<ObjJFunctionDeclarationImpl> {
        return ObjJFunctionDeclarationStubImpl(parent, fileName, fqName, paramNames, returnType, shouldResolve, scope)
    }

}
