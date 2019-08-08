package com.agoda;

import com.agoda.enums.CompressAlgorithm;
import com.agoda.factory.CompressServiceFactory;
import com.agoda.object.CompressArg;
import com.agoda.service.CompressService;

import java.io.IOException;

/**
 * Since the requirement requires not use third party library to do the compression, the program will not use Spring or
 * any other framework.
 */
public class Compress {

    public static void main(String[] args) throws IOException {
        CompressArg compressArg = validateInput(args);
        // we may get the compress algorithm from command line in future if we implement other algorithm
        CompressAlgorithm compressAlgorithm = CompressAlgorithm.ZIP;
        // we may get the compress level in future if we want to change the compress level
        int compressLevel = 9;
        CompressService compressService = CompressServiceFactory.getCompressService(compressAlgorithm, compressLevel);
        compressService.compress(compressArg);
    }

    private static CompressArg validateInput(String[] args) {
        if (args.length != 2 && args.length != 3) {
            throw new IllegalArgumentException("2 or 3 args required. 2 for decompression. 3 for compression.");
        }
        CompressArg compressArg = new CompressArg();
        compressArg.setInput(args[0]);
        compressArg.setOutput(args[1]);
        if (args.length == 3) {
            compressArg.setCompress(true);
            int maxSize = Integer.parseInt(args[2]); // throws NumberFormatException if invalid integer
            compressArg.setMaxSize(maxSize);
        }
        return compressArg;
    }
}