package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportStatement

class ObjJImportsIndex : ObjJStringStubIndexBase<ObjJImportStatement<*>>() {

    override val indexedElementClass: Class<ObjJImportStatement<*>>
        get() = ObjJImportStatement::class.java

    override fun getKey(): StubIndexKey<String, ObjJImportStatement<*>> {
        return KEY
    }

    companion object {

        val instance = ObjJImportsIndex()
        private val KEY = IndexKeyUtil.createIndexKey<String, ObjJImportStatement<*>>(ObjJImportsIndex::class.java)
    }
}
