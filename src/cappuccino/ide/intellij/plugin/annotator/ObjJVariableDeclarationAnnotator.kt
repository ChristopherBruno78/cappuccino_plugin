package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJBodyVariableAssignment
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.ObjJQualifiedReference
import cappuccino.ide.intellij.plugin.psi.ObjJVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getNextSiblingOfType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange

object ObjJVariableDeclarationAnnotator {

    /**
     * Provides a unified entry point for this annotation to add clarity to Main Annotator
     */
    internal fun annotateVariableDeclarations(variableDeclaration: ObjJVariableDeclaration, annotationHolder: AnnotationHolder) {

        // Only check if direct parent if body variable assignment.
        // Looking for any inherited parent of caused problems with nested declarations
        val inBodyVariableAssignment:Boolean = (variableDeclaration.parent as? ObjJBodyVariableAssignment)?.varModifier != null

        for (qualifiedReference in variableDeclaration.qualifiedReferenceList) {
            // Check that method call is not being assigned to directly
            // Values can only be assigned to (.) or [array] expressions
            if (qualifiedReference.methodCall != null && qualifiedReference.qualifiedNameParts.isEmpty()) {
                val range = qualifiedReference.getNextSiblingOfType(ObjJTypes.ObjJ_EQUALS)
                        ?: qualifiedReference;
                val errorMessageKey = "objective-j.annotator-messages.variable-declaration-annotator.assign-to-method-call-err.message";
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, ObjJBundle.message(errorMessageKey))
                        .range(range)
                        .create()
                return
            }
            // Check that there is not a qualified reference in a 'var' declaration
            if (inBodyVariableAssignment && qualifiedReference.qualifiedNameParts.size > 1) {
                annotateVariableInBodyAssignment(qualifiedReference, variableDeclaration, annotationHolder)
                return
            }
            // Check that the last part of a qualified name is not a function call
            // as these cannot be assigned to
            val lastChild = qualifiedReference.qualifiedNameParts.lastOrNull() ?: return
            if (lastChild is ObjJFunctionCall) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, ObjJBundle.message("objective-j.annotator-messages.variable-declaration-annotator.assign-to-func-call.message"))
                        .range(TextRange(lastChild.textRange.startOffset, variableDeclaration.textRange.endOffset))
                        .create()
            }
        }
    }

    /**
     * Annotates qualified reference in variable declaration
     * (ie. var hello.world = "こんにちは世界")
     */
    private fun annotateVariableInBodyAssignment(qualifiedReference: ObjJQualifiedReference, variableDeclaration: ObjJVariableDeclaration, annotationHolder: AnnotationHolder) {
        val textRange: TextRange
        val firstDot = qualifiedReference.firstChild.getNextSiblingOfType(ObjJTypes.ObjJ_DOT)
        if (firstDot != null) {
            textRange = firstDot.textRange
        } else {
            val startOffsetTemp = qualifiedReference.firstChild.getNextSiblingOfType(ObjJTypes.ObjJ_DOT)?.textRange?.startOffset
                    ?: qualifiedReference.qualifiedNameParts.getOrNull(1)?.textRange?.startOffset
            val startOffset: Int
            startOffset = if (startOffsetTemp != null) {
                startOffsetTemp - 1
            } else {
                qualifiedReference.textRange.startOffset
            }
            textRange = TextRange.create(startOffset, variableDeclaration.textRange.endOffset)
        }
        val message = ObjJBundle.message(
                "objective-j.annotator-messages.variable-declaration-annotator.qname-in-var-dec.message"
        )
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, message)
                .range(textRange)
                .create()
    }
}