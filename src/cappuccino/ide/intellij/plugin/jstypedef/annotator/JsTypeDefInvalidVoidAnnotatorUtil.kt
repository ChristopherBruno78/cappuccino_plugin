package cappuccino.ide.intellij.plugin.jstypedef.annotator

import cappuccino.ide.intellij.plugin.jstypedef.fixes.RemoveElementInPipedListFix
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefGenericTypeTypes
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefNoVoid
import cappuccino.ide.intellij.plugin.psi.utils.hasAnyParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

/**
 * Annotates invalid use of void keyword
 */
internal fun annotateInvalidVoidStatements (
            element: PsiElement,
            annotationHolder: AnnotationHolder) {
    val hasNonType = element.hasAnyParentOfType(
            JsTypeDefNoVoid::class.java,
            JsTypeDefGenericTypeTypes::class.java
    )
    if (!hasNonType)
        return
    val annotation = annotationHolder
            .newAnnotation(HighlightSeverity.ERROR,
                    JsTypeDefBundle.message("jstypedef.annotation.error.invalid-void.message")
            )
            .range(element.textRange)
            .withFix(RemoveElementInPipedListFix(
                    element,
                    JsTypeDefBundle.message("jstypedef.annotation.error.invalid-void.quick-fix.remove-void.message"))
            )
            .create()
}