package com.agoda.manager;

import com.agoda.object.CompressPara;
import com.agoda.service.CompressServiceFactory;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: zhihua.su
 * @date: 2019-08-05
 * @time: 22:12
 */
public class CompressManager {

    public void init(CompressPara compressPara) {
        CompressServiceFactory.getCompressService(compressPara.getAlgorithm(), compressPara.getLevel());
    }

    public void doCompression() {

    }

    public void doDecompression() {

    }
}
