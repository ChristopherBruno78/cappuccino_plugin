package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.fixes.ObjJAddSuppressInspectionForScope
import cappuccino.ide.intellij.plugin.fixes.ObjJAlterIgnoredSelector
import cappuccino.ide.intellij.plugin.fixes.ObjJSuppressInspectionScope
import cappuccino.ide.intellij.plugin.indices.ObjJClassInstanceVariableAccessorMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJInstanceVariablesByNameIndex
import cappuccino.ide.intellij.plugin.indices.ObjJSelectorInferredMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJQualifiedMethodCallSelector
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.psi.utils.getSelectorString
import cappuccino.ide.intellij.plugin.psi.utils.getSelectorStringFromSelectorList
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.lang.annotation.AnnotationBuilder
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project

/**
 * Annotator/Validator of method calls
 */
internal object ObjJMethodCallAnnotatorUtil {


    /**
     * Responsible for annotating method calls
     * @param methodCall method call to annotate
     * @param holder annotation holder used for markup
     */
    fun annotateMethodCall(
            methodCall: ObjJMethodCall,
            holder: AnnotationHolder) {

        //First validate that all selector sub elements are present
        validateMissingSelectorElements(methodCall, holder)

        //Validate the method for selector exists. if not, stop annotation
        if (!validMethodSelector(methodCall, holder)) {
            //Method call is invalid, stop annotations
            return
        }

        // Check that call target for method call is valid
        // Only used is setting for validate call target is set.
        if (ObjJPluginSettings.validateCallTarget()) {// && !IgnoreUtil.shouldIgnore(methodCall, ElementType.CALL_TARGET)) {
            //validateCallTarget(methodCall, holder)
        }
    }

    /**
     * Validates and annotates missing children of selector elements
     * somewhat of a hack for the way selector elements are handled in the psi tree
     * @param methodCall method call to evaluate
     * @param holder annotation holder
     */
    private fun validateMissingSelectorElements(methodCall: ObjJMethodCall, holder: AnnotationHolder) {
        if (methodCall.selectorList.size > 1) {
            for (selector in methodCall.qualifiedMethodCallSelectorList) {
                if (selector.exprList.isEmpty() && selector.selector != null) {
                    holder.newAnnotation(HighlightSeverity.ERROR, ObjJBundle.message("objective-j.annotator-messages.method-call-annotator.method-call-missing-expression.message"))
                            .range(selector.selector!!)
                            .create()
                    return
                }
            }
        }
    }

    /**
     * Validates and annotates method selector signature
     * @param methodCall method call
     * @param annotationHolder annotation annotationHolder
     * @return **true** if valid, **false** otherwise
     */
    private fun validMethodSelector(methodCall: ObjJMethodCall, annotationHolder: AnnotationHolder): Boolean {
        // Get project
        val project = methodCall.project

        //Checks that there are selectors
        val selectors = methodCall.selectorList
        if (selectors.isEmpty()) {
            return false
        }
        //Get full selector signature
        val fullSelector = getSelectorStringFromSelectorList(selectors)

        //Check that method selector signature is valid, and return if it is
        if (isValidMethodCall(fullSelector, project)) {
            return true
        }

        // Check if selector is ignored through annotations or other means
        if (isIgnored(methodCall, fullSelector, annotationHolder)) {
            return true
        }

        // If selector is single in size, markup simply
        if (selectors.size == 1) {
            val selector = selectors.getOrNull(0) ?: return true
            val messageKey = "objective-j.annotator-messages.method-call-annotator.selector-not-found.message";
            val message = ObjJBundle.message(messageKey, selector.getSelectorString(true))
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, message)
                    .range(selector)
                    .addInvalidSelectorFixes(methodCall, fullSelector)
                    .create()
            return false
        }

        //Selector is invalid, so find first non-matching selector
        val failIndex = getSelectorFailedIndex(methodCall.selectorStrings, project)

