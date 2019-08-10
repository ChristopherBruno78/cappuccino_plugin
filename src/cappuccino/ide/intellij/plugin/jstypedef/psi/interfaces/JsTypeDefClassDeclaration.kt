package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsClassDefinition
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toJsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toNamedPropertiesList
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefClassDeclarationStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank

interface JsTypeDefClassDeclaration<PsiT:JsTypeDefClassDeclaration<PsiT,StubT>,StubT:JsTypeDefClassDeclarationStub<PsiT>> : JsTypeDefStubBasedElement<StubT>, JsTypeDefElement, JsTypeDefHasNamespace {
    val typeName: JsTypeDefTypeName?
    val genericTypeTypes: JsTypeDefGenericTypeTypes?
    val extendsStatement: JsTypeDefExtendsStatement?
    val interfaceConstructorList: List<JsTypeDefInterfaceConstructor>
    val functionList: List<JsTypeDefFunction>
    val propertyList: List<JsTypeDefProperty>
    val genericsKeys:Set<JsTypeListType.JsTypeListGenericType>
    val isStatic:Boolean
    val isSilent:Boolean
    val isQuiet:Boolean
    val className:String
    val stub:StubT?
}

fun JsTypeDefClassDeclaration<*,*>.toJsClassDefinition() : JsClassDefinition {
    val extends = extendsStatement?.typeList.toJsTypeDefTypeListTypes()
    return JsClassDefinition(
            className = className,
            extends = extends,
            static = isStatic,
            isObjJ = false,
            functions = functionList.filterNot {
                it.isStatic && it.functionNameString.isNotNullOrBlank()
            }.map { it.toJsTypeListType() }.toSet(),
            staticFunctions = functionList.filter {
                it.isStatic && it.functionNameString.isNotNullOrBlank()
            }.map { it.toJsTypeListType() }.toSet(),
            properties = this.propertyList.filter{it.staticKeyword == null && it.propertyNameString.isNotNullOrBlank()}.toNamedPropertiesList().toSet(),
            staticProperties = this.propertyList.filter{it.staticKeyword != null && it.propertyNameString.isNotNullOrBlank()}.toNamedPropertiesList().toSet(),
            isStruct = this is JsTypeDefInterfaceElement,
            isQuiet = isQuiet,
            isSilent = isSilent,
            enclosingNameSpaceComponents = enclosingNamespaceComponents
    )
}