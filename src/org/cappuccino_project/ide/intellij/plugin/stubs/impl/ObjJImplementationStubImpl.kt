package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImplementationDeclarationImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImplementationStub
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes

class ObjJImplementationStubImpl(
        parent: StubElement<*>, className: String, override val superClassName: String?, override val categoryName: String?, protocols: List<String>, shouldResolve: Boolean) : ObjJClassDeclarationStubImpl<ObjJImplementationDeclarationImpl>(parent, ObjJStubTypes.IMPLEMENTATION, className, protocols, shouldResolve), ObjJImplementationStub {

    override val isCategory: Boolean
        get() = categoryName != null
}
