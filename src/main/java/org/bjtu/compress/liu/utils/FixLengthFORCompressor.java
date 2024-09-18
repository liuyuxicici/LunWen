package org.bjtu.compress.liu.utils;

import gr.aueb.delorean.chimp.OutputBitStream;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: FOR + fixLength编码
 * @author：lyx
 * @date: 2024/9/11
 */
public class FixLengthFORCompressor {

    private int size;

    private final int patchSize;

    private long[] patchValues;

    private long patchMaxValue;

    private List<Long> patchMaxValues;

    private int currIndexOfPatch;

    private int[] dict;


    private final OutputBitStream out;

    public FixLengthFORCompressor(int patchSize) {
        out = new OutputBitStream(
                new byte[10000]);
        size = 0;
        this.patchSize = patchSize;
        patchValues = new long[patchSize];
        patchMaxValue = 0;
        currIndexOfPatch = 0;
        patchMaxValues = new ArrayList<>(500);

        dict = new int[1000];
    }

    public int addValue(long value) {
//        dict[(int) value]++;

        // add value in patch
        writeValue(value);
        int thisSize = 0;
        if (currIndexOfPatch == patchSize) {
            // compress value in patch
            thisSize += compressValues();
            size += thisSize;
        }
        return thisSize;
    }

    private int compressValues() {
        int thisSize = 0;
        int fixLen = Integer.MAX_VALUE;
        long[] patchDelta = new long[currIndexOfPatch];
        for (int i = 0; i < currIndexOfPatch; i++) {
            long delta = patchMaxValue - patchValues[i];
            fixLen = Math.min(fixLen, Long.numberOfLeadingZeros(delta));
        }

        fixLen = 64 - fixLen;
        thisSize += 4;
        for (long delta : patchDelta) {
            out.writeLong(delta, fixLen);
            thisSize += fixLen;
        }

        patchMaxValues.add(patchMaxValue);
        patchMaxValue = 0;
        currIndexOfPatch = 0;
        return thisSize;
    }

    private void writeValue(long value) {
        patchValues[currIndexOfPatch] = value;
        patchMaxValue = Math.max(patchMaxValue, value);
        currIndexOfPatch++;
    }

    public int compressMaxValue() {
        if (patchMaxValues.size() == 0) {
            return 0;
        }

        int thisSize = 0;
        long preValue = 0;
        int fixLen = 0;
        for (int i = 1; i < patchMaxValues.size(); i++) {
            long tmp = patchMaxValues.get(i);
            patchMaxValues.set(i, tmp - preValue);
            fixLen = Math.max(fixLen, DataUtils.getBitNum(tmp - preValue));
            preValue = tmp;
        }

        thisSize += fixLen * patchMaxValues.size();
        return thisSize;
    }

    public int getSize() {
        return size;
    }

    public int[] getDict() {
        return dict;
    }

    public List<Long> getPatchMaxValues() {
        return patchMaxValues;
    }
}
