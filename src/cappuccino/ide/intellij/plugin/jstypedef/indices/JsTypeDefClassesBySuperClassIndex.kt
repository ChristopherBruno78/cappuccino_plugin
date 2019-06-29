package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.IndexKeyUtil
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import com.intellij.psi.stubs.StubIndexKey

class JsTypeDefClassesBySuperClassIndex private constructor() : JsTypeDefStringStubIndexBase<JsTypeDefClassDeclaration<*>>() {

    override val indexedElementClass: Class<JsTypeDefClassDeclaration<*>>
        get() = JsTypeDefClassDeclaration::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, JsTypeDefClassDeclaration<*>> {
        return KEY
    }

    companion object {

        val instance = JsTypeDefClassesBySuperClassIndex()

        val KEY = IndexKeyUtil.createIndexKey(JsTypeDefClassesBySuperClassIndex::class.java)

        private const val VERSION = 1
    }


}
