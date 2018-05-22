package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFoldable
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.getNextSiblingOfType
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

import java.util.ArrayList

class ObjJCodeFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val group:FoldingGroup = FoldingGroup.newGroup("objj")
        val foldingDescriptors = ArrayList<FoldingDescriptor>()
        PsiTreeUtil.getChildrenOfTypeAsList(root, ObjJFoldable::class.java).forEach {
            addDescriptor(foldingDescriptors, it.createFoldingDescriptor(group))
            for (child in it.foldableChildren) {
                addDescriptor(foldingDescriptors, it.createFoldingDescriptor(group))
            }
        }
        return foldingDescriptors.toTypedArray()
    }

    private fun addDescriptor(descriptors:MutableList<FoldingDescriptor>, descriptor: FoldingDescriptor?) {
        if (descriptor == null) {
            return
        }
        descriptors.add(descriptor)
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }

    companion object {

        fun execute(declaration: ObjJImplementationDeclaration, group:FoldingGroup): FoldingDescriptor? {
            val startElement = (declaration.inheritedProtocolList ?: declaration.categoryName?: declaration.superClass ?: declaration.getClassName() ?: return null)
            val startOffset:Int = startElement.textRange.endOffset
            val endOffset:Int = declaration.atEnd?.textRange?.endOffset ?: declaration.textRange.endOffset
            val numMethods = declaration.methodDeclarationList.size
            var placeholderText = "..."
            if (numMethods > 0) {
                placeholderText += "$numMethods methods"
            }
            val numInstanceVariables:Int = declaration.instanceVariableList?.instanceVariableDeclarationList?.size ?: 0
            if (numInstanceVariables > 0) {
                placeholderText += "${if (numMethods > 0) " and " else "" }${numInstanceVariables} instance variable"
            }
            if (numInstanceVariables > 0 || numMethods > 0) {
                placeholderText += " hidden"
            }
            return ObjJFoldingDescriptor(declaration, startOffset, endOffset, group, placeholderText)
        }

        fun execute(protocol:ObjJProtocolDeclaration, group:FoldingGroup) : FoldingDescriptor? {
            val startElement = protocol.inheritedProtocolList ?: protocol.getClassName() ?: return null
            val startOffset = startElement.textRange.endOffset
            val endOffset = (protocol.atEnd?.textRange?.endOffset ?: protocol.textRange.endOffset) - 1
            if (startOffset >= endOffset) {
                return null
            }
            val numMethods = protocol.getMethodHeaders().size
            val placeholderString = if (numMethods > 0) "...$numMethods hidden" else null
            return ObjJFoldingDescriptor(protocol, startOffset, endOffset, group, placeholderString)
        }

        fun execute(pragma:ObjJPreprocessorPragma, group:FoldingGroup) : FoldingDescriptor? {
            val startOffset:Int = pragma.textRange.endOffset
            val endOffset:Int = pragma.getNextSiblingOfType(ObjJPreprocessorPragma::class.java)?.textRange?.endOffset ?: pragma.containingFile.textRange.endOffset
            if (startOffset >= endOffset) {
                return null
            }
            return ObjJFoldingDescriptor(pragma, startOffset, endOffset, group)
        }

        fun execute(variableAssignment: ObjJBodyVariableAssignment, group:FoldingGroup) : FoldingDescriptor? {
            val startOffset = (variableAssignment.varModifier?.textRange?.endOffset ?: return null)
            val endOffset = variableAssignment.textRange.endOffset - 1
            if (startOffset >= endOffset) {
                return null
            }
            return ObjJFoldingDescriptor(variableAssignment, startOffset, endOffset, group)
        }

        fun execute(function: ObjJFunctionDeclarationElement<*>, group:FoldingGroup) : FoldingDescriptor? {
            val startOffset = function.functionNameNode?.textRange?.endOffset ?: return null
            val endOffset = (function.block?.textRange?.endOffset ?: return null)
            return ObjJFoldingDescriptor(function, startOffset, endOffset, group)
        }

        fun execute(methodDeclaration:ObjJMethodDeclaration, group:FoldingGroup) : FoldingDescriptor? {
            val startRange = methodDeclaration.methodHeader.textRange.endOffset
            val endRange = (methodDeclaration.block?.textRange?.endOffset ?: return null)
            return ObjJFoldingDescriptor(methodDeclaration, startRange, endRange, group)
        }

        fun execute(instanceVariablesList:ObjJInstanceVariableList, group:FoldingGroup) : FoldingDescriptor? {
            val endOffset: Int = instanceVariablesList.textRange.endOffset - 1
            val startOffset = if (instanceVariablesList.textRange.startOffset + 1 < endOffset)
                instanceVariablesList.textRange.startOffset// + 1
            else
                instanceVariablesList.textRange.startOffset
            return ObjJFoldingDescriptor(instanceVariablesList, startOffset, endOffset, group)
        }

        fun execute(comment:ObjJComment, group:FoldingGroup) : FoldingDescriptor? {
            val startOffset = comment.textRange.startOffset + 2
            val endOffset:Int = comment.textRange.endOffset - 2
            return ObjJFoldingDescriptor(comment, startOffset, endOffset, group)
        }
    }
}

private class ObjJFoldingDescriptor(element: PsiElement, startOffset: Int, endOffset: Int, group:FoldingGroup, placeholderString: String? = defaultPlaceholderText) : FoldingDescriptor(element.node, TextRange(startOffset, endOffset), group) {
    private val placeholderString:String

    override fun getPlaceholderText(): String {
        return placeholderString
    }
    companion object {
        const val defaultPlaceholderText:String = "..."
    }

    init {
        this.placeholderString = when {
            placeholderString == null || placeholderString.isEmpty() -> defaultPlaceholderText
            else -> placeholderString.replace("\n", "\\n").replace("\"", "\\\\\"")
        }
    }
}