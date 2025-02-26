package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListArrayType
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefPropertiesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefClassElement
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJUniversalQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariablePsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.docComment
import cappuccino.ide.intellij.plugin.stubs.types.TYPES_DELIM
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import com.intellij.openapi.project.Project

internal fun inferQualifiedReferenceType(parts: List<ObjJUniversalQualifiedReferenceComponent>, tag: Tag): InferenceResult? {
    if (parts.isEmpty())
        return null
    addStatusFileChangeListener(parts[0].project)
    return internalInferQualifiedReferenceType(parts, tag)
}

internal fun internalInferQualifiedReferenceType(parts: List<ObjJUniversalQualifiedReferenceComponent>, tag: Tag): InferenceResult? {
    if (parts.isEmpty()) {
        return null
    }
    //ProgressIndicatorProvider.checkCanceled()
    val project: Project = parts[0].project
    var parentTypes: InferenceResult? = null
    var isStatic = false
    for (i in parts.indices) {
        val part = parts[i]
        val thisParentTypes = parentTypes
        parentTypes = part.getCachedInferredTypes(tag) {
            if (part.tagged(tag, false))
                return@getCachedInferredTypes null
            //ProgressIndicatorProvider.checkCanceled()
            if (parts.size == 1 && parts[0] is ObjJVariableName) {
                val variableName = parts[0] as ObjJVariableName
                val simpleType = simpleVariableInference(variableName)
                if (simpleType?.withoutAnyType().orEmpty().isNotEmpty())
                    return@getCachedInferredTypes simpleType
            }
            if (i == parts.lastIndex && (part.parent is ObjJVariableDeclaration || part.parent?.parent is ObjJVariableDeclaration || part.parent?.parent?.parent is ObjJVariableDeclaration)) {
                val variableDeclarationExpr = (part.parent as? ObjJVariableDeclaration
                                ?: part.parent.parent as? ObjJVariableDeclaration
                                ?: part.parent.parent.parent as ObjJVariableDeclaration
                        ).expr
                                ?: return@getCachedInferredTypes null
                inferExpressionType(variableDeclarationExpr, tag)
            } else if (i == 0) {
                getPartTypes(part, thisParentTypes, false, tag)
            } else {
                getPartTypes(part, thisParentTypes, isStatic, tag)
            }
        }
        if (isStatic) {
            isStatic = false
        }
        if (i == 0) {
            isStatic = JsTypeDefClassesByNameIndex.instance[part.text, project].any {
                it is JsTypeDefClassElement
            }
        }
    }
    if (parentTypes == null && parts.size == 1 && !ObjJVariablePsiUtil.isNewVariableDec(parts[0])) {
        return getFirstMatchesInGlobals(parts[0], tag)
    }
    return parentTypes
}

val SPLIT_JS_CLASS_TYPES_LIST_REGEX = """\s*\$TYPES_DELIM\s*""".toRegex()

private fun simpleVariableInference(variableName: ObjJVariableName) : InferenceResult? {
    val variableNameString = variableName.text
    val variableDefTypeSimple = variableName.docComment
            ?.getParameterComment(variableNameString)
            ?.types
            ?: return null
    return if (variableDefTypeSimple.types.isNotEmpty())
        variableDefTypeSimple
    else
        null
}

internal fun getPartTypes(part: ObjJUniversalQualifiedReferenceComponent, parentTypes: InferenceResult?, static: Boolean, tag: Tag): InferenceResult? {
    return when (part) {
        is ObjJVariableName -> getVariableNameComponentTypes(part, parentTypes, static, tag)
        is ObjJFunctionCall -> getFunctionComponentTypes(part.functionName, parentTypes, static, tag)
        is ObjJFunctionName -> getFunctionComponentTypes(part, parentTypes, static, tag)
        is ObjJArrayIndexSelector -> getArrayTypes(parentTypes)
        is ObjJMethodCall -> inferMethodCallType(part, tag)
        is ObjJParenEnclosedExpr -> if (part.expr != null) inferExpressionType(part.expr!!, tag) else null
        is ObjJStringLiteral -> return STRING_TYPE_INFERENCE_RESULT
        else -> return null
    }
}

private fun getArrayTypes(parentTypes: InferenceResult?): InferenceResult {
    if (parentTypes == null) {
        return INFERRED_ANY_TYPE
    }
    var types =  parentTypes.types.flatMap {
        (it as? JsTypeListArrayType)?.types.orEmpty()
    }.toSet()
    if (types.isNotNullOrEmpty()) {
        return InferenceResult(types = types)
    }
    types = parentTypes.arrayTypes.types
    if (types.isNotEmpty()) {
        return InferenceResult(types = types)
    }
    return INFERRED_ANY_TYPE
}

private fun getFirstMatchesInGlobals(part: ObjJUniversalQualifiedReferenceComponent, tag: Tag): InferenceResult? {
    val project = part.project
    //ProgressManager.checkCanceled()
    val name = (part as? ObjJVariableName)?.text ?: (part as? ObjJFunctionName)?.text
    ?: (part as? ObjJFunctionCall)?.functionName?.text
    if (name == null && part is ObjJMethodCall) {
        return inferMethodCallType(part, tag)
    } else if (name == null)
        return INFERRED_ANY_TYPE
    val firstMatches: MutableList<JsTypeListType> = mutableListOf()

    val functions = JsTypeDefFunctionsByNameIndex.instance[name, project].map {
        //ProgressIndicatorProvider.checkCanceled()
        it.toJsFunctionType()
    }.toMutableList()
    firstMatches.addAll(functions)
    val properties = JsTypeDefPropertiesByNameIndex.instance[name, project].flatMap {
        //ProgressIndicatorProvider.checkCanceled()
        it.typeListTypes
    }
    firstMatches.addAll(properties)
    if (firstMatches.isEmpty())
        return null
    return InferenceResult(
            types = firstMatches.toSet()
    )
}

fun ObjJVariableName.getAssignmentExprOrNull(): ObjJExpr? {
    return (this.parent as? ObjJGlobalVariableDeclaration)?.expr
            ?: (this.parent.parent as? ObjJVariableDeclaration)?.expr
}

private val STRING_TYPE_INFERENCE_RESULT = InferenceResult(types = setOf("String").toJsTypeList())