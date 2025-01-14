package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNamedElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJUniversalFunctionElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJQualifiedReferenceComponentPartType.VARIABLE_NAME
import cappuccino.ide.intellij.plugin.stubs.types.toStubParts
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import java.util.*

object ObjJFunctionDeclarationPsiUtil {

    /**
     * Gets the functions name
     */
    fun getName(functionDeclaration: ObjJFunctionDeclaration): String {
        return functionDeclaration.functionName.text ?: ""
    }

    /**
     * Renames function
     *
     * @param functionDeclaration function to rename
     * @param name                new function name
     * @return function name element
     * @throws IncorrectOperationException exception
     */
    @Throws(IncorrectOperationException::class)
    fun setName(
            functionDeclaration: ObjJFunctionDeclaration,
            name: String): ObjJFunctionName {
        val oldFunctionName: ObjJFunctionName? = functionDeclaration.functionName
        val newFunctionName = ObjJElementFactory.createFunctionName(functionDeclaration.project, name)
        Logger.getInstance(ObjJPsiImplUtil::class.java).assertTrue(newFunctionName != null)
        if (oldFunctionName == null) {
            if (functionDeclaration.formalParameterList?.openParen != null) {
                functionDeclaration.addBefore(functionDeclaration.formalParameterList?.openParen!!, newFunctionName)
            } else {
                functionDeclaration.addBefore(functionDeclaration.firstChild, newFunctionName)
            }
        } else {
            functionDeclaration.node.replaceChild(oldFunctionName.node, newFunctionName!!.node)
        }
        return newFunctionName!!
    }

    /**
     * Renames function literal node.
     *
     * @param functionLiteral the literal to rename
     * @param name            the new name
     * @return this function literal
     * @throws IncorrectOperationException exception
     */
    @Throws(IncorrectOperationException::class)
    fun setName(
            functionLiteral: ObjJFunctionLiteral,
            name: String): ObjJFunctionLiteral {
        //Get existing name node.
        val oldFunctionName = functionLiteral.functionNameNode ?: return functionLiteral

        //Create new name node
        val newFunctionName = ObjJElementFactory.createFunctionName(functionLiteral.project, name)
        Logger.getInstance(ObjJPsiImplUtil::class.java).assertTrue(newFunctionName != null)

        //Replace node
        oldFunctionName.parent.node.replaceChild(oldFunctionName.node, newFunctionName!!.node)
        return functionLiteral
    }


    /**
     * Renames a preproc function
     */
    fun setName(defineFunction: ObjJPreprocessorDefineFunction, name: String): PsiElement {
        if (defineFunction.functionName != null) {
            val functionName = ObjJElementFactory.createFunctionName(defineFunction.project, name)
            if (functionName != null) {
                defineFunction.node.replaceChild(defineFunction.functionName!!.node, functionName.node)
            }
        } else if (defineFunction.formalParameterList?.openParen != null) {
            val functionName = ObjJElementFactory.createFunctionName(defineFunction.project, name)
            if (functionName != null) {
                defineFunction.addBefore(defineFunction.formalParameterList?.openParen!!, functionName)
            }
        } else if (defineFunction.variableName != null) {
            val newVariableName = ObjJElementFactory.createVariableName(defineFunction.project, name)
            defineFunction.node.replaceChild(defineFunction.variableName!!.node, newVariableName.node)
        }
        return defineFunction
    }

    /**
     * Gets this functions qualified name component
     */
    fun getQualifiedNameText(functionCall: ObjJFunctionCall): String? {
        return functionCall.functionName?.text
    }

    /**
     * Gets a function literals name if any.
     * returns empty string if no name found
     */
    fun getFunctionNameAsString(functionLiteral: ObjJFunctionLiteral): String {
        if (functionLiteral.stub != null) {
            return functionLiteral.stub.fqName
        }
        // Get containing varaible declaration if any
        // if not, return
        val variableDeclaration = functionLiteral.getParentOfType( ObjJVariableDeclaration::class.java)
                ?: return ""

        // Get qualified reference name list.
        // Declarations can contain a string of references pointing to the same variable
        // In this case the function literal
        val qualifiedReferenceList = variableDeclaration.qualifiedReferenceList

        // Loop through qualified name parts, and search for a qualified name with a single name element
        for (qualifiedReference in qualifiedReferenceList) {
            val parts = qualifiedReference.qualifiedNameParts
            if (parts.size > 1) continue
            // Must check if variable name, as some qualified name parts can also be method calls or function calls
            // Unlikely here as it should not be assignable to, but have to be sure.
            val variableName = (parts[0] as? ObjJVariableName)?.text ?: continue
            if (variableName.isNotEmpty())
                return variableName
        }
        return ""
    }

