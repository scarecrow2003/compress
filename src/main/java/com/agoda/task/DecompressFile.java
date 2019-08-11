package com.agoda.task;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author zhihua.su
 */
public class DecompressFile implements Callable<Integer> {
    private static final int BUFFER_SIZE = 4096;

    private final BlockingQueue<Path> files;

    private final Path sourceDir;

    private final String destDir;

    public DecompressFile(BlockingQueue<Path> files, Path sourceDir, String destDir) {
        this.files = files;
        this.sourceDir = sourceDir;
        this.destDir = destDir;
    }

    @Override
    public Integer call() {
        Path file;
        int fileDecompressed = 0;
        try {
            while ((file = files.poll()) != null) {
                int current = 0;
                String zipFileName = this.sourceDir.resolve(file).toString();
                ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileName));
                String fileName = zipFileName.substring(0, zipFileName.lastIndexOf("."));
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    String path = this.destDir + File.separator + entry.getName();
                    File fileOrDir = new File(path);
                    if (entry.isDirectory()) {
                        fileOrDir.mkdirs();
                    } else {
                        doDecompress(zis, fileOrDir);
                    }
                    zis.closeEntry();
                    entry = zis.getNextEntry();
                    if (entry == null) {
                        File nextFile = new File(fileName + String.format(".%03d", ++current));
                        if (nextFile.exists()) {
                            zis.close();
                            zis = new ZipInputStream(new FileInputStream(nextFile));
                            entry = zis.getNextEntry();
                        }
                    }
                }
                zis.close();
                fileDecompressed++;
            }
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
        return fileDecompressed;
    }

    /**
     * Do actual decompression of file. If the output file already exists, it means some part of the file already written
     * by the previous zip file. So we append the content in this zip file into the existing file. If the output file does
     * not exist, we create a new file to write.
     *
     * @param zis ZipInputStream to read from
     * @param file The output File
     */
    private void doDecompress(ZipInputStream zis, File file) throws IOException {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, file.exists()))) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read;
            while ((read = zis.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
            bos.flush();
        }
    }
}
