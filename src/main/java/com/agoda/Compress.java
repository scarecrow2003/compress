package com.agoda;

import com.agoda.enums.ParameterEnum;
import com.agoda.service.CompressService;
import com.agoda.service.CompressServiceZipImpl;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: zhihua.su
 * @date: 2019-08-05
 * @time: 17:26
 */
public class Compress {
    private static final int PARAMETER_COUNTER_COMPRESS = 3;
    private static final int PARAMETER_COUNTER_DECOMPRESS = 2;

    public static void main(String[] args) throws IOException {
        ParameterEnum parameterEnum = validateInput(args);
        if (parameterEnum.equals(ParameterEnum.INVALID)) {
            System.out.println("Invalid number of parameters.");
        } else {
            CompressService compressService = new CompressServiceZipImpl(8);
            if (parameterEnum.equals(ParameterEnum.COMPRESS)) {
                compressService.compress(args[0], args[1], args[2]);
            } else if (parameterEnum.equals(ParameterEnum.DECOMPRESS)) {
                compressService.decompress(args[0], args[1]);
            }
        }
    }

    private static ParameterEnum validateInput(String[] args) {
        if (args.length == PARAMETER_COUNTER_COMPRESS) {
            return ParameterEnum.COMPRESS;
        } else if (args.length == PARAMETER_COUNTER_DECOMPRESS) {
            return ParameterEnum.DECOMPRESS;
        }
        return ParameterEnum.INVALID;
    }
}
