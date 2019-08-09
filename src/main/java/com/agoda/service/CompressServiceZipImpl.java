package com.agoda.service;

import com.agoda.common.DirContent;
import com.agoda.common.PathDetail;
import com.agoda.exception.FileAccessException;
import com.agoda.object.CompressArg;
import com.agoda.task.CompressFile;
import com.agoda.task.DecompressFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author zhihua.su
 */
public class CompressServiceZipImpl implements CompressService {
    private static final long BYTES_PER_MEGABYTE = 1024 * 1024;

    private final int level;

    public CompressServiceZipImpl(int level) {
        this.level = level;
    }

    @Override
    public void compress(CompressArg compressArg) throws IOException {
        long start = System.currentTimeMillis();

        // 1. First traverse the directory to get all the files and directory
        Path sourceDir = Paths.get(compressArg.getInput());
        DirContent dirContent = new DirContent(sourceDir, compressArg.isCompress(), compressArg.getMaxSize() * BYTES_PER_MEGABYTE);
        Files.walkFileTree(sourceDir, dirContent);

        // 2. Then do the compression or decompression
        int cores = Runtime.getRuntime().availableProcessors();
        if (compressArg.isCompress()) {
            compressFiles(dirContent, cores, compressArg);
        } else {
            decompressFiles(dirContent, cores, compressArg);
        }

        long end = System.currentTimeMillis();
        System.out.println("Compression/Decompression duration: " + (end - start));
    }

    private void compressFiles(DirContent dirContent, int cores, CompressArg compressArg) {
        // The zip file may contain some meta data, so we set the maximum byte to write less than our limit
        long maxByte = compressArg.getMaxSize() * BYTES_PER_MEGABYTE - (1024 << 3);

        // Put all the files and folder into a thread safe queue
        BlockingQueue<PathDetail> queue = new LinkedBlockingQueue<>();
        queue.addAll(dirContent.getFiles().stream().map(PathDetail::new).collect(Collectors.toList()));
        queue.addAll(dirContent.getSmallFiles().stream().map(PathDetail::new).collect(Collectors.toList()));
        queue.addAll(dirContent.getDirs().stream().map(dir -> new PathDetail(dir, true)).collect(Collectors.toList()));

        // Use multiple thread to do parallel compression.
        int threadCounter = getThreadCounterForCompression(cores, dirContent.getFiles().size(), (int) ((dirContent.getSmallSize() + maxByte - 1) / maxByte));
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCounter);
        try {
            threadPoolExecutor.invokeAll(IntStream.rangeClosed(1, threadCounter)
                    .mapToObj(i -> new CompressFile(queue, dirContent.getSourceDir(), compressArg.getOutput(), level, maxByte))
                    .collect(Collectors.toList()));
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
        threadPoolExecutor.shutdown();
    }

    private void decompressFiles(DirContent dirContent, int cores, CompressArg compressArg) {
        // Get all the files with extension of "zip".
        BlockingQueue<Path> queue = dirContent.getFiles().stream().filter(file -> {
            String fileName = file.getFileName().toString();
            int index = fileName.lastIndexOf(".");
            return ".zip".equals(fileName.substring(index));
        }).collect(Collectors.toCollection(LinkedBlockingQueue::new));

        File destDir = new File(compressArg.getOutput());
        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                throw new FileAccessException("Unable to create directory: " + destDir.toString());
            }
        }

        // Create multiple thread to do decompression. The number of thread equals the number of cores.
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cores);
        try {
            threadPoolExecutor.invokeAll(IntStream.rangeClosed(1, cores)
                    .mapToObj(i -> new DecompressFile(queue, dirContent.getSourceDir(), compressArg.getOutput()))
                    .collect(Collectors.toList()));
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
        threadPoolExecutor.shutdown();
    }

    private int getThreadCounterForCompression(int cores, int bigFileCounter, int smallFileCounter) {
        // To get the number of thread, we first get how many thread needed for big files and small files. The final result
        // will be the minimum of this counter and the number of cores.
        // Here if we use bigFileCounter only, we end up with less thread and less output files. The small files will be
        // compressed together with the big files. If we use bigFileCounter + smallFileCounter, we creat more thread and
        // more output files. The small files are compressed separately. The compression speed will be faster since we create
        // more thread.
        // The above difference only applies to small number of files. If we have large number of files, the thread number
        // will be limited by number of cores. So it doesn't matter whether we use bigFileCounter or bigFileCounter + smallFileCounter.
        int result = bigFileCounter + smallFileCounter;
        return Math.min(result, cores);
    }
}
