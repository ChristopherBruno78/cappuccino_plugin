package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.util.io.StringRef
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub

open class ObjJClassDeclarationStubImpl<PsiT : ObjJClassDeclarationElement<out ObjJClassDeclarationStub<*>>> internal constructor(
        parent: StubElement<*>,
        elementType: IStubElementType<*, *>,
        override val className: String,
        override val inheritedProtocols: List<String>,
        private val shouldResolve: Boolean) : ObjJStubBaseImpl<PsiT>(parent, elementType), ObjJClassDeclarationStub {

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
