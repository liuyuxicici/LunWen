package org.bjtu.compress.liu.utils;

import gr.aueb.delorean.chimp.OutputBitStream;

import java.util.ArrayList;

/**
 * @description:
 * @author：lyx
 * @date: 2024/9/12
 */
public class DeltaCompressor {

    private int size;

    private final int patchSize;

    private boolean first;

    private long storedValue;

    private final OutputBitStream out;

    public DeltaCompressor(int patchSize) {
        out = new OutputBitStream(
                new byte[10000]);
        size = 0;
        this.patchSize = patchSize;
        this.first = true;
        storedValue = 0L;
    }

    public int addValue(long value) {
        int thisSize = 0;
        if (first) {
            thisSize += writeFirst(value);
        } else {
            thisSize += compressValue(value);
        }
        return thisSize;
    }

    private int compressValue(long value) {
        return 0;
    }

    private int writeFirst(long value) {
        return 0;
    }
}
