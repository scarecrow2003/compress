package com.agoda.service;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: zhihua.su
 * @date: 2019-08-05
 * @time: 22:13
 */
public class CompressServiceFactory {
    public static final String ZIP = "zip";

    public static CompressService getCompressService(String compressAlgorithm, int level) {
        if (ZIP.equals(compressAlgorithm)) {
            return new CompressServiceZipImpl(level);
        }
        // todo other compress algorithm
        return null;
    }
}
