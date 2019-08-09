package com.agoda.common;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhihua.su
 */
public class DirContent extends SimpleFileVisitor<Path> {
    private Path sourceDir;

    private boolean isCompress;

    private long sizeThreshold;

    private long smallSize = 0;

    private List<Path> dirs = new ArrayList<>();

    private List<Path> files = new ArrayList<>();

    private List<Path> smallFiles = new ArrayList<>();

    public DirContent(Path sourceDir, boolean isCompress, long sizeThreshold) {
        this.sourceDir = sourceDir;
        this.isCompress = isCompress;
        this.sizeThreshold = sizeThreshold;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
        // we put big and small files in different list so we can optimize the number of thread to compress the files
        // and optimize the number of output files. However this is at the cost of spend additional time to access
        // the files to get their size. But since time constraints is not a requirement for this problem, we can leave
        // it as it is now.
        long fileSize = file.toFile().length();
        if (isCompress &&  fileSize < sizeThreshold) {
            smallFiles.add(sourceDir.relativize(file));
            smallSize += fileSize;
        } else {
            files.add(sourceDir.relativize(file));
        }
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

    public List<Path> getDirs() {
        return dirs;
    }

    public List<Path> getFiles() {
        return files;
    }

    public List<Path> getSmallFiles() {
        return smallFiles;
    }

    public long getSmallSize() {
        return smallSize;
    }
}