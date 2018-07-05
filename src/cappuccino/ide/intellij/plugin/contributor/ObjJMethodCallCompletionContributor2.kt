package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.GENERIC_METHOD_SUGGESTION_PRIORITY
import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.TARGETTED_METHOD_SUGGESTION_PRIORITY
import cappuccino.ide.intellij.plugin.contributor.utils.ObjJSelectorLookupUtil
import cappuccino.ide.intellij.plugin.indices.ObjJClassInstanceVariableAccessorMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJInstanceVariablesByNameIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.lang.ObjJIcons
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import java.util.logging.Level
import java.util.logging.Logger

object ObjJMethodCallCompletionContributor2 {

    private val LOGGER = Logger.getLogger(ObjJMethodCallCompletionContributor2::class.java.name)
    val CARET_INDICATOR = ObjJCompletionContributor.CARET_INDICATOR


    /**
     * Method to manage adding selector lookup elements to result set
     * @param result completion result set
     * @param psiElement currently editing psi element
     */
    internal fun addSelectorLookupElementsFromSelectorList(
            result: CompletionResultSet,
            psiElement: PsiElement?) {
        if (psiElement == null) {
            LOGGER.log(Level.SEVERE, "Cannot add selector lookup elements. Selector element is null")
            return
        }
        val methodCall = psiElement.getParentOfType(ObjJMethodCall::class.java)
                ?: //LOGGER.log(Level.INFO, "Cannot get completion parameters. Method call is null.");
                return
        addMethodCallCompletions(result, psiElement, methodCall)
    }

    private fun addMethodCallCompletions(result: CompletionResultSet, psiElement: PsiElement, elementsParentMethodCall: ObjJMethodCall?) {

        //LOGGER.log(Level.INFO, "Add method call completions");
        if (elementsParentMethodCall == null) {
            LOGGER.log(Level.SEVERE, "Cannot add method call completions. Method call parent element is null")
            return
        }
        if (DumbService.isDumb(psiElement.project)) {
            return
        }

        val selectors: List<ObjJSelector> = getSelectorsFromIncompleteMethodCall(psiElement, elementsParentMethodCall)
        val selectorString: String = ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors)
        val selectorIndex:Int = getSelectorIndex(selectors, psiElement)
        val selector: ObjJSelector? = if (selectorIndex >= 0 && selectorIndex < selectors.size) selectors[selectorIndex] else null

        //Determine target scope
        val scope:TargetScope = getTargetScope(elementsParentMethodCall)
        LOGGER.log(Level.INFO, String.format("Call target: <%s> has scope of <%s> with selector: <%s>", elementsParentMethodCall.callTargetText, scope.toString(), selectorString))
        //Determine possible containing class names
        val possibleContainingClassNames:List<String> = when {
            scope == TargetScope.STATIC -> ObjJInheritanceUtil.getAllInheritedClasses(elementsParentMethodCall.callTargetText, psiElement.project)
            selector != null -> ObjJSelectorReferenceResolveUtil.getClassConstraints(selector)
            else -> emptyList()
        }
        //Add actual method call completions
        addMethodDeclarationLookupElements(psiElement.project, result, possibleContainingClassNames, scope, selectorString, selectorIndex)

