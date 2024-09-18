package org.bjtu.compress.liu.decompressor;

import org.apache.commons.math3.fitting.leastsquares.EvaluationRmsChecker;
import org.bjtu.compress.liu.entity.Decimal;
import org.bjtu.compress.liu.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author：lyx
 * @date: 2024/9/14
 */
public abstract class AbstractDecimalDecompressor implements IDecompressor {
    private int leastSignificantPos;

    private int fractionLen;

    protected int blockSize = 1024;

    protected int patchSize = 32;

    protected int dataSize = 0;

    //    @Override
    public List<Double> decompress(List<Integer> bias) {
        dataSize = readInt(20);
        leastSignificantPos = readInt(10);
        leastSignificantPos = (leastSignificantPos >> 1) ^ -(leastSignificantPos & 1);
        fractionLen = leastSignificantPos < 0 ? (int) Math.ceil(-leastSignificantPos / Math.log10(2)) : 0;
        setFractionLen(fractionLen);
//        List<Integer> bias = new ArrayList<>(blockSize);
        List<Double> values = new ArrayList<>(blockSize);

        boolean[] signs = signDecompress(dataSize);

        Double value;

        for (int i = 0; i < dataSize; i++) {
            int ibias = bias.get(i);
            value = nextValue(signs[i], ibias);
            values.add(value);
        }
        return values;
    }


    private Double nextValue(boolean sign, int bias) {
        long v;
        v = xorDecompress(bias);

        return decodeValue(sign, v);
    }

    private Double decodeValue(boolean isNegative, long value) {
        long fractionValue = DataUtils.reverseLowerKBits(value, fractionLen);
        long integerValue = value >>> fractionLen;

        Decimal decimal = new Decimal(isNegative, integerValue, fractionValue, leastSignificantPos);
        Double v = decimal.getDataValue();
        return v;
    }


    protected abstract boolean[] signDecompress(int dataSize);

    protected abstract Long xorDecompress(int bias);

    protected abstract int readBit();

    protected abstract int readInt(int len);

    protected abstract long readLong(int len);

    protected abstract void setFractionLen(int fractionLen);


}
