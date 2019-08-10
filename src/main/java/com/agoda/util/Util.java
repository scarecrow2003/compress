package com.agoda.util;

import java.util.Random;

/**
 * @author zhihua.su
 */
public class Util {
    /**
     * Generate random string of length. The individual letter in the string is between 'a' and 'z'.
     * @param length Length between 1 (inclusive) and 100 (inclusive)
     * @return A random string of length
     */
    public static String generateRandomStr(int length) {
        if (length < 1 || length > 100) {
            throw new IllegalArgumentException("Invalid length");
        }
        int start = 'a';
        int end = 'z';
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i=0; i<length; i++) {
            int one = start + (int) (random.nextFloat() * (end - start + 1));
            sb.append((char) one);
        }
        return sb.toString();
    }
}