    /**
     * Gets the preprocessor function definitions name as a string
     */
    fun getFunctionNameAsString(functionDeclaration: ObjJPreprocessorDefineFunction): String {
        if (functionDeclaration.stub != null) {
            return functionDeclaration.stub.functionName
        }
        return if (functionDeclaration.functionName != null) functionDeclaration.functionName!!.text else if (functionDeclaration.variableName != null) functionDeclaration.variableName!!.text else "{UNDEF}"
    }

    /**
     * Gets a list of function names for a function literal
     */
    fun getFunctionNamesAsString(functionLiteral: ObjJFunctionLiteral): List<String> {
        val out = ArrayList<String>()
        val variableDeclaration = functionLiteral.getParentOfType( ObjJVariableDeclaration::class.java)
        if (variableDeclaration == null || variableDeclaration.qualifiedReferenceList.isEmpty()) {
            return emptyList()
        }
        for (reference in variableDeclaration.qualifiedReferenceList) {
            val nameParts = reference.toStubParts()
            if (nameParts.size != 1 || nameParts[0].type != VARIABLE_NAME)
                continue
            val name = nameParts[0].name ?: continue
            if (name.isNotBlank()) {
                out.add(name)
            }
        }
        return out
    }

    /**
     * Gets the function definitions name as a string
     */
    fun getFunctionNameAsString(functionDeclaration: ObjJFunctionDeclaration): String {
        if (functionDeclaration.stub != null) {
            return functionDeclaration.stub.functionName
        }
        return functionDeclaration.functionName.text
    }

    /**
     * Gets function parameters' variable name elements
     */
    fun getParameterNameElements(
            functionDeclaration: ObjJFunctionDeclarationElement<*>): List<ObjJVariableName> {
        val out = mutableListOf<ObjJVariableName?>()
        for (parameterArg in functionDeclaration.formalParameterArgList) {
            out.add(parameterArg.variableName)
        }
        if (functionDeclaration.lastFormalParameterArg != null) {
            out.add(functionDeclaration.lastFormalParameterArg!!.variableName)
        }
        return out.filterNotNull()

    }

    /**
     * Gets function parameters' names as strings
     */
    fun getParameterNames(
            functionDeclaration: ObjJFunctionDeclarationElement<*>): List<String> {
        if (functionDeclaration.stub != null) {

            return (functionDeclaration.stub as ObjJFunctionDeclarationElementStub<*>).parameterNames
        }
        val out = mutableListOf<String?>()
        for (parameterArg in functionDeclaration.formalParameterArgList) {
            out.add(parameterArg.variableName?.text)
        }
        if (functionDeclaration.lastFormalParameterArg != null) {
            out.add(functionDeclaration.lastFormalParameterArg?.variableName?.text)
        }
        return out.filterNotNull()
    }

