package com.agoda.util;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: zhihua.su
 * @date: 2019-08-07
 * @time: 15:53
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
