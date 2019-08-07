package com.agoda.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: zhihua.su
 * @date: 2019-08-05
 * @time: 20:15
 */
public class CompressServiceZipImpl implements CompressService {
    private static final long BYTES_PER_MEGABYTE = 1024 * 1024;

    private final int level;

    public CompressServiceZipImpl(int level) {
        this.level = level;
    }

    @Override
    public void compress(String input, String output, String maxSize) throws IOException {
        Path sourceDir = Paths.get(input);
        DirContent dirContent = new DirContent(sourceDir);
        Files.walkFileTree(sourceDir, dirContent);
        int cores = Runtime.getRuntime().availableProcessors();
//        cores = 1;
        System.out.println("cores: " + cores);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cores);
//        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destDir));
        for (Path dir: dirContent.dirs) {
            executor.submit(() -> {
                System.out.println(Thread.currentThread().getName() + ":" + Thread.currentThread().getId());
                try {
                    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output + File.separator + Thread.currentThread().getName() + ".zip"));
                    zos.putNextEntry(new ZipEntry(dir.toString()+"/"));
                    zos.closeEntry();
                    zos.close();
                } catch (IOException e) {
                    System.out.println("Error reading file: " + dir.toString());
                }
            });
        }
        for (Path file: dirContent.files) {

        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
//        zos.close();
        long maxByte = Integer.parseInt(maxSize) * BYTES_PER_MEGABYTE;
        BlockingQueue<Path> queue = new LinkedBlockingQueue<>(dirContent.files); // todo syn
        Thread[] threads = new Thread[cores];
        long start = System.currentTimeMillis();
        for (int i=0; i<cores; i++) {
            Thread thread = new Thread(new CompressFile(queue, sourceDir, output, maxByte));
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
        long end = System.currentTimeMillis();
        System.out.println("Compression duration: " + (end - start));
    }

    @Override
    public void decompress(String input, String output) throws IOException {
        Path sourceDir = Paths.get(input);
        DirContent dirContent = new DirContent(sourceDir);
        Files.walkFileTree(sourceDir, Collections.emptySet(), 1, dirContent);
        BlockingQueue<Path> queue = dirContent.files.stream().filter(file -> {
            String fileName = file.getFileName().toString();
            int index = fileName.lastIndexOf(".");
            return fileName.substring(index).equals(".zip");
        }).collect(Collectors.toCollection(LinkedBlockingQueue::new));
        File destDir = new File(output);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        int cores = Runtime.getRuntime().availableProcessors();
//        cores = 1;
        Thread[] threads = new Thread[cores];
        long start = System.currentTimeMillis();
        for (int i=0; i<cores; i++) {
            Thread thread = new Thread(new DecompressFile(queue, sourceDir, output));
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
        long end = System.currentTimeMillis();
        System.out.println("Decompression duration: " + (end - start));
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
    }
}
