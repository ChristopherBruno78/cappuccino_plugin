package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndexKey

@Suppress("MemberVisibilityCanBePrivate")
class ObjJVariableNameByScopeIndex private constructor() : ObjJStringStubIndexBase<ObjJVariableName>() {

    override val indexedElementClass: Class<ObjJVariableName>
        get() = ObjJVariableName::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJVariableName> {
        return KEY
    }

    fun getInRangeStrict(file: PsiFile, textRange: TextRange, project: Project): List<ObjJVariableName> {
        val fileScope = GlobalSearchScope.fileScope(file)
        return getAllKeys(project)
                .mapNotNull { it.split("-").getOrNull(1) }
                .flatMap {
                    getInRangeStrict(file, it, textRange, fileScope, project)
                }
    }

    fun getInRangeStrict(file: PsiFile, variableName: String, textRange: TextRange, project: Project): List<ObjJVariableName> {
        val fileScope = GlobalSearchScope.fileScope(file)
        return getInRangeStrict(file, variableName, textRange, fileScope, project)
    }

    fun getInRangeStrict(file: PsiFile, variableName: String, textRange: TextRange, searchScope: GlobalSearchScope, project: Project): List<ObjJVariableName> {
        return getInRangeStrict(file, variableName, textRange.startOffset, textRange.endOffset, searchScope, project)
    }

    fun getInRangeStrict(file: PsiFile, variableName: String, elementStartOffset: Int, elementEndOffset: Int, scope: GlobalSearchScope, project: Project): List<ObjJVariableName> {
        val queryKey = ObjJAssignedVariableNamesByBlockIndex.getIndexKey(variableName, elementStartOffset, elementEndOffset)
        return get(queryKey, project, scopeOrDefault(scope, project))
    }

    fun getInRangeFuzzy(file: PsiFile, variableName: String, textRange: TextRange, project: Project): List<ObjJVariableName> {
        val fileScope = GlobalSearchScope.fileScope(file)
        return getInRangeFuzzy(variableName, textRange, fileScope, project)
    }

    fun getInRangeFuzzy(file: PsiFile, textRange: TextRange, project: Project): List<ObjJVariableName> {
        return getInRangeFuzzy(file, textRange.startOffset, textRange.endOffset, project)
    }

    fun getInRangeFuzzy(file: PsiFile, startOffset: Int, endOffset: Int, project: Project): List<ObjJVariableName> {
        val fileScope = GlobalSearchScope.fileScope(file)
        return getAllKeys(project).flatMap {
            getInRangeFuzzy(it, startOffset, endOffset, fileScope, project)
        }
    }

    /*

    fun getInRangeFuzzy(file:PsiFile, textRange: TextRange, project: Project): List<ObjJVariableName> {
        return getInRangeFuzzy(file, textRange.startOffset, textRange.endOffset, project)
    }

    fun getInRangeFuzzy(file:PsiFile, startOffset: Int, endOffset:Int, project: Project): List<ObjJVariableName> {
        val fileScope = GlobalSearchScope.fileScope(file)
        return getAll(project, fileScope).filter {
            val variableTextRange = it.textRange
            variableTextRange.startOffset >= startOffset && variableTextRange.endOffset <= endOffset
        }
    }
     */

    fun getInRangeFuzzy(variableName: String, textRange: TextRange, searchScope: GlobalSearchScope, project: Project): List<ObjJVariableName> {
        return getInRangeFuzzy(variableName, textRange.startOffset, textRange.endOffset, searchScope, project)
    }

    fun getInRangeFuzzy(variableName: String, elementStartOffset: Int, elementEndOffset: Int, scope: GlobalSearchScope, project: Project): List<ObjJVariableName> {
        val keys = filterKeys(variableName, elementStartOffset, elementEndOffset, project)
        val out = mutableListOf<ObjJVariableName>()
        for (key in keys) {
            out.addAll(get(key, project, scopeOrDefault(scope, project)))
        }
        return out
    }

    fun filterKeys(variableName: String, elementStartOffset: Int, elementEndOffset: Int, project: Project): List<String> {
        return getAllKeys(project).filter {
            val rangeVariableNameSplit = it.split("-")
            if (rangeVariableNameSplit.getOrNull(1) != variableName)
                return@filter false
            val range = rangeVariableNameSplit[0].split(":")
            val blockStart = range.getOrNull(0)?.toInt()
                    ?: return@filter false
            if (elementStartOffset < blockStart)
                return@filter false

            val blockEnd = range.getOrNull(1)?.toInt()
                    ?: return@filter false
            elementEndOffset <= blockEnd
        }
    }

    companion object {
        val instance = ObjJVariableNameByScopeIndex()
        val KEY = IndexKeyUtil.createIndexKey(ObjJVariableNameByScopeIndex::class.java)
        private const val KEY_FORMAT = "%s:%s-%s"
        private const val VERSION = 4
        fun getIndexKey(fileName: String, blockRange: Pair<Int, Int>): String {
            return getIndexKey(fileName, blockRange.getFirst(), blockRange.getSecond())
        }

        fun getIndexKey(fileName: String?, startOffset: Int, endOffset: Int): String {
            return getIndexKey(fileName, startOffset.toString() + "", endOffset.toString() + "")
        }

        private fun getIndexKey(fileName: String?, startOffset: String, endOffset: String): String {
            return String.format(KEY_FORMAT, fileName, startOffset, endOffset)
        }
    }
}
