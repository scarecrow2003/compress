package com.agoda.task;

import com.agoda.common.LengthOutputStream;
import com.agoda.common.PathDetail;
import com.agoda.util.Util;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author zhihua.su
 */
public class CompressFile implements Callable<Integer> {
    private static final int BUFFER_SIZE = 4096;

    private final BlockingQueue<PathDetail> files;

    private final Path sourceDir;

    private final String destDir;

    private final int level;

    private final long maxByte;

    public CompressFile(BlockingQueue<PathDetail> files, Path sourceDir, String destDir, int level, long maxByte) {
        this.files = files;
        this.sourceDir = sourceDir;
        this.destDir = destDir;
        this.level = level;
        this.maxByte = maxByte;
    }

    @Override
    public Integer call() {
        // Use random file name for generated zip file. If the generated file is greater than the size limit, it will be
        // divided into small chunk. The extension of the first chunk is ".zip" and subsequent chunks are "001", "002",
        // "003", etc.
        String fileName = destDir + File.separator + Util.generateRandomStr(10);
        int currentChunk = 0;
        // LengthOutputStream is extended from OutputStream to add the function of recording the length of the output stream
        LengthOutputStream fos;
        ZipOutputStream zos = null;
        long maxToWrite = maxByte;
        PathDetail file;
        int fileCompressed = 0;
        try {
            if ((file = files.poll()) != null) {
                fos = new LengthOutputStream(fileName + ".zip");
                zos = new ZipOutputStream(fos);
                zos.setLevel(level);
                while (file != null) {
                    long offset = 0;
                    // If the returned result of doCompress() is 0, it means the whole file is compressed, so we jump out of
                    // the inner while loop and process the next file. If the returned result is not 0, it means the zip chunk
                    // reached its size limit, so we close the current output stream and create another chunk to write.
                    while ((offset = doCompress(zos, maxToWrite, file, offset)) != 0) {
                        zos.close();
                        fos = new LengthOutputStream(fileName + String.format(".%03d", ++currentChunk));
                        zos = new ZipOutputStream(fos);
                        zos.setLevel(level);
                        maxToWrite = maxByte;
                    }
                    maxToWrite = maxByte - fos.getLength();
                    if (maxToWrite <= 0) {
                        // In some extreme case, when one file is completed compressed, the compressed file reaches its
                        // size limit at the same time. In such case, we create a new zip file to hold the rest of files.
                        zos.close();
                        fileName = destDir + File.separator + Util.generateRandomStr(10);
                        fos = new LengthOutputStream(fileName + ".zip");
                        zos = new ZipOutputStream(fos);
                        zos.setLevel(level);
                        maxToWrite = maxByte;
                    }
                    fileCompressed++;
                    file = files.poll();
                }
            }
            if (zos != null) {
                zos.close();
            }
        } catch (IOException e) {
            System.out.println("Error writing to file");
            e.printStackTrace();
        }
        return fileCompressed;
    }

    /**
     * Do actual compression
     *
     * @param zos ZipOutputStream
     * @param maxToWrite Maximum bytes that can be write into the ZipOutputStream
     * @param fileToWrite File to compress
     * @param offset Starting offset of the file. This is because some part of the file may already written into the previous ZipOutputStream
     * @return Total bytes compressed, 0 if the whole file is compressed
     */
    private long doCompress(ZipOutputStream zos, long maxToWrite, PathDetail fileToWrite, long offset) {
        try {
            // Add tailing "/" for folder
            ZipEntry zipEntry = new ZipEntry(fileToWrite.getPath().toString() + (fileToWrite.isDir() ? "/" : ""));
            zos.putNextEntry(zipEntry);
            if (!fileToWrite.isDir()) {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceDir.resolve(fileToWrite.getPath()).toString()));
                bis.skip(offset);
                long bytesRead = 0;
                byte[] bytesIn = new byte[BUFFER_SIZE];
                int read;
                while ((read = bis.read(bytesIn)) != -1) {
                    zos.write(bytesIn, 0, read);
                    bytesRead += read;
                    if (bytesRead > maxToWrite) {
                        zos.closeEntry();
                        bis.close();
                        return offset + bytesRead;
                    }
                }
                bis.close();
            }
            zos.closeEntry();
        } catch (IOException e) {
            System.out.println("Error reading file");
            e.printStackTrace();
        }
        return 0;
    }
}

