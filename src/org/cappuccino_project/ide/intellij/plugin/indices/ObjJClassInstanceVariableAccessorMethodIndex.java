package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration;
import org.cappuccino_project.ide.intellij.plugin.stubs.ObjJStubVersions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObjJClassInstanceVariableAccessorMethodIndex extends ObjJStringStubIndexBase<ObjJInstanceVariableDeclaration> {

    private static final ObjJClassInstanceVariableAccessorMethodIndex INSTANCE = new ObjJClassInstanceVariableAccessorMethodIndex();
    private static final StubIndexKey<String, ObjJInstanceVariableDeclaration> KEY = IndexKeyUtil.createIndexKey(ObjJClassInstanceVariableAccessorMethodIndex.class);
    private static final int VERSION = 1;

    public static ObjJClassInstanceVariableAccessorMethodIndex getInstance() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJInstanceVariableDeclaration> getKey() {
        return KEY;
    }

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJInstanceVariableDeclaration> getIndexedElementClass() {
        return ObjJInstanceVariableDeclaration.class;
    }
}
