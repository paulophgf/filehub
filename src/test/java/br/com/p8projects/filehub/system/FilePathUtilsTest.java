package br.com.p8projects.filehub.system;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FilePathUtilsTest {

    @Test
    void getNewPathDirectoryRenameRoot() {
        String path = "test/";
        String newName = "hello";
        String newPath = FilePathUtils.getNewPathDirectoryRename(path, newName);
        assertEquals("hello/", newPath);
    }

    @Test
    void getNewPathDirectoryRenameSubdir() {
        String path = "test/sub1";
        String newName = "hello";
        String newPath = FilePathUtils.getNewPathDirectoryRename(path, newName);
        assertEquals("test/hello/", newPath);
    }

    @Test
    void getNewPathDirectoryRenameSubdirEndsWithSlash() {
        String path = "test/sub1/";
        String newName = "hello";
        String newPath = FilePathUtils.getNewPathDirectoryRename(path, newName);
        assertEquals("test/hello/", newPath);
    }

}