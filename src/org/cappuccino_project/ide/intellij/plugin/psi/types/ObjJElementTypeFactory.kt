package org.cappuccino_project.ide.intellij.plugin.psi.types

import com.intellij.psi.tree.IElementType
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.*
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.ACCESSOR_PROPERTY
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.FUNCTION_DECLARATION
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.FUNCTION_LITERAL
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.GLOBAL_VARIABLE
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.IMPLEMENTATION
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.IMPORT_FILE
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.IMPORT_FRAMEWORK
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.INCLUDE_FILE
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.INCLUDE_FRAMEWORK
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.INSTANCE_VAR
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.METHOD_CALL
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.METHOD_HEADER
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.PREPROCESSOR_FUNCTION
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.PROTOCOL
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.SELECTOR_LITERAL
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.VARIABLE_NAME
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes.VAR_TYPE_ID

interface ObjJElementTypeFactory {
    companion object {
        fun factory(name: String): IElementType {
            when (name) {
                "ObjJ_ACCESSOR_PROPERTY" -> return ACCESSOR_PROPERTY
                "ObjJ_FUNCTION_DECLARATION" -> return FUNCTION_DECLARATION
                "ObjJ_FUNCTION_LITERAL" -> return FUNCTION_LITERAL
                "ObjJ_GLOBAL_VARIABLE_DECLARATION" -> return GLOBAL_VARIABLE
                "ObjJ_IMPLEMENTATION_DECLARATION" -> return IMPLEMENTATION
                "ObjJ_IMPORT_FILE" -> return IMPORT_FILE
                "ObjJ_IMPORT_FRAMEWORK" -> return IMPORT_FRAMEWORK
                "ObjJ_INCLUDE_FILE" -> return INCLUDE_FILE
                "ObjJ_INCLUDE_FRAMEWORK" -> return INCLUDE_FRAMEWORK
                "ObjJ_INSTANCE_VARIABLE_DECLARATION" -> return INSTANCE_VAR
                "ObjJ_METHOD_CALL" -> return METHOD_CALL
                "ObjJ_METHOD_HEADER" -> return METHOD_HEADER
                "ObjJ_PREPROCESSOR_DEFINE_FUNCTION" -> return PREPROCESSOR_FUNCTION
                "ObjJ_PROTOCOL_DECLARATION" -> return PROTOCOL
                "ObjJ_SELECTOR_LITERAL" -> return SELECTOR_LITERAL
                "ObjJ_VAR_TYPE_ID" -> return VAR_TYPE_ID
                "ObjJ_VARIABLE_NAME" -> return VARIABLE_NAME
                else -> throw RuntimeException("Failed to find element type in factory for type <$name>")
            }
        }
    }
}
