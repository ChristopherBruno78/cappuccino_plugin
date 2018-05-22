package cappuccino.ide.intellij.plugin.utils

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportStatement
import cappuccino.ide.intellij.plugin.psi.utils.getChildrenOfType

import java.util.*
import java.util.regex.Pattern

class ObjJFileUtil {

    /**
     * Gets a list of PSI files for the names of strings given
     * Todo: actually get imported files.
     * @param importedFileNames list of file names specified in import statements
     * @param project project to get files from
     * @return files for import file names
     */
    fun getImportedFiles(importedFileNames: List<String>, project: Project): List<ObjJFile> {
        return EMPTY_FILE_LIST
    }

    fun getImportsAsMap(file: PsiFile): Map<String, List<String>> {
        val out = HashMap<String, List<String>>()
        getImportsAsMap(file, out)
        return out
    }

    private fun getImportsAsMap(file: PsiFile, imports: MutableMap<String, List<String>>) {
        val importStatements = file.getChildrenOfType( ObjJImportStatement::class.java)
        val checked = ArrayList<String>()
        val project = file.project
        val searchScope = GlobalSearchScope.everythingScope(project)
        for (importStatement in importStatements) {
            val framework = importStatement.frameworkName
            val fileName = importStatement.fileName
            if (!addImport(imports, framework, fileName)) {
                continue
            }
            val possibleFiles = FilenameIndex.getFilesByName(project, fileName, searchScope)
            for (possibleImportedFile in possibleFiles) {
                if (framework != null && !framework.isEmpty()) {
                    var directory: PsiDirectory? = possibleImportedFile.containingDirectory
                    while (directory != null) {
                        val directoryName = directory.name
                        directory = if (directoryName == framework) {
                            getImportsAsMap(possibleImportedFile, imports)
                            null
                        } else {
                            directory.parentDirectory
                        }
                    }
                    break
                }
                if (possibleImportedFile.containingDirectory.isEquivalentTo(file.containingDirectory)) {
                    getImportsAsMap(possibleImportedFile, imports)
                    break
                }
            }
        }
    }

    private fun addImport(imports: MutableMap<String, List<String>>,
                          frameworkIn: String?,
                          fileName: String
    ): Boolean {
        val framework = if (frameworkIn != null && frameworkIn.isNotEmpty())  frameworkIn else FILE_PATH_KEY
        val files : MutableList<String>  = imports[framework] as MutableList<String>
        if (files.contains(fileName)) {
            return false
        }
        files.add(fileName)
        imports[framework] = files
        return true
    }

    fun inList(file: ObjJFile, filePaths: List<Pattern>): Boolean {
        val thisPath = file.virtualFile.path
        for (pattern in filePaths) {
            if (pattern.matcher(thisPath).matches()) {
                return true
            }
        }
        return false
    }

    companion object {

        private val EMPTY_FILE_LIST = emptyList<ObjJFile>()
        val FILE_PATH_KEY = "__FILE__"

        fun getContainingFileName(psiElement: PsiElement?): String? {
            return getFileNameSafe(psiElement?.containingFile)
        }

        @JvmOverloads
        fun getFileNameSafe(psiFile: PsiFile?, defaultValue: String? = null, includePath: Boolean = false): String? {
            if (psiFile == null) {
                return defaultValue
            }
            if (psiFile.virtualFile != null) {
                return if (includePath) {
                    psiFile.virtualFile.path
                } else psiFile.virtualFile.name
            }
            val fileName = psiFile.originalFile.name
            return if (!fileName.isEmpty()) fileName else defaultValue
        }

        fun getFilePath(psiFile: PsiFile?, defaultValue: String?): String? {
            if (psiFile == null) {
                return defaultValue
            }
            if (psiFile.virtualFile != null) {
                return psiFile.virtualFile.path
            }
            try {
                return psiFile.originalFile.virtualFile.path
            } catch (ignored: Exception) {
            }

            return defaultValue
        }

        fun isFrameworkElement(psiElement: PsiElement) : Boolean {
            return getFileNameSafe(psiElement.containingFile)?.endsWith("d.j") == true
        }
    }

}
