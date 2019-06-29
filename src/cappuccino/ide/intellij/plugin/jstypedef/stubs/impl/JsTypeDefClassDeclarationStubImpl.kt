package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefClassElementImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefInterfaceElementImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefClassStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefInterfaceStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefClassStubImpl(
        parent:StubElement<*>,
        override val fileName:String,
        override val enclosingNamespaceComponents: List<String>,
        override val className: String,
        override val superTypes: Set<JsTypeListType>
) : JsTypeDefStubBaseImpl<JsTypeDefClassElementImpl>(parent, JsTypeDefStubTypes.JS_CLASS), JsTypeDefClassStub {
    override val enclosingNamespace: String
        get() = enclosingNamespaceComponents.joinToString(".")
}


class JsTypeDefInterfaceStubImpl(
        parent:StubElement<*>,
        override val fileName:String,
        override val enclosingNamespaceComponents: List<String>,
        override val className: String,
        override val superTypes: Set<JsTypeListType>
) : JsTypeDefStubBaseImpl<JsTypeDefInterfaceElementImpl>(parent, JsTypeDefStubTypes.JS_INTERFACE), JsTypeDefInterfaceStub {
    override val enclosingNamespace: String
        get() = enclosingNamespaceComponents.joinToString(".")
}