package com.agoda.common;

import java.nio.file.Path;

public class PathDetail {
    private Path path;
    private boolean isDir;

    public PathDetail(Path path, boolean isDir) {
        this.path = path;
        this.isDir = isDir;
    }

    public PathDetail(Path path) {
        this.path = path;
        this.isDir = false;
    }

    public Path getPath() {
        return path;
    }

    public boolean isDir() {
        return isDir;
    }
}
