package cappuccino.ide.intellij.plugin.references

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement

import java.util.ArrayList

class ObjJClassNameReference(element: ObjJClassName) : PsiPolyVariantReferenceBase<ObjJClassName>(element, TextRange.create(0, element.textLength)) {
    private val className: String? = element.text

    override fun multiResolve(b: Boolean): Array<ResolveResult> {
        if (className == null) {
            return arrayOf()
        }
        if (DumbService.isDumb(myElement.project)) {
            return ResolveResult.EMPTY_ARRAY
        }
        val classNames = ArrayList<ObjJClassName>()
        val classDeclarations = ObjJClassDeclarationsIndex.instance.get(className, myElement.project)
        if (classDeclarations.isEmpty()) {
            return ResolveResult.EMPTY_ARRAY
        }

        for (classDec in classDeclarations) {
            val classDecName = classDec.getClassName()
            if (classDecName != null && !classDecName!!.getText().isEmpty() && !classDecName!!.isEquivalentTo(myElement) && classDec.shouldResolve()) {
                classNames.add(classDecName)
            }
        }
        return PsiElementResolveResult.createResults(classNames)
    }

    override fun getVariants(): Array<Any> {
        val keys = ArrayList<Any>(ObjJClassDeclarationsIndex.instance.getAllResolveableKeys(myElement.project))
        return keys.toTypedArray()
    }
}