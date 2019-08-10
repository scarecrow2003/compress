package com.agoda.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author zhihua.su
 */
public class UtilTest {

    @Test
    public void generateRandomStrSuccess() {
        int expectedLength = 8;
        String result = Util.generateRandomStr(expectedLength);
        assertEquals(expectedLength, result.length());
    }

    @Test
    public void generateRandomStrFail() {
        int zeorLength = 0;
        assertThrows(IllegalArgumentException.class, () -> Util.generateRandomStr(zeorLength));

        int negativeLength = -10;
        assertThrows(IllegalArgumentException.class, () -> Util.generateRandomStr(negativeLength));

        int largeLength = 999;
        assertThrows(IllegalArgumentException.class, () -> Util.generateRandomStr(largeLength));
    }
}
