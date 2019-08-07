package com.agoda.service;

import com.agoda.util.Util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Deque;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: zhihua.su
 * @date: 2019-08-06
 * @time: 22:32
 */
public class CompressFile implements Runnable {
    private static final int BUFFER_SIZE = 4096;

    private final BlockingQueue<Path> files;

    private final Path sourceDir;

    private final String destDir;

    private final long maxByte;

    public CompressFile(BlockingQueue<Path> files, Path sourceDir, String destDir, long maxByte) {
        this.files = files;
        this.sourceDir = sourceDir;
        this.destDir = destDir;
        this.maxByte = maxByte;
    }

    @Override
    public void run() {
        String fileName = destDir + File.separator + Util.generateRandomStr(10);
        int current = 0;
        LengthOutputStream fos = null;
        ZipOutputStream zos = null;
        boolean started = false;
        long maxToWrite = maxByte; //todo minus file description
        Path file;
        try {
            while ((file = files.poll()) != null) {
                System.out.println(file.toString());
                if (!started) {
                    fos = new LengthOutputStream(fileName + ".zip");
                    zos = new ZipOutputStream(fos);
                    started = true;
                }
                long offset = 0;
                while ((offset = doCompress(zos, maxToWrite, file, offset)) != 0) {
                    zos.close();
                    fos = new LengthOutputStream(fileName + String.format(".%03d", ++current));
                    zos = new ZipOutputStream(fos);
                    maxToWrite = maxByte;
                }
                maxToWrite = maxByte - fos.getLength();
            }
            if (zos != null) {
                zos.close();
            }
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
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
    private long doCompress(ZipOutputStream zos, long maxToWrite, Path fileToWrite, long offset) {
        try {
            ZipEntry zipEntry = new ZipEntry(fileToWrite.toString());
            zos.putNextEntry(zipEntry);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceDir.resolve(fileToWrite).toString()));
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
            zos.closeEntry();
            bis.close();
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
        return 0;
    }
}
