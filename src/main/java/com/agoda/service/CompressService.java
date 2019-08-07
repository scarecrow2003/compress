package com.agoda.service;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: zhihua.su
 * @date: 2019-08-05
 * @time: 20:15
 */
public interface CompressService {
    /**
     * To compress directory
     *
     * @param input path to input directory
     * @param output path to output directory
     * @param maxSize maximum compressed size per file in MB
     */
    void compress(String input, String output, String maxSize) throws IOException;

    /**
     * To decompress
     *
     * @param input path to input directory
     * @param output path to output directory
     */
    void decompress(String input, String output) throws IOException;
}