        val hasLocalScope:Boolean = (scope == TargetScope.INSTANCE || scope == TargetScope.ANY)
        // Add accessor and instance variable elements if selector size is equal to one
        // Accessors methods only apply to single element selectors
        if (hasLocalScope && selectors.size == 1) {
            addAccessorLookupElements(result, psiElement.project, possibleContainingClassNames, selectorString)
        }
    }

    private fun addMethodDeclarationLookupElements(project:Project, result:CompletionResultSet, possibleContainingClassNames: List<String>, targetScope: TargetScope, selectorString: String, selectorIndex:Int) {
        val methodHeaders: List<ObjJMethodHeaderDeclaration<*>> = ObjJUnifiedMethodIndex.instance.getByPatternFlat(selectorString.replace(CARET_INDICATOR, "(.*)"), project)
        if (methodHeaders.isEmpty()) {
            return
        }
        LOGGER.log(Level.INFO, "Found <"+methodHeaders.size+"> method headers in list")
        for (methodHeader:ObjJMethodHeaderDeclaration<*> in methodHeaders) {
            ProgressIndicatorProvider.checkCanceled()
            LOGGER.log(Level.INFO, String.format("Scope for target is <%s>; Method scope is <%s>;",targetScope.toString(),if(methodHeader.isStatic)"static" else "instance"))
            //Determine if method call matches scope, continue loop if it does not
            if (!inScope(targetScope, methodHeader)){ continue }
            //Get the selector at index, or continue loop
            val selector: ObjJSelector = getSelectorAtIndex(methodHeader, selectorIndex) ?: continue
            //Determine the priority
            val priority:Double = getPriority(possibleContainingClassNames, selector.containingClassName, TARGETTED_METHOD_SUGGESTION_PRIORITY, GENERIC_METHOD_SUGGESTION_PRIORITY)
            //Add the lookup element
            ObjJSelectorLookupUtil.addSelectorLookupElement(result, selector, selectorIndex, priority)
        }
    }

    private fun inScope(scope:TargetScope, methodHeader:ObjJMethodHeaderDeclaration<*>) : Boolean {
        return when (scope) {
            TargetScope.STATIC -> methodHeader.isStatic
            TargetScope.INSTANCE -> !methodHeader.isStatic
            else -> true
        }
    }

    private fun getSelectorAtIndex(methodHeader: ObjJMethodHeaderDeclaration<*>, selectorIndex: Int) : ObjJSelector? {
        return if (methodHeader.selectorList.isNotEmpty()) {
            return methodHeader.selectorList[selectorIndex]
        } else {
            null
        }
    }

    /**
     * adds instance variables as lookup elements to result set
     * @param result completion result set
     * @param project containing project
     * @param possibleContainingClassNames possible class names to determine completion priority
     * @param selectorStringIn selector string to match
     */
    private fun addAccessorLookupElements(result: CompletionResultSet, project: Project, possibleContainingClassNames: List<String>, selectorStringIn: String?) {
        // Determines if string contains caret indicator
        // If it does, it changes the index fetch method
        val hasCaretIndicator:Boolean = selectorStringIn != null && selectorStringIn.indexOf(CARET_INDICATOR) > -1
        //Gets the selector string with wildcard as necessary
        val selectorString = getWildCardSelectorStringOrNull(selectorStringIn) ?: return
        //Add Declaration Selectors
        val declarations: List<ObjJInstanceVariableDeclaration> = getInstanceVariableDeclarationsForSelector(selectorString, hasCaretIndicator, project)
        for (instanceVariableDeclaration in declarations) {
            ProgressIndicatorProvider.checkCanceled()
            addInstanceVariableDeclarationCompletion(result, possibleContainingClassNames, instanceVariableDeclaration)
        }
        //Add Accessors
        val accessors: List<ObjJInstanceVariableDeclaration> = getAccessorsForSelector(selectorString, hasCaretIndicator, project)
        for (instanceVariable in accessors) {
            ProgressIndicatorProvider.checkCanceled()
            addInstanceVariableAccessorMethods(result, possibleContainingClassNames, instanceVariable)
        }
    }

    /**
     * Inserts a wildcard regex placeholder in place of the caret indicator
     * @return null if string is null, which is a possibility
     */
    private fun getWildCardSelectorStringOrNull(selectorStringIn: String?) : String? {
        if (selectorStringIn == null || selectorStringIn.isEmpty()) {
            return null
        }
        val caretIndicatorIndex = selectorStringIn.indexOf(CARET_INDICATOR)
        return if (caretIndicatorIndex >= 0) selectorStringIn.replace(CARET_INDICATOR, "(.*)") else selectorStringIn
    }

    /**
     * Gets the instance variables list determined by whether or not there was a placeholder caret
     */
    private fun getInstanceVariableDeclarationsForSelector(selectorString: String, hasCaretIndicator:Boolean, project:Project) : List<ObjJInstanceVariableDeclaration> {
        return if (hasCaretIndicator) {
            ObjJInstanceVariablesByNameIndex.instance.getByPatternFlat(selectorString, project)
        } else {
            ObjJInstanceVariablesByNameIndex.instance[selectorString, project]
        }
    }

    /**
     * Gets all accessors matching this property
     */
    private fun getAccessorsForSelector(selectorString:String, hasCaretIndicator: Boolean, project:Project) : List<ObjJInstanceVariableDeclaration>{
        return if (hasCaretIndicator) {
            ObjJClassInstanceVariableAccessorMethodIndex.instance.getByPatternFlat(selectorString, project)
        } else {
            ObjJClassInstanceVariableAccessorMethodIndex.instance[selectorString, project]
        }
    }

    /**
     * Adds simple suggestions for instance variables, without accessor methods.
     * These can be called simply by their name without alteration
     * Variables can still have accessors and be accessed by name
     */
    private fun addInstanceVariableDeclarationCompletion(result:CompletionResultSet, possibleContainingClassNames:List<String>, instanceVariableDeclaration:ObjJInstanceVariableDeclaration) {
        if (instanceVariableDeclaration.variableName == null) {
            return
        }
        //ProgressIndicatorProvider.checkCanceled();
        val priority = if (possibleContainingClassNames.contains(instanceVariableDeclaration.containingClassName))
            ObjJCompletionContributor.TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY
        else
            ObjJCompletionContributor.GENERIC_INSTANCE_VARIABLE_SUGGESTION_PRIORITY

        val className = instanceVariableDeclaration.containingClassName
        val variableName = instanceVariableDeclaration.variableName!!.name
        val variableType = instanceVariableDeclaration.formalVariableType.text
        ObjJSelectorLookupUtil.addSelectorLookupElement(result, variableName, className, "<$variableType>", priority, false, ObjJIcons.VARIABLE_ICON)
    }


    /**
     * Adds an instance variable accessor method
     * Instance variable can have both getters and setters, and this covers both
     */
    private fun addInstanceVariableAccessorMethods(result: CompletionResultSet, possibleContainingClassNames:List<String>, instanceVariable: ObjJInstanceVariableDeclaration) {
        //Get className
        val className = instanceVariable.containingClassName

        //Find completion contribution list priority
        val priority:Double = getPriority(possibleContainingClassNames, className, TARGETTED_METHOD_SUGGESTION_PRIORITY,GENERIC_METHOD_SUGGESTION_PRIORITY)

        //Add Getter
        val getter = instanceVariable.getter
        if (getter != null) {
            ObjJSelectorLookupUtil.addSelectorLookupElement(result, getter.selectorString, className, "<" + instanceVariable.formalVariableType.text + ">", priority, false, ObjJIcons.ACCESSOR_ICON)
        }
        //Add Setter
        val setter = instanceVariable.setter
        if (setter != null) {
            ObjJSelectorLookupUtil.addSelectorLookupElement(result, setter.selectorString, className, "<" + instanceVariable.formalVariableType.text + ">", priority, false, ObjJIcons.ACCESSOR_ICON)
        }
    }


    /**
     * Gets the scope for the suggested methods we should have
     */
    private fun getTargetScope(methodCall:ObjJMethodCall) : TargetScope {
        return when {
            ObjJImplementationDeclarationsIndex.instance[methodCall.callTargetText, methodCall.project].isNotEmpty() -> TargetScope.STATIC
            else -> TargetScope.ANY
        }
    }

    /**
     * Gets the index of the selector that we are looking for suggestions on
     */
    private fun getSelectorIndex(selectors:List<ObjJSelector>, psiElement: PsiElement) : Int {
        for (i in selectors.indices) {
            if (selectors[i] equals psiElement) {
                return i
            }
        }
        return selectors.size - 1
    }

    private fun getPriority(possibleContainingClassNames: List<String>, className:String, priorityIfTarget:Double, priorityIfNotTarget:Double) : Double {
        return if (possibleContainingClassNames.isEmpty() || possibleContainingClassNames.contains(className)) {
            priorityIfTarget
        } else {
            priorityIfNotTarget
        }
     }

    private enum class TargetScope {
        STATIC,
        INSTANCE,
        ANY;
    }

}