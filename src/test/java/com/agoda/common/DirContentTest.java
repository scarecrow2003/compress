package com.agoda.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import static com.agoda.service.CompressServiceZipImpl.BYTES_PER_MEGABYTE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author zhihua.su
 */
public class DirContentTest {

    @Test
    public void visitFileTest() throws IOException {
        Path sourceDir = Paths.get("src/test/fixture/input");
        DirContent dirContent = new DirContent(sourceDir, true, BYTES_PER_MEGABYTE);
        Path filePath = Paths.get("src/test/fixture/input/folder2/small1");
        long expectedSize = filePath.toFile().length();
        dirContent.visitFile(filePath, Files.readAttributes(filePath, BasicFileAttributes.class));
        assertEquals(1, dirContent.getSmallFiles().size());
        assertEquals(expectedSize, dirContent.getSmallSize());
        assertEquals(0, dirContent.getFiles().size());

        Path largeFilePath = Paths.get("src/test/fixture/input/folder2/large");
        dirContent.visitFile(largeFilePath, Files.readAttributes(largeFilePath, BasicFileAttributes.class));
        assertEquals(1, dirContent.getSmallFiles().size());
        assertEquals(expectedSize, dirContent.getSmallSize());
        assertEquals(1, dirContent.getFiles().size());
    }

    @Test
    public void preVisitDirectoryTest() throws IOException {
        Path sourceDir = Paths.get("src/test/fixture/input");
        DirContent dirContent = new DirContent(sourceDir, true, BYTES_PER_MEGABYTE);
        Path dirPath = Paths.get("src/test/fixture/input/folder1");
        dirContent.preVisitDirectory(dirPath, Files.readAttributes(dirPath, BasicFileAttributes.class));
        assertEquals(1, dirContent.getDirs().size());
    }
}
