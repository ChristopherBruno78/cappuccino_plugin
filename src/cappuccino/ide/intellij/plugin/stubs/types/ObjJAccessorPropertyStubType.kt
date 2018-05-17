package cappuccino.ide.intellij.plugin.stubs.types

import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.impl.ObjJAccessorPropertyImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJAccessorPropertyStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJAccessorPropertyStub
import cappuccino.ide.intellij.plugin.psi.utils.getGetterSelector
import cappuccino.ide.intellij.plugin.psi.utils.getSetterSelector
import cappuccino.ide.intellij.plugin.utils.Strings

import java.io.IOException

class ObjJAccessorPropertyStubType internal constructor(
        debugName: String) : ObjJStubElementType<ObjJAccessorPropertyStub, ObjJAccessorPropertyImpl>(debugName, ObjJAccessorPropertyImpl::class.java) {

    override fun createPsi(
            objJAccessorPropertyStub: ObjJAccessorPropertyStub): ObjJAccessorPropertyImpl {
        return ObjJAccessorPropertyImpl(objJAccessorPropertyStub, this)
    }

    override fun createStub(
            accessorProperty: ObjJAccessorPropertyImpl, parentStub: StubElement<*>): ObjJAccessorPropertyStub {
        val containingClass = accessorProperty.containingClassName
        val variableDeclaration = accessorProperty.getParentOfType( ObjJInstanceVariableDeclaration::class.java)
        val variableName: String?
        val variableType: String?
        if (variableDeclaration != null) {
            val variableDeclarationStub = variableDeclaration.stub
            variableType = variableDeclarationStub?.varType ?: variableDeclaration.formalVariableType.text
            variableName = variableDeclarationStub?.variableName ?: if (variableDeclaration.variableName != null) variableDeclaration.variableName!!.text else null
        } else {
            variableName = null
            variableType = null
        }
        val getter = if (variableName != null && variableType != null) getGetterSelector(variableName, variableType, accessorProperty) else null
        val setter = if (variableName != null && variableType != null) getSetterSelector(variableName, variableType, accessorProperty) else null
        return ObjJAccessorPropertyStubImpl(parentStub, containingClass, variableType, variableName, getter, setter, shouldResolve(accessorProperty.node))
    }

    @Throws(IOException::class)
    override fun serialize(
            stub: ObjJAccessorPropertyStub,
            stream: StubOutputStream) {
        stream.writeName(stub.containingClass)
        stream.writeName(Strings.notNull(stub.varType))
        stream.writeName(Strings.notNull(stub.variableName))
        stream.writeName(Strings.notNull(stub.getter))
        stream.writeName(Strings.notNull(stub.setter))
        stream.writeBoolean(stub.shouldResolve())
    }

    @Throws(IOException::class)
    override fun deserialize(
            stream: StubInputStream, parentStub: StubElement<*>): ObjJAccessorPropertyStub {
        val containingClass = StringRef.toString(stream.readName())
        val varType = StringRef.toString(stream.readName())
        val variableName = StringRef.toString(stream.readName())
        val getter = StringRef.toString(stream.readName())
        val setter = StringRef.toString(stream.readName())
        val shouldResolve = stream.readBoolean()
        return ObjJAccessorPropertyStubImpl(parentStub, containingClass, varType, variableName, getter, setter, shouldResolve)
    }

    override fun indexStub(stub: ObjJAccessorPropertyStub, sink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexAccessorProperty(stub, sink)
    }
}