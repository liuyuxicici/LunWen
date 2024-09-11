package org.bjtu.compress.liu.compressor;

import gr.aueb.delorean.chimp.OutputBitStream;

/**
 * @description: 数据处理
 * @author：lyx
 * @date: 2024/9/9
 */
public class DataCompressorSupport {

    protected int size = 0;

    protected OutputBitStream out;


    protected int blockSize;
    protected int patchSize;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public OutputBitStream getOut() {
        return out;
    }

    public void setOut(OutputBitStream out) {
        this.out = out;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public int getPatchSize() {
        return patchSize;
    }

    public void setPatchSize(int patchSize) {
        this.patchSize = patchSize;
    }

    public byte[] getBytes() {
        return out.getBuffer();
    }
}
