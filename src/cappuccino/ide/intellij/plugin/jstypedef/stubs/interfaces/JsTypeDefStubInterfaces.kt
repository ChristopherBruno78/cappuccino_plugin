package cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces

import cappuccino.ide.intellij.plugin.contributor.JsProperty
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefTypeMapEntry
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunctionProperty
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.*
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.NAMESPACE_SPLITTER_REGEX
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.StubElement

interface JsTypeDefFileStub : PsiFileStub<JsTypeDefFile> {
    val fileName:String
}

/**
 * Keys list stub interface
 */
interface JsTypeDefKeysListStub : StubElement<JsTypeDefKeyListImpl> {
    val fileName:String
    val listName:String
    val values:List<String>
}

/**
 * Function stub interface
 */
interface JsTypeDefFunctionStub : StubElement<JsTypeDefFunctionImpl>, JsTypeDefNamespacedComponent {
    val fileName:String
    val functionName:String
    val parameters:List<JsTypeDefNamedProperty>
    val returnType: InferenceResult
    val global:Boolean
    val static:Boolean
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + functionName
}

fun JsTypeDefProperty.toStubParameter() : JsTypeDefNamedProperty {
    return JsTypeDefNamedProperty(
            name = propertyName.text,
            types = InferenceResult( types = propertyTypes.toJsTypeDefTypeListTypes(), nullable = isNullable)
    )
}

fun JsTypeDefFunctionProperty.toStubParameter() : JsTypeDefNamedProperty {
    return JsTypeDefNamedProperty(
            name = propertyName.text,
            types = InferenceResult( types = propertyTypes.toJsTypeDefTypeListTypes(), nullable = isNullable)
    )
}

/**
 * Property stub interface
 */
interface JsTypeDefPropertyStub : StubElement<JsTypeDefPropertyImpl>, JsTypeDefNamespacedComponent {
    val fileName:String
    val propertyName:String
    val types:InferenceResult
    val nullable:Boolean
    val static:Boolean
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + propertyName
}

/**
 * Stub type for modules
 */
interface JsTypeDefModuleStub : StubElement<JsTypeDefModuleImpl>, JsTypeDefNamespacedComponent {
    val fileName:String
    val moduleName:String
    val fullyNamespacedName:String
        get() = namespaceComponents.joinToString (".")
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + moduleName
}

/**
 * Stub type for module name
 */
interface JsTypeDefModuleNameStub : StubElement<JsTypeDefModuleNameImpl>, JsTypeDefNamespacedComponent {
    val fileName:String
    val moduleName:String
    val fullyNamespacedName:String
        get() = namespaceComponents.joinToString (".")
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + moduleName
}

interface JsTypeDefClassDeclarationStub<PsiT:JsTypeDefClassDeclaration<*>> : StubElement<PsiT>, JsTypeDefNamespacedComponent {
    val fileName:String
    val className:String
    val superTypes:Set<JsTypeListType>
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + className
}


interface JsTypeDefClassStub : StubElement<JsTypeDefClassElementImpl>, JsTypeDefClassDeclarationStub<JsTypeDefClassElementImpl> {
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + className
}

interface JsTypeDefInterfaceStub : StubElement<JsTypeDefInterfaceElementImpl>, JsTypeDefClassDeclarationStub<JsTypeDefInterfaceElementImpl> {
    override val namespaceComponents:List<String>
        get() = enclosingNamespaceComponents + className
}

interface JsTypeDefNamespacedComponent {
    val enclosingNamespace:String
    val enclosingNamespaceComponents:List<String> get() = enclosingNamespace.split(NAMESPACE_SPLITTER_REGEX)
    val namespaceComponents:List<String>
}

val JsTypeDefNamespacedComponent.fullyNamespacedName : String
    get() = namespaceComponents.joinToString(".")


interface JsTypeDefTypeMapStub : StubElement<JsTypeDefTypeMapElementImpl> {
    val fileName:String
    val mapName:String
    val values:List<JsTypeDefTypeMapEntry>
    fun getTypesForKey(key:String) : InferenceResult
}

interface JsTypeDefVariableDeclarationStub : StubElement<JsTypeDefVariableDeclarationImpl>, JsTypeDefNamespacedComponent, JsProperty {
    val fileName:String
    val variableName:String
    val types:InferenceResult
    val static:Boolean
}