    fun getReturnTypes(functionDeclaration: ObjJFunctionDeclarationElement<*>, tag: Tag) : InferenceResult? {
        val stubReturnType = functionDeclaration.stub?.returnType
        if (stubReturnType != null) {
            return stubReturnType.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX).toJsTypeList().let {
                InferenceResult(types = it)
            }
        }
        return inferFunctionDeclarationReturnType(functionDeclaration, tag)
    }

    /**
     * A method to get the function scope of a given function
     * This is to prevent a current problem of resolving to functions outside scope.
     */
    fun getFunctionScope(functionDeclaration:ObjJFunctionDeclarationElement<*>, useStub:Boolean = true) : ObjJFunctionScope {
        if (useStub) {
            val stubScope = functionDeclaration.stub?.scope
            if (stubScope != null) {
                return stubScope
            }
        }

        if (functionDeclaration.parent is ObjJFile || functionDeclaration.parent is PsiFile || functionIsEnclosedGlobal(functionDeclaration)) {
            return ObjJFunctionScope.GLOBAL_SCOPE
        }

        if (functionDeclaration.parent is ObjJBlock) {
            return ObjJFunctionScope.PRIVATE
        }

        if (functionDeclaration.hasParentOfType(ObjJPreprocessorDefineFunction::class.java))
            return ObjJFunctionScope.GLOBAL_SCOPE

        if (functionDeclaration.functionNameNode == null)
            return ObjJFunctionScope.INVALID

        val expr = functionDeclaration.getParentOfType(ObjJExpr::class.java) ?: return ObjJFunctionScope.PRIVATE

        if (expr.parent is ObjJArguments || expr.parent is ObjJQualifiedMethodCallSelector) {
            return ObjJFunctionScope.PARAMETER_SCOPE
        }

        if (expr.parent is ObjJGlobalVariableDeclaration) {
            return ObjJFunctionScope.GLOBAL_SCOPE
        }

        val variableDeclaration = expr.parent as? ObjJVariableDeclaration ?: return ObjJFunctionScope.PRIVATE
        val bodyDeclaration = variableDeclaration.parent?.parent as? ObjJBodyVariableAssignment
        if (bodyDeclaration?.varModifier != null) {
            return ObjJFunctionScope.FILE_SCOPE
        }

        /*
        var largestScope = Int.MAX_VALUE
        variableDeclaration.qualifiedReferenceList.forEach {
            val parts = it.qualifiedNamesList
            if (parts.size != 1)
                return@forEach
            val part = parts.getOrNull(0) as? ObjJVariableName ?: return@forEach;
            // @todo should possibly return global scope as I think that is how javascript works
            val reference = part.reference.resolve() ?: return ObjJFunctionScope.PRIVATE
            if (reference.parent is ObjJGlobalVariableDeclaration) {
                return ObjJFunctionScope.GLOBAL_SCOPE
            }
            val qnameParent = reference.parent?.parent?.parent?.parent as? ObjJBodyVariableAssignment
            if (qnameParent == null || qnameParent.parent !is ObjJFile) {
                largestScope = min(largestScope, ObjJFunctionScope.PRIVATE.intVal)
                return@forEach
            }
            largestScope = min(largestScope, ObjJFunctionScope.FILE_SCOPE.intVal)
        }

*/
        return ObjJFunctionScope.PRIVATE
    }


    private fun functionIsEnclosedGlobal(functionDeclaration:ObjJFunctionDeclarationElement<*>) : Boolean {
        if(false && !DumbService.isDumb(functionDeclaration.project)) {
            return functionIsEnclosedGlobalStrict(functionDeclaration)
        }
        val variableDeclaration = functionDeclaration.parent.parent.parent as? ObjJVariableDeclaration ?: return false
        val isBodyVariableAssignmentLocal
                = (variableDeclaration.parent.parent as? ObjJBodyVariableAssignment)?.varModifier != null
        val isNested = variableDeclaration.qualifiedReferenceList.all { it.qualifiedNameParts.size > 1 }
        if (isBodyVariableAssignmentLocal || isNested)
            return false
        val functionName = functionDeclaration.functionNameString
        return !variableDeclaration.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true).any { bodyVariableAssignment ->
            bodyVariableAssignment.variableDeclarationList?.variableDeclarationList?.any { variableDec ->
                variableDec.qualifiedReferenceList.any {
                    if (it.qualifiedNameParts.size == 1 && it.qualifiedNameParts[0].text == functionName)
                        bodyVariableAssignment.varModifier != null
                    else
                        false
                }
            }.orFalse()
        }
    }

    private fun functionIsEnclosedGlobalStrict(functionDeclaration: ObjJFunctionDeclarationElement<*>) : Boolean {
        val variableDeclaration = functionDeclaration.parent.parent.parent as? ObjJVariableDeclaration ?: return false
        val isBodyVariableAssignmentLocal
                = (variableDeclaration.parent.parent as? ObjJBodyVariableAssignment)?.varModifier != null
        val isNested = variableDeclaration.qualifiedReferenceList.all { it.qualifiedNameParts.size > 1 }
        if (isBodyVariableAssignmentLocal || isNested)
            return false
        val functionName = functionDeclaration.functionNameString
        return !variableDeclaration.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true).any { bodyVariableAssignment ->
            bodyVariableAssignment.variableDeclarationList?.variableDeclarationList?.any variableDec@{ variableDec ->
                variableDec.qualifiedReferenceList.any{
                    if (it.qualifiedNameParts.size != 1 && it.qualifiedNameParts[0].text != functionName)
                        return@variableDec false
                    val qualifiedReference = it.qualifiedNameParts[0].reference?.resolve()?.parent as? ObjJQualifiedReference ?: return@variableDec false
                    if (qualifiedReference.hasParentOfType(ObjJExpr::class.java))
                        return false
                    return qualifiedReference.getParentOfType(ObjJBodyVariableAssignment::class.java)?.varModifier != null
                }
            }.orFalse()
        }
    }


    @Suppress("unused")
    private fun isParameterScope(functionDeclaration:ObjJFunctionDeclarationElement<*>) : Boolean {
        return PsiTreeUtil.getParentOfType(functionDeclaration,
                ObjJArguments::class.java,
                ObjJMethodCall::class.java) != null
    }

    /**
     * Gets the function name node in  a function literal
     */
    fun getFunctionNameNode(
        functionLiteral: ObjJFunctionLiteral
    ): ObjJNamedElement? {

        // If is namelessParameter, ignore
        val expr = functionLiteral.getParentOfType(ObjJExpr::class.java)
        if (expr != null) {

        }
        // Get containing varaible declaration if any
        // if not, return
        val variableDeclaration = functionLiteral.getParentOfType( ObjJVariableDeclaration::class.java)
                ?: return null

        // Get qualified reference name list.
        // Declarations can contain a string of references pointing to the same variable
        // In this case the function literal
        val qualifiedReferenceList = variableDeclaration.qualifiedReferenceList

        // Loop through qualified name parts, and search for a qualified name with a single name element
        for (qualifiedReference in qualifiedReferenceList) {
            val parts = qualifiedReference.qualifiedNameParts
            if (parts.size > 1) continue

            // Must check if variable name, as some qualified name parts can also be method calls or function calls
            // Unlikely here as it should not be assignable to, but have to be sure.
            val variableName = (parts[0] as? ObjJVariableName) ?: continue

            // @todo check if returning first is the best course of action
            if (variableName.text.isNotEmpty())
                return variableName
        }
        return null
    }

    fun getParameterType(parameterArg:ObjJFormalParameterArg) : String? {
        val previousNode = parameterArg.getPreviousNonEmptyNode(true)
        if (previousNode?.elementType in ObjJTokenSets.COMMENTS) {
            val commentTokens = previousNode!!.text.trim().split(" ".toRegex())
            if (commentTokens.size == 1) {
                return commentTokens[0]
            }
        }
        val index = parameterArg.getParentOfType(ObjJFunctionDeclarationElement::class.java)?.formalParameterArgList?.indexOf(parameterArg) ?: -1
        val docComment = parameterArg.docComment
                ?: return null
        val variableName = parameterArg.variableName?.text
                ?: return null
        val parameter = docComment.getParameterComment(variableName) ?: docComment.getParameterComment(index)
            ?: return null
        return parameter.types?.withoutAnyType()?.joinToString("|")
    }

    fun getParentFunctionDeclaration(element:PsiElement?) : ObjJUniversalFunctionElement? {
        if (element == null)
            return null

        val parentFunctionDeclaration = element.parent as? ObjJFunctionDeclarationElement<*>
        if (parentFunctionDeclaration != null)
            return parentFunctionDeclaration
        val qualifiedReference = element.parent as? ObjJQualifiedReference ?: element as? ObjJQualifiedReference
        val variableDeclaration = qualifiedReference?.parent as? ObjJVariableDeclaration
        val expr:ObjJExpr = if (variableDeclaration != null) {
            variableDeclaration.expr
        } else {
            val globalFunctionDeclaration = element.parent as? ObjJGlobalVariableDeclaration
            globalFunctionDeclaration?.expr
        } ?: return null
        return (expr.leftExpr?.functionDeclaration as? ObjJUniversalFunctionElement) ?: (expr.leftExpr?.functionLiteral as? ObjJUniversalFunctionElement) ?: element.parentFunctionDeclarationNoCache
    }

    fun isNullableParameter(@Suppress("UNUSED_PARAMETER") it:ObjJFormalParameterArg) : Boolean {
        return false
    }

}

fun ObjJFunctionName.resolve() : PsiElement? {
    return this.reference.resolve()
}

val ObjJFunctionCall.functionDeclarationReference:ObjJUniversalFunctionElement? get() {
    val functionName = this.functionName?: return null
    if (indexInQualifiedReference == 0) {
        val jsResolved = JsTypeDefFunctionsByNameIndex.instance[functionName.text, project].firstOrNull()
        if (jsResolved != null)
            return jsResolved
        val objResolved = ObjJFunctionsIndex.instance[functionName.text, project].firstOrNull()
        if (objResolved != null)
            return objResolved
    }
    val resolved = functionName.resolve() ?: return null
    return resolved.parentFunctionDeclaration
}

val ObjJFormalParameterArg.nullable : Boolean get() {
    return ObjJFunctionDeclarationPsiUtil.isNullableParameter(this)
}

val ObjJFormalParameterArg.parameterType : String? get() {
    return ObjJFunctionDeclarationPsiUtil.getParameterType(this)
}