        //If fail index is less than one, mark all selectors and return;
        val messageKey = "objective-j.annotator-messages.method-call-annotator.selector-not-found.message";
        if (failIndex < 0) {
            val message = ObjJBundle.message(messageKey, fullSelector)
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, message)
                    .range(methodCall)
                    .addInvalidSelectorFixes(methodCall, fullSelector)
                    .create()
            return false
        }

        // Annotate all selectors individually
        annotateInvalidSelectorsIndividually(methodCall, selectors, failIndex, fullSelector, annotationHolder)
        return false
    }

    /**
     * Annotates only the selectors not matching any known selector
     * Highlights these and adds fixes on a selector by selector basis
     */
    private fun annotateInvalidSelectorsIndividually(methodCall: ObjJMethodCall, selectors: List<ObjJSelector>, failIndex: Int, fullSelector: String, annotationHolder: AnnotationHolder) {
        val selectorToFailPointTextSoFar = StringBuilder(getSelectorStringFromSelectorList(selectors.subList(0, failIndex)))
        val methodCallSelectors = methodCall.qualifiedMethodCallSelectorList
        val numSelectors = methodCallSelectors.size
        var selector: ObjJQualifiedMethodCallSelector
        //loop through invalid selectors and annotate them
        for (i in failIndex until numSelectors) {
            selector = methodCallSelectors.getOrNull(i) ?: return
            annotateInvalidSelector(methodCall, selector, selectorToFailPointTextSoFar, fullSelector, annotationHolder)
        }
    }

    /**
     * Annotates a single selector with an error annotation
     */
    private fun annotateInvalidSelector(
            methodCall: ObjJMethodCall,
            selector: ObjJQualifiedMethodCallSelector,
            selectorToFailPointTextSoFar: StringBuilder,
            fullSelector: String,
            annotationHolder: AnnotationHolder
    ) {
        // Uses fail point and not strictly the selector as some
        // qualified selectors do not have text selectors, but are just colons
        val failPoint = if (selector.selector != null && !selector.selector!!.text.isEmpty()) selector.selector else selector.colon

        // Append fail text to this option
        selectorToFailPointTextSoFar.append(getSelectorString(selector.selector, true))

        // Create annotation
        val messageKey = "objective-j.annotator-messages.method-call-annotator.selector-not-found.message"
        val message = ObjJBundle.message(messageKey, selectorToFailPointTextSoFar);
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, message)
                .range(failPoint!!)
                .needsUpdateOnTyping(true)
                .addInvalidSelectorFixes(methodCall, fullSelector) // Add fixes
                .create();
    }

    /**
     * Adds all fixes for an invalid selector
     * @todo add fix to add a selector matching to a class
     */
    private fun addInvalidSelectorFixes(
            annotation: AnnotationBuilder,
            methodCall: ObjJMethodCall,
            fullSelector: String
    ): AnnotationBuilder {
        var outBuilder = annotation.withFix(ObjJAlterIgnoredSelector(fullSelector, true))
        for (scope in scopeList) {
            val fix = ObjJAddSuppressInspectionForScope(methodCall, ObjJSuppressInspectionFlags.IGNORE_INVALID_SELECTOR, scope)
            outBuilder = outBuilder.withFix(fix)
        }
        return outBuilder
    }


    /**
     * Brute force method to check if method call is valid
     * @param fullSelector full selector for method call
     * @param project project
     * @return true if method selector is valid in any place, false otherwise
     */
    private fun isValidMethodCall(fullSelector: String, project: Project): Boolean {
        return !ObjJUnifiedMethodIndex.instance[fullSelector, project].isEmpty() ||
                !ObjJSelectorInferredMethodIndex.instance[fullSelector, project].isEmpty() ||
                !ObjJInstanceVariablesByNameIndex.instance[fullSelector.substring(0, fullSelector.length - 1), project].isEmpty() ||
                !ObjJClassInstanceVariableAccessorMethodIndex.instance[fullSelector, project].isEmpty() ||
                !ObjJClassInstanceVariableAccessorMethodIndex.instance[fullSelector, project].isEmpty()
    }

    /**
     * Checks whether or not a selector is in any way ignored.
     */
    private fun isIgnored(methodCall: ObjJMethodCall, fullSelector: String, annotationHolder: AnnotationHolder): Boolean {
        // Ensure that selector is not listed in project level ignore list
        if (ObjJPluginSettings.isIgnoredSelector(fullSelector)) {
            // If ignored, add fix to remove it from ignored list
            val messageKey = "objective-j.annotator-messages.method-call-annotator.invalid-selector-ignored.message";
            for (selector in methodCall.selectorList) {
                val message = ObjJBundle.message(messageKey, fullSelector);
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, message)
                        .range(selector)
                        .withFix(ObjJAlterIgnoredSelector(fullSelector, false))
                        .create()
            }
            return true
        }

        // Ensure that selector is not annoted with ignore statement
        if (ObjJCommentEvaluatorUtil.isIgnored(methodCall, ObjJSuppressInspectionFlags.IGNORE_INVALID_SELECTOR)) {
            return true
        }
        return false
    }

    /**
     * Gets index of selector where selector stops being valid.
     * This allows for partial matches where possibly the
     * first selector matches a method, but the second does not
     * @param selectors selector list
     * @param project project
     * @return index of first invalid selector
     */
    private fun getSelectorFailedIndex(selectors: List<String>, project: Project): Int {
        if (selectors.size < 2 || DumbService.isDumb(project)) {
            return 0
        }
        val builder = StringBuilder()
        var selector: String
        for (i in selectors.indices) {
            selector = selectors[i]
            builder.append(selector).append(ObjJMethodPsiUtils.SELECTOR_SYMBOL)
            selector = builder.toString()
            if (!ObjJUnifiedMethodIndex.instance.getStartingWith(selector, project).isEmpty() || !ObjJSelectorInferredMethodIndex.instance.getStartingWith(selector, project).isEmpty()) {
                continue
            }
            return i
        }
        return 0
    }

}


// List of scopes to add fixes to
private val scopeList = listOf(
        ObjJSuppressInspectionScope.STATEMENT,
        ObjJSuppressInspectionScope.METHOD,
        ObjJSuppressInspectionScope.FUNCTION,
        ObjJSuppressInspectionScope.CLASS,
        ObjJSuppressInspectionScope.FILE
)

/**
 * Adds all fixes for an invalid selector
 * @todo add fix to add a selector matching to a class
 */
private fun AnnotationBuilder.addInvalidSelectorFixes(
        methodCall: ObjJMethodCall,
        fullSelector: String
): AnnotationBuilder {
    var outBuilder = withFix(ObjJAlterIgnoredSelector(fullSelector, true))
    for (scope in scopeList) {
        val fix = ObjJAddSuppressInspectionForScope(methodCall, ObjJSuppressInspectionFlags.IGNORE_INVALID_SELECTOR, scope)
        outBuilder = outBuilder.withFix(fix)
    }
    return outBuilder
}
