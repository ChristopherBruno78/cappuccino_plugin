package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.indices.ObjJClassInheritanceIndex
import com.intellij.openapi.project.Project


fun getAllObjJAndJsClassObjects(project: Project): List<GlobalJSClass> {
    return globalJSClasses + AllObjJClassesAsJsClasses(project)
}

fun getJsClassObject(project:Project, objjClasses:List<GlobalJSClass>, className: String) : GlobalJSClass? {
    return _getJsClassObject(objjClasses, className)?.mergeWithSuperClasses(project)
}


fun GlobalJSClass.flattenWithChildrenProperties(project:Project, objjClasses:List<GlobalJSClass>, className: String) : GlobalJSClass? {
    val baseClass = _getJsClassObject(objjClasses, className)
    val jsClass = if (baseClass != null)
        baseClass + globalJSClasses.filter { className in this.extends }.flatten(className)
    else
        globalJSClasses.filter { className in this.extends }.flatten(className)
    val objjClass = objJClassAsJsClass(project, className)
    val allClasses = ObjJClassInheritanceIndex.instance[className, project].mapNotNull {
        objJClassAsJsClass(project, it.getClassNameString())
    }.toMutableList()
    allClasses.add(jsClass)
    if (objjClass != null)
        allClasses.add(objjClass)
    return allClasses.flatten(className)
}

private fun _getJsClassObject(objjClasses:List<GlobalJSClass>, className: String) : GlobalJSClass? {
    val out = (globalJSClasses.filter { it.className == className } + objjClasses.filter { it.className == className })
    if (out.isEmpty())
        return null
    return out.flatten(className)
}

fun Iterable<GlobalJSClass>.flatten(className:String) : GlobalJSClass {
    val properties = mutableListOf<JsNamedProperty>()
    val staticProperties= mutableListOf<JsNamedProperty>()
    val functions = mutableListOf<GlobalJSClassFunction>()
    val staticFunctions = mutableListOf<GlobalJSClassFunction>()
    val extendsTemp = mutableListOf<String>()
    for (jsClass in this) {
        properties.addAll(jsClass.properties)
        staticProperties.addAll(jsClass.staticProperties)
        functions.addAll(jsClass.functions)
        staticFunctions.addAll(jsClass.staticFunctions)
        extendsTemp.addAll(jsClass.extends)
    }
    val extends = extendsTemp.flattenNestedSuperClasses()
    return GlobalJSClass(
            className = className,
            properties = properties,
            staticProperties = staticProperties,
            functions = functions,
            extends = extends.toList(),
            staticFunctions = staticFunctions
    )
}

fun GlobalJSClass.mergeWithSuperClasses(project:Project) : GlobalJSClass {
    val objjClasses = AllObjJClassesAsJsClasses(project)
    val superClassNames = extends.flattenNestedSuperClasses()
    val superClasses = superClassNames.mapNotNull { _getJsClassObject(objjClasses, it) }
    return (superClasses + this).flatten(className)
}

fun List<String>.flattenNestedSuperClasses() : Set<String> {
    val superClasses = mutableListOf<String>()
    for (jsClass in this) {
        if (superClasses.contains(jsClass))
            continue
        superClasses.add(jsClass)
        addNestedSuperClasses(jsClass, superClasses)
    }
    return superClasses.toSet()
}

private fun addNestedSuperClasses(className:String, superClasses:MutableList<String>) {
    val referenced = globalJSClasses.firstOrNull{
        it.className == className
    } ?: return
    for(superClass in referenced.extends) {
        if (superClasses.contains(superClass))
            continue
        superClasses.add(superClass)
        addNestedSuperClasses(superClass, superClasses)
    }
    return

}


operator fun GlobalJSClass.plus(otherClass:GlobalJSClass) : GlobalJSClass {
    val properties = this.properties + otherClass.properties
    val staticProperties= this.staticProperties + otherClass.staticProperties
    val functions = this.functions + otherClass.functions
    val staticFunctions = this.staticFunctions + otherClass.staticFunctions
    val extendsTemp = this.extends + otherClass.extends
    val extends = extendsTemp.flattenNestedSuperClasses()
    return this.copy(
            properties = properties,
            staticProperties = staticProperties,
            functions = functions,
            extends = extends.toList(),
            staticFunctions = staticFunctions
    )
}