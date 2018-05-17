package cappuccino.ide.intellij.plugin.references

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.DefinitionsScopedSearch
import com.intellij.psi.search.searches.DefinitionsScopedSearch.SearchParameters
import com.intellij.psi.search.searches.ExtensibleQueryFactory
import com.intellij.util.Query
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement

class ObjJClassInheritersSearch : ExtensibleQueryFactory<ObjJClassDeclarationElement<*>, SearchParameters>() {
    companion object {

        private val INSTANCE = ObjJClassInheritersSearch()

        @JvmOverloads
        fun search(aClass: ObjJClassDeclarationElement<*>, scope: SearchScope = GlobalSearchScope.allScope(aClass.project)): Query<ObjJClassDeclarationElement<*>> {
            return INSTANCE.createUniqueResultsQuery(SearchParameters(aClass))
        }
    }
}