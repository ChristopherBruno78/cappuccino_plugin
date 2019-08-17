package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJVarTypeIdImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJVarTypeIdStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

class ObjJVarTypeIdStubImpl(parent: StubElement<*>, override val idType: String, internal val shouldResolve: Boolean) : ObjJStubBaseImpl<ObjJVarTypeIdImpl>(parent, ObjJStubTypes.VAR_TYPE_ID), ObjJVarTypeIdStub {

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
