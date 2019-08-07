package com.agoda.object;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: zhihua.su
 * @date: 2019-08-05
 * @time: 22:24
 */
public class CompressPara {
    private String input;

    private String output;

    private String algorithm;

    private int level;

    private int maxSize;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
