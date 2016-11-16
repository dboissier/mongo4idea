/*
 * Copyright (c) 2016 David Boissier.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.mongo.view.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MongoFileSystem extends VirtualFileSystem implements ApplicationComponent {

    private static final String PROTOCOL = "mongo";

    public static MongoFileSystem getInstance() {
        return ApplicationManager.getApplication().getComponent(MongoFileSystem.class);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "MongoPlugin.MongoFileSystem";
    }

    @NotNull
    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    public void openEditor(final MongoObjectFile mongoObjectFile) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(mongoObjectFile.getProject());
        fileEditorManager.openFile(mongoObjectFile, true);
    }

//    Unused methods

    @Nullable
    @Override
    public VirtualFile findFileByPath(@NotNull @NonNls String path) {
        return null;
    }

    @Override
    public void refresh(boolean asynchronous) {

    }

    @Nullable
    @Override
    public VirtualFile refreshAndFindFileByPath(@NotNull String path) {
        return null;
    }

    @Override
    public void addVirtualFileListener(@NotNull VirtualFileListener listener) {

    }

    @Override
    public void removeVirtualFileListener(@NotNull VirtualFileListener listener) {

    }

    @Override
    protected void deleteFile(Object requestor, @NotNull VirtualFile vFile) throws IOException {

    }

    @Override
    protected void moveFile(Object requestor, @NotNull VirtualFile vFile, @NotNull VirtualFile newParent) throws IOException {

    }

    @Override
    protected void renameFile(Object requestor, @NotNull VirtualFile vFile, @NotNull String newName) throws IOException {

    }

    @NotNull
    @Override
    protected VirtualFile createChildFile(Object requestor, @NotNull VirtualFile vDir, @NotNull String fileName) throws IOException {
        return null;
    }

    @NotNull
    @Override
    protected VirtualFile createChildDirectory(Object requestor, @NotNull VirtualFile vDir, @NotNull String dirName) throws IOException {
        throw new UnsupportedOperationException("No file management in this plugin");
    }

    @NotNull
    @Override
    protected VirtualFile copyFile(Object requestor, @NotNull VirtualFile virtualFile, @NotNull VirtualFile newParent, @NotNull String copyName) throws IOException {
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }
}
