package com.agoda.common;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author zhihua.su
 */

public class LengthOutputStreamTest {
    @Test
    public void writeTest() throws IOException {
        String file = "src/test/fixture/temp/test";
        LengthOutputStream lengthOutputStream = new LengthOutputStream(file);
        lengthOutputStream.write(99);
        assertEquals(1, lengthOutputStream.getLength());
        File temp = new File("src/test/fixture/temp/test");
        temp.delete();
    }
}
