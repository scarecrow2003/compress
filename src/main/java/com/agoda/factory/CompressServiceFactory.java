package com.agoda.factory;

import com.agoda.enums.CompressAlgorithm;
import com.agoda.exception.UnsupportedAlgorithm;
import com.agoda.service.CompressService;
import com.agoda.service.CompressServiceZipImpl;

public class CompressServiceFactory {
    public static CompressService getCompressService(CompressAlgorithm compressAlgorithm, int level) {
        if (CompressAlgorithm.ZIP.equals(compressAlgorithm)) {
            return new CompressServiceZipImpl(level);
        }
        // todo other compress algorithm
        throw new UnsupportedAlgorithm("Unsupported algorithm: " + compressAlgorithm.name());
    }
}
