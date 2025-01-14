package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.*
import com.intellij.codeInspection.ex.ProblemDescriptorImpl
import com.intellij.lang.ASTNode
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.sun.xml.bind.v2.model.annotation.Quick
import org.jetbrains.annotations.Contract
import java.lang.reflect.TypeVariable

typealias AnnotationBuilderImpl = com.intellij.lang.annotation.AnnotationBuilder

class AnnotationHolderWrapper(private val annotationHolder: AnnotationHolder) {

    @Contract(pure = true)
    fun newAnnotation(severity:HighlightSeverity, message: String) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, severity, message)
    }

    @Contract(pure = true)
    fun newErrorAnnotation(message:String) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, HighlightSeverity.ERROR, message)
    }

    @Contract(pure = true)
    fun newWarningAnnotation(message:String) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, HighlightSeverity.WARNING, message)
    }

    @Contract(pure = true)
    fun newWeakWarningAnnotation(message:String) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, HighlightSeverity.WEAK_WARNING, message)
    }

    @Contract(pure = true)
    fun newInfoAnnotation(message:String?) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, HighlightSeverity.INFORMATION, message)
    }
}

internal data class AnnotationBuilderData(
        internal val message:String?,
        internal val severity: HighlightSeverity,
        internal val range: TextRange? = null,
        internal val fixBuilderData:List<FixBuilderData> = listOf(),
        internal val fixes:List<IntentionAction> = listOf(),
        internal val enforcedTextAttributes: TextAttributes? = null,
        internal val textAttributes: TextAttributesKey? = null,
        internal val needsUpdateOnTyping: Boolean? = null,
        internal val highlightType:ProblemHighlightType? = null,
        internal val tooltip:String? = null
)

class AnnotationBuilder private constructor(internal val annotationHolder: AnnotationHolder, internal val data:AnnotationBuilderData) {

    constructor(annotationHolder:AnnotationHolder, severity:HighlightSeverity, message: String?)
            : this(annotationHolder, AnnotationBuilderData(severity = severity, message = message))

    @Contract(pure = true)
    fun range(range:TextRange) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, data.copy(range = range))
    }

    @Contract(pure = true)
    fun range(element: PsiElement) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, data.copy(range = element.textRange))
    }

    fun range(node: ASTNode) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, data.copy (range = node.textRange))
    }

    @Contract(pure = true)
    fun withFix(fix:IntentionAction) : AnnotationBuilder  {
        return AnnotationBuilder(annotationHolder, data.copy(fixes = data.fixes + fix))
    }

    @Contract(pure = true)
    private fun withFix(fix:FixBuilderData) : AnnotationBuilder  {
        return AnnotationBuilder(annotationHolder, data.copy(fixBuilderData = data.fixBuilderData + fix))
    }

    @Contract(pure = true)
    fun withFixes(fixes:List<IntentionAction>) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, data.copy(fixes = data.fixes + fixes))
    }

    @Contract(pure = true)
    fun needsUpdateOnTyping(needsUpdateOnTyping:Boolean) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, data.copy(needsUpdateOnTyping = needsUpdateOnTyping))
    }

    @Contract(pure = true)
    fun needsUpdateOnTyping() : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, data.copy(needsUpdateOnTyping = true))
    }

    @Contract(pure = true)
    fun highlightType(highlightType:ProblemHighlightType) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, data.copy(highlightType = highlightType))
    }

    @Contract(pure = true)
    fun tooltip(tooltip:String) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, data.copy(tooltip = tooltip))
    }

    @Contract(pure = true)
    fun textAttributes(textAttributes: TextAttributesKey) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, data.copy(textAttributes = textAttributes))
    }

    @Contract(pure = true)
    fun enforcedTextAttributes(textAttributes:TextAttributes) : AnnotationBuilder {
        return AnnotationBuilder(annotationHolder, data.copy(enforcedTextAttributes = textAttributes))
    }

    @Contract(pure = true)
    fun newFix(intentionAction: IntentionAction): FixBuilder {
        return FixBuilder._createFixBuilder(this, FixBuilderData(intentionAction = intentionAction))
    }

    @Contract(pure = true)
    fun newLocalQuickFix(quickFix: LocalQuickFix, problemDescriptor: ProblemDescriptor): FixBuilder {
        return FixBuilder._createFixBuilder(this, FixBuilderData(quickFix = quickFix, problemDescriptor = problemDescriptor))
    }

    @Contract(pure = true)
    fun create() {
        val range = data.range
                ?: throw Exception("Cannot create annotation without range")
        var annotation: AnnotationBuilderImpl = data.message?.let { message ->
            annotationHolder
                .newAnnotation(data.severity, message)
        } ?: annotationHolder.newSilentAnnotation(data.severity)
        annotation = annotation.range(range)
        data.fixBuilderData.forEach {val intentionAction = it.intentionAction ?: it.quickFix as? IntentionAction
            val quickFix = it.quickFix ?: it.intentionAction as? LocalQuickFix
            val union:FixUnion? = if (quickFix != null && intentionAction != null)
                FixUnion(intentionAction = intentionAction, quickFix = quickFix)
            else
                null
            if (it.batch.orFalse()) {
                annotation = annotation.registerBatchFix(union!!, it.range, it.key)
            }
            if (it.universal.orFalse()) {
                annotation = annotation.registerBatchFix(union!!, it.range, it.key)
            }
            annotation = if (!it.universal.orFalse() && it.batch.orFalse()) {
                if (quickFix != null) {
                    annotation.registerFix(quickFix, it.range, it.key, it.problemDescriptor!!)
                } else if (intentionAction != null){
                    if (it.range != null) {
                        if (it.key != null) {
                            annotation.registerFix(intentionAction, it.range, it.key)
                        } else {
                            annotation.registerFix(intentionAction, it.range)
                        }
                    } else {
                        annotation.registerFix(intentionAction, null)
                    }
                } else {
                    throw Exception("Cannot create fix without any fixes")
                }
            } else {
                annotation
            }
        }
        data.fixes.forEach {
            annotation = annotation.registerFix(it)
        }

        data.tooltip?.let {
            annotation = annotation.tooltip(it)
        }
        data.enforcedTextAttributes?.let {
            annotation = annotation.enforcedTextAttributes(it)
        }
        data.textAttributes?.let {
            annotation = annotation.textAttributes(it)
        }
        data.needsUpdateOnTyping?.let {
            annotation = annotation.needsUpdateOnTyping(it)
        }
        data.highlightType?.let {
            annotation = annotation.highlightType(it)
        }
        annotation.create()
    }



    class FixBuilder private  constructor(private val annotationBuilder: AnnotationBuilder, private val fixBuilderData: FixBuilderData) {

        @Contract(pure = true)
        fun range(range: TextRange): FixBuilder {
            return FixBuilder(annotationBuilder, fixBuilderData.copy(range = range))
        }

        @Contract(pure = true)
        fun key(key: HighlightDisplayKey): FixBuilder {
            return FixBuilder(annotationBuilder, fixBuilderData.copy(key = key))
        }

        @Contract(pure = true)
        fun batch(): FixBuilder {
            return FixBuilder(annotationBuilder, fixBuilderData.copy(batch = true))
        }

        @Contract(pure = true)
        fun registerFix(): AnnotationBuilder {
            return annotationBuilder.withFix(fixBuilderData)
        }

        companion object {
            @Suppress("FunctionName")
            internal fun _createFixBuilder(annotationBuilder: AnnotationBuilder, fixBuilderData: FixBuilderData) : FixBuilder {
                return FixBuilder(annotationBuilder, fixBuilderData)
            }
        }
    }
}

