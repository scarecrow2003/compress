package com.agoda.service;

import com.agoda.common.PathDetail;
import com.agoda.exception.FileAccessException;
import com.agoda.object.CompressArg;
import com.agoda.task.CompressFile;
import com.agoda.task.DecompressFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class CompressServiceZipImpl implements CompressService {
    private static final long BYTES_PER_MEGABYTE = 1024 * 1024;

    private final int level;

    public CompressServiceZipImpl(int level) {
        this.level = level;
    }

    @Override
    public void compress(CompressArg compressArg) throws IOException {
        // 1. First traverse the directory to get all the files and directory
        Path sourceDir = Paths.get(compressArg.getInput());
        DirContent dirContent = new DirContent(sourceDir);
        Files.walkFileTree(sourceDir, dirContent);

        // 2. Then do the compression or decompression
        int cores = Runtime.getRuntime().availableProcessors();
        long start = System.currentTimeMillis();
        if (compressArg.isCompress()) {
            compressFiles(dirContent, cores, compressArg);
        } else {
            decompressFiles(dirContent, cores, compressArg);
        }
        long end = System.currentTimeMillis();
        System.out.println("Compression/Decompression duration: " + (end - start));
    }

    private void compressFiles(DirContent dirContent, int cores, CompressArg compressArg) {
        long maxByte = compressArg.getMaxSize() * BYTES_PER_MEGABYTE;

        // Put all the files and folder into a thread safe queue
        BlockingQueue<PathDetail> queue = new LinkedBlockingQueue<>();
        queue.addAll(dirContent.files.stream().map(PathDetail::new).collect(Collectors.toList()));
        queue.addAll(dirContent.dirs.stream().map(dir -> new PathDetail(dir, true)).collect(Collectors.toList()));

        // Use multiple thread to do parallel compression. The number of thread will be the minimum of cores and number of files to compress
        int threadCounter = Math.min(cores, dirContent.files.size());
        Thread[] threads = new Thread[threadCounter];
        for (int i=0; i<threadCounter; i++) {
            Thread thread = new Thread(new CompressFile(queue, dirContent.getSourceDir(), compressArg.getOutput(), level, maxByte));
            thread.start();
            threads[i] = thread;
        }
        try {
            for (int i = 0; i < threadCounter; i++) {
                threads[i].join();
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }

    private void decompressFiles(DirContent dirContent, int cores, CompressArg compressArg) {
        // Get all the files with extension of "zip".
        BlockingQueue<Path> queue = dirContent.files.stream().filter(file -> {
            String fileName = file.getFileName().toString();
            int index = fileName.lastIndexOf(".");
            return fileName.substring(index).equals(".zip");
        }).collect(Collectors.toCollection(LinkedBlockingQueue::new));

        File destDir = new File(compressArg.getOutput());
        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                throw new FileAccessException("Unable to create directory: " + destDir.toString());
            }
        }

        // Create multiple thread to do decompression. The number of thread equals the number of cores.
        Thread[] threads = new Thread[cores];
        for (int i=0; i<cores; i++) {
            Thread thread = new Thread(new DecompressFile(queue, dirContent.getSourceDir(), compressArg.getOutput()));
            thread.start();
            threads[i] = thread;
        }
        try {
            for (int i = 0; i < cores; i++) {
                threads[i].join();
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }

    }

    class DirContent extends SimpleFileVisitor<Path> {
        private Path sourceDir;

        private List<Path> dirs = new ArrayList<>();

        private List<Path> files = new ArrayList<>();

        public DirContent(Path sourceDir) {
            this.sourceDir = sourceDir;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
            files.add(sourceDir.relativize(file));
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (!dir.equals(sourceDir)) {
                dirs.add(sourceDir.relativize(dir));
            }
            return FileVisitResult.CONTINUE;
        }

        public Path getSourceDir() {
            return sourceDir;
        }
    }
}
