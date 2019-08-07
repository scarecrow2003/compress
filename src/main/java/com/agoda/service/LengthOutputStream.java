package com.agoda.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: zhihua.su
 * @date: 2019-08-07
 * @time: 11:11
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
