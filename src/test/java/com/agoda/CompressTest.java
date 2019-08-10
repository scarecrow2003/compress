package com.agoda;

import com.agoda.object.CompressArg;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author zhihua.su
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CompressTest {

    private Method method;

    @BeforeAll
    public void setup() throws NoSuchMethodException {
        method = Compress.class.getDeclaredMethod("validateInput", String[].class);
        method.setAccessible(true);
    }

    @Test
    public void validateInputCompress() throws InvocationTargetException, IllegalAccessException {
        String expectedInput = "/temp/input";
        String expectedOutput = "/temp/output";
        String expectedSize = "2";
        String[] validCompressArg = {expectedInput, expectedOutput, expectedSize};
        CompressArg compressArg = (CompressArg) method.invoke(Compress.class, (Object) validCompressArg);
        assertEquals(compressArg.getInput(), expectedInput);
        assertEquals(compressArg.getOutput(), expectedOutput);
        assertTrue(compressArg.isCompress());
        assertEquals(compressArg.getMaxSize(), 2);
    }

    @Test
    public void validateInputDecompress() throws InvocationTargetException, IllegalAccessException {
        String expectedInput = "/temp/input";
        String expectedOutput = "/temp/output";
        String[] validCompressArg = {expectedInput, expectedOutput};
        CompressArg compressArg = (CompressArg) method.invoke(Compress.class, (Object) validCompressArg);
        assertEquals(compressArg.getInput(), expectedInput);
        assertEquals(compressArg.getOutput(), expectedOutput);
        assertFalse(compressArg.isCompress());
    }

    @Test
    public void validateInputInvalid() {
        String[] zeroLength = {};
        assertThrows(InvocationTargetException.class, () -> method.invoke(Compress.class, (Object) zeroLength));

        String[] oneLength = new String[]{"/temp/input"};
        assertThrows(InvocationTargetException.class, () -> method.invoke(Compress.class, (Object) oneLength));

        String[] fourLength = new String[]{"/temp/input", "/temp/output", "2", "2"};
        assertThrows(InvocationTargetException.class, () -> method.invoke(Compress.class, (Object) fourLength));

        String[] invalid = new String[]{"/temp/input", "/temp/output", "invalid number"};
        assertThrows(InvocationTargetException.class, () -> method.invoke(Compress.class, (Object) invalid));
    }
}
