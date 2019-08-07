package com.agoda.enums;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: zhihua.su
 * @date: 2019-08-05
 * @time: 20:18
 */
public enum  ParameterEnum {
    INVALID(0),
    COMPRESS(1),
    DECOMPRESS(2);

    private final int value;

    ParameterEnum(int value) {
        this.value = value;
    }
}
