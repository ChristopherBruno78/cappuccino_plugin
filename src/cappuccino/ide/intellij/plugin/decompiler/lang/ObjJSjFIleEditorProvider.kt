package cappuccino.ide.intellij.plugin.decompiler.lang;

import cappuccino.decompiler.lang.ObjJSjFileType
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class ObjJSjFIleEditorProvider: com.intellij.openapi.fileEditor.FileEditorProvider {
    override fun getEditorTypeId():String="BEAM"

    override fun accept(project:Project,file:VirtualFile):Boolean=
    file.fileType is ObjJSjFileType ||
    try{
    ScratchFileService.getInstance().scratchesMapping.getMapping(file)is ObjJSjLanguage
    }catch(e:Throwable){
    false
    }

    override fun createEditor(project:Project,file:VirtualFile):ObjJSjFileEditor{
    return ObjJSjFileEditor(file,project)
    }

    override fun getPolicy():FileEditorPolicy=FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
}