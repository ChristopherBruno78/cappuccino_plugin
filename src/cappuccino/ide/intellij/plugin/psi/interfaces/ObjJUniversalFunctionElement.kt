package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.hints.ObjJFunctionDescription
import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement

interface ObjJUniversalFunctionElement : ObjJUniversalPsiElement {
    val functionNameString:String?
    val description:ObjJFunctionDescription
}