internal data class FixBuilderData(
        internal val quickFix: LocalQuickFix? = null,
        internal val intentionAction: IntentionAction? = null,
        internal val range: TextRange? = null,
        internal val key: HighlightDisplayKey? = null,
        internal val universal:Boolean? = null,
        internal val batch:Boolean? = null,
        internal val problemDescriptor: ProblemDescriptor? = null

)

class FixUnion(val quickFix:LocalQuickFix, val intentionAction: IntentionAction) : IntentionAction by intentionAction, LocalQuickFix by quickFix {

    override fun getText(): String = intentionAction.text

    override fun getFamilyName(): String = quickFix.familyName

    override fun startInWriteAction(): Boolean = intentionAction.startInWriteAction()

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = intentionAction.isAvailable(project, editor, file)

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) = quickFix.applyFix(project, descriptor)

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) = intentionAction.invoke(project, editor, file)


}

private fun AnnotationBuilderImpl.registerBatchFix(
    fix: FixUnion,
    range: TextRange? = null,
    key: HighlightDisplayKey? = null,
): AnnotationBuilderImpl {
    var fixBuilder = newFix(fix)
    if (key != null) {
        fixBuilder = fixBuilder.key(key)
    }
    if (range != null) {
        fixBuilder = fixBuilder.range(range)
    }
    return fixBuilder.registerFix()
}

private fun AnnotationBuilderImpl.registerFix(
    fix: IntentionAction,
    range: TextRange? = null,
    key: HighlightDisplayKey? = null,
): AnnotationBuilderImpl {
    var fixBuilder = newFix(fix)
    if (key != null) {
        fixBuilder = fixBuilder.key(key)
    }
    if (range != null) {
        fixBuilder = fixBuilder.range(range)
    }
    return fixBuilder.registerFix()
}

private fun AnnotationBuilderImpl.registerFix(
    fix: LocalQuickFix,
    range: TextRange?,
    key: HighlightDisplayKey?,
    problemDescriptor: ProblemDescriptor
): AnnotationBuilderImpl {
    var fixBuilder = newLocalQuickFix(
        fix,
        problemDescriptor
    )
    if (key != null) {
        fixBuilder = fixBuilder.key(key)
    }
    if (range != null) {
        fixBuilder = fixBuilder.range(range)
    }
    return fixBuilder.registerFix()
}