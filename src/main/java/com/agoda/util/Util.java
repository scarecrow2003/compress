package com.agoda.util;

import java.util.Random;

/**
 * @author zhihua.su
 */
public class Util {
    public static String generateRandomStr(int length) {
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
