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
                String fileName = this.sourceDir.resolve(file).toString();
                ZipInputStream zis = new ZipInputStream(new FileInputStream(fileName));
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                BufferedOutputStream bos = null;
                boolean keep = false;
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    String path = this.destDir + File.separator + entry.getName();
                    File fileOrDir = new File(path);
                    if (entry.isDirectory()) {
                        fileOrDir.mkdirs();
                        zis.closeEntry();
                        entry = zis.getNextEntry();
                    } else {
                        File parent = fileOrDir.getParentFile();
                        if (!parent.exists()) {
                            parent.mkdirs();
                        }
                        if (!keep) {
                            bos = new BufferedOutputStream(new FileOutputStream(path));
                        }
                        byte[] bytesIn = new byte[BUFFER_SIZE];
                        int read;
                        while ((read = zis.read(bytesIn)) != -1) {
                            bos.write(bytesIn, 0, read);
                        }
                        zis.closeEntry();
                        entry = zis.getNextEntry();
                        if (entry == null) {
                            File nextFile = new File(fileName + String.format(".%03d", ++current));
                            if (nextFile.exists()) {
                                zis.close();
                                zis = new ZipInputStream(new FileInputStream(nextFile));
                                entry = zis.getNextEntry();
                                keep = true;
                            } else {
                                bos.close();
                            }
                        } else {
                            bos.close();
                            keep = false;
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
}
