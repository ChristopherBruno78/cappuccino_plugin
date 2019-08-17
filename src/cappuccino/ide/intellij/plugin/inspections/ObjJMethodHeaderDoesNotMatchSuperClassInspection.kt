package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.fixes.ObjJAddSuppressInspectionForScope
import cappuccino.ide.intellij.plugin.fixes.ObjJChangeVarTypeToMatchQuickFix
import cappuccino.ide.intellij.plugin.fixes.ObjJSuppressInspectionScope
import cappuccino.ide.intellij.plugin.fixes.ObjJSuppressInspectionScope.*
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJVisitor
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import java.util.*

class ObjJMethodHeaderDoesNotMatchSuperClassInspection : LocalInspectionTool() {

    override fun getShortName(): String {
        return "MethodHeaderDoesNotMatchSuperClass"
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitMethodHeader(header: ObjJMethodHeader) {
                validateMethodHeader(header, holder)
            }
        }
    }

    companion object {
        private fun validateMethodHeader(header: ObjJMethodHeader, problemsHolder: ProblemsHolder) {

            if (ObjJCommentEvaluatorUtil.isIgnored(header, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE)) {
                return
            }
            val inheritedClasses = ObjJInheritanceUtil.getAllInheritedClasses(header.containingClassName, header.project, true)
            val selectorString = header.selectorString
            val matchingMethodHeaders = ObjJUnifiedMethodIndex.instance[selectorString, header.project]
            if (matchingMethodHeaders.isEmpty()) {
                return
            }
            for (aHeader in matchingMethodHeaders) {
                if (!inheritedClasses.contains(aHeader.containingClassName) || aHeader !is ObjJMethodHeader || aHeader.isEquivalentTo(header)) {
                    continue
                }
                if (!matches(header, aHeader, problemsHolder) && !ObjJMethodPsiUtils.hasSimilarDisposition(header, aHeader)) {
                    problemsHolder.registerProblem(header, ObjJBundle.message("objective-j.inspections.method-header-match.incompatible-method-override.message"),
                            suppressInspectionFix(header, STATEMENT),
                            suppressInspectionFix(header, METHOD),
                            suppressInspectionFix(header, FUNCTION),
                            suppressInspectionFix(header, CLASS),
                            suppressInspectionFix(header, FILE))
                    return
                }
            }
        }

        private fun matches(thisHeader: ObjJMethodHeader, thatHeader: ObjJMethodHeader, problemsHolder: ProblemsHolder): Boolean {
            if (ObjJCommentEvaluatorUtil.isIgnored(thisHeader, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE)) {
                return true
            }
            if (!ObjJMethodPsiUtils.hasSimilarDisposition(thisHeader, thatHeader))
                return true

            var matches = true
            if (thisHeader.explicitReturnType != thatHeader.explicitReturnType && thisHeader.explicitReturnType != "IBAction" || thatHeader.explicitReturnType == "IBAction" ) {
                val methodHeaderReturnTypeElement = thisHeader.methodHeaderReturnTypeElement
                if (methodHeaderReturnTypeElement != null) {
                    val thisHeaderReturnType = methodHeaderReturnTypeElement.formalVariableType
                    matches = if (methodHeaderReturnTypeElement.formalVariableType.varTypeId != null && thatHeader.methodHeaderReturnTypeElement?.formalVariableType?.varTypeId == null) {
                        problemsHolder.registerProblem(thisHeaderReturnType, ObjJBundle.message("objective-j.inspections.method-header-match.return-type-less-specific.message"), ProblemHighlightType.INFORMATION, ObjJChangeVarTypeToMatchQuickFix(thisHeaderReturnType, thatHeader.explicitReturnType),
                                suppressInspectionFix(methodHeaderReturnTypeElement, STATEMENT),
                                suppressInspectionFix(methodHeaderReturnTypeElement, METHOD),
                                suppressInspectionFix(methodHeaderReturnTypeElement, FUNCTION),
                                suppressInspectionFix(methodHeaderReturnTypeElement, CLASS),
                                suppressInspectionFix(methodHeaderReturnTypeElement, FILE))
                        false
                    } else if (ObjJClassType.isSubclassOrSelf(thatHeader.methodHeaderReturnTypeElement?.text
                                    ?: "", thisHeader.methodHeaderReturnTypeElement?.text ?: "", thisHeader.project)) {
                        true
                    } else {
                        registerProblem(problemsHolder, methodHeaderReturnTypeElement, ObjJBundle.message("objective-j.inspections.method-header-match.should-have-return-type.message", thatHeader.explicitReturnType), ObjJChangeVarTypeToMatchQuickFix(thisHeader, thatHeader.explicitReturnType))
                        false
                    }
                } else {
                    matches = false
                }
            }
            val thoseSelectors = thatHeader.methodDeclarationSelectorList

            for ((i, selector) in thisHeader.methodDeclarationSelectorList.withIndex()) {
                val otherParam = thoseSelectors[i].varType ?: continue
                val thisVarType = selector.varType
                if (!Objects.equals(thisVarType?.text, otherParam.text)) {
                    if (thisVarType != null && (thisVarType.text?.toLowerCase() != "void" && otherParam.varTypeId != null)) {
                        problemsHolder.registerProblem(thisVarType, ObjJBundle.message("objective-j.inspections.method-header-match.parent-less-specific.message"), ProblemHighlightType.INFORMATION, ObjJChangeVarTypeToMatchQuickFix(thisVarType, otherParam.text),
                                suppressInspectionFix(thisVarType, STATEMENT),
                                suppressInspectionFix(thisVarType, METHOD),
                                suppressInspectionFix(thisVarType, FUNCTION),
                                suppressInspectionFix(thisVarType, CLASS),
                                suppressInspectionFix(thisVarType, FILE))
                    } else {
                        val errorMessage = ObjJBundle.message("objective-j.inspections.method-header-match.should-have-type.message", otherParam.text)
                        if (thisVarType != null) {
                            registerProblem(problemsHolder, thisVarType, errorMessage, ObjJChangeVarTypeToMatchQuickFix(thisVarType, otherParam.text))
                        } else if (selector.methodHeaderSelectorFormalVariableType?.openParen != null && selector.methodHeaderSelectorFormalVariableType?.closeParen != null) {
                            problemsHolder.registerProblem(selector, errorMessage, ObjJChangeVarTypeToMatchQuickFix(selector, otherParam.text))
                        } else {
                            problemsHolder.registerProblem(selector, errorMessage)
                        }
                        matches = false
                    }
                }
            }
            return matches
        }

        private fun registerProblem(problemsHolder: ProblemsHolder, elementToAnnotate:PsiElement, errorMessage:String, varTypeFix:ObjJChangeVarTypeToMatchQuickFix) {
            problemsHolder.registerProblem(elementToAnnotate, errorMessage, varTypeFix,
                    suppressInspectionFix(elementToAnnotate, STATEMENT),
                    suppressInspectionFix(elementToAnnotate, METHOD),
                    suppressInspectionFix(elementToAnnotate, FUNCTION),
                    suppressInspectionFix(elementToAnnotate, CLASS),
                    suppressInspectionFix(elementToAnnotate, FILE))
        }

        private fun suppressInspectionFix(element:PsiElement, scope:ObjJSuppressInspectionScope) : ObjJAddSuppressInspectionForScope {
            return ObjJAddSuppressInspectionForScope(element, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, scope)
        }

    }

}