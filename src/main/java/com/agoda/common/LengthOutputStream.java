package com.agoda.common;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author zhihua.su
 */
public class LengthOutputStream extends FileOutputStream {
    private long length = 0L;

    public LengthOutputStream(String name) throws FileNotFoundException {
        super(name);
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        length++;
    }

    public long getLength() {
        return length;
    }
}
