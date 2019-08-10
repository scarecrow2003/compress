package com.agoda.factory;

import com.agoda.enums.CompressAlgorithm;
import com.agoda.service.CompressService;
import com.agoda.service.CompressServiceZipImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author zhihua.su
 */

public class CompressServiceFactoryTest {
    @Test
    public void getCompressServiceTest() {
        CompressService compressService = CompressServiceFactory.getCompressService(CompressAlgorithm.ZIP, 9);
        assertEquals(CompressServiceZipImpl.class, compressService.getClass());
    }
}
