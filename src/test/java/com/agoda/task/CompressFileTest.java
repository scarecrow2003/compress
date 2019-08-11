package com.agoda.task;

import com.agoda.common.PathDetail;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipOutputStream;

import static com.agoda.service.CompressServiceZipImpl.BYTES_PER_MEGABYTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author: zhihua.su
 */
public class CompressFileTest {
    @Test
    public void doCompressTest() throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
        BlockingQueue<PathDetail> queue = new LinkedBlockingQueue<>();
        CompressFile compressFile = new CompressFile(queue, Paths.get("src/test/fixture/input"), "src/test/fixture/output", 9, BYTES_PER_MEGABYTE);
        Method method = CompressFile.class.getDeclaredMethod("doCompress", ZipOutputStream.class, long.class, PathDetail.class, long.class);
        method.setAccessible(true);

        long result = (long) method.invoke(compressFile, new ZipOutputStream(new FileOutputStream("src/test/fixture/temp/temp.zip")), BYTES_PER_MEGABYTE, new PathDetail(Paths.get("folder2/small1"), false), 0);
        assertEquals(0, result);

        result = (long) method.invoke(compressFile, new ZipOutputStream(new FileOutputStream("src/test/fixture/temp/temp.zip")), BYTES_PER_MEGABYTE, new PathDetail(Paths.get("folder2/folder3"), true), 0);
        assertEquals(0, result);

        result = (long) method.invoke(compressFile, new ZipOutputStream(new FileOutputStream("src/test/fixture/temp/temp.zip")), BYTES_PER_MEGABYTE, new PathDetail(Paths.get("folder2/large"), false), 0);
        assertTrue(result > 0);

        // clean up
        new File("src/test/fixture/temp/temp.zip").delete();
    }
}
