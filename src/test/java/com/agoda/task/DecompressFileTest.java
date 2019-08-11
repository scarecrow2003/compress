package com.agoda.task;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author: zhihua.su
 */

public class DecompressFileTest {
    @Test
    public void doDecompressTest() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        BlockingQueue<Path> queue = new LinkedBlockingQueue<>();
        DecompressFile decompressFile = new DecompressFile(queue, Paths.get("src/test/fixture/output"), "src/test/fixture/decompress");
        Method method = DecompressFile.class.getDeclaredMethod("doDecompress", ZipInputStream.class, File.class);
        method.setAccessible(true);
        ZipInputStream zis = new ZipInputStream(new FileInputStream("src/test/fixture/zip/zip.zip"));
        ZipEntry entry = zis.getNextEntry();
        File file = new File("src/test/fixture/temp" + File.separator + entry.getName());
        method.invoke(decompressFile, zis, file);
        assertTrue(new File("src/test/fixture/temp/folder2/folder4/small9").exists());

        // clean up
        FileUtils.cleanDirectory(new File("src/test/fixture/temp"));
    }
}
