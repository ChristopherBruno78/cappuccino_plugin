package cappuccino.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.PsiFileStubImpl
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFileStub

import java.util.ArrayList
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.MatchResult
import java.util.regex.Pattern

class ObjJFileStubImpl(objJFile: ObjJFile?, override val fileName: String) : PsiFileStubImpl<ObjJFile>(objJFile), ObjJFileStub {


/*
    override fun getImportsForFramework(framework: String): List<String> {
        val out = ArrayList<String>()
        var matchResult: MatchResult
        var importFramework: String?
        for (importString in imports) {
            matchResult = IMPORT_FILENAME_REGEX.matcher(importString)
            if (matchResult.groupCount() < 3) {
                LOGGER.log(Level.WARNING, "File import for name is invalid when filtering imports by framework")
                continue
            }
            importFramework = matchResult.group(1)
            if (importFramework != null && importFramework == framework) {
                out.add(matchResult.group(2))
            }
        }
        return out
    }
*/
    companion object {

        private val LOGGER = Logger.getLogger("ObjJFileStubImpl")
    }
}