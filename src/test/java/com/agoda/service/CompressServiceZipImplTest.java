package com.agoda.service;

import com.agoda.object.CompressArg;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.agoda.service.CompressServiceZipImpl.BYTES_PER_MEGABYTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author zhihua.su
 */

public class CompressServiceZipImplTest {
    @Test
    public void compressTest() throws IOException {
        int sizeLimit = 1;
        CompressArg compressArg = new CompressArg();
        compressArg.setInput("src/test/fixture/input");
        compressArg.setOutput("src/test/fixture/output");
        compressArg.setCompress(true);
        compressArg.setMaxSize(1);
        CompressService compressService = new CompressServiceZipImpl(9);
        compressService.compress(compressArg);

        Collection<File> compressedFiles = FileUtils.listFiles(Paths.get("src/test/fixture/output").toFile(), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        for (File compressedFile: compressedFiles) {
            assertTrue(compressedFile.length() <= sizeLimit * BYTES_PER_MEGABYTE);
        }

        CompressArg decompressArg = new CompressArg();
        decompressArg.setInput("src/test/fixture/output");
        decompressArg.setOutput("src/test/fixture/decompress");
        decompressArg.setCompress(false);
        compressService.compress(decompressArg);

        Path originalFolder = Paths.get("src/test/fixture/input");
        Collection<String> original = FileUtils.listFilesAndDirs(originalFolder.toFile(), TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY)
                .stream()
                .map(file -> originalFolder.relativize(file.toPath()).toString())
                .collect(Collectors.toSet());
        Path generatedFolder = Paths.get("src/test/fixture/decompress");
        Collection<String> generated = FileUtils.listFilesAndDirs(generatedFolder.toFile(), TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY)
                .stream()
                .map(file -> generatedFolder.relativize(file.toPath()).toString())
                .collect(Collectors.toSet());
        assertEquals(original.size(), generated.size());
        for (String file: original) {
            assertTrue(generated.contains(file));
            long originalLength = originalFolder.resolve(Paths.get(file)).toFile().length();
            long generatedLength = generatedFolder.resolve(Paths.get(file)).toFile().length();
            assertEquals(originalLength, generatedLength);
        }

        // clean up
        FileUtils.cleanDirectory(new File("src/test/fixture/output"));
        FileUtils.deleteDirectory(new File("src/test/fixture/decompress"));
    }
}
