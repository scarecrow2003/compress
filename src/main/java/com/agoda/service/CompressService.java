package com.agoda.service;

import com.agoda.object.CompressArg;

import java.io.IOException;

/**
 * @author zhihua.su
 */
public interface CompressService {
    /**
     * To compress or decompress directory
     *
     * @param compressArg compress arguments
     */
    void compress(CompressArg compressArg) throws IOException;
}
