package org.bjtu.compress.oran.xorcompressor;

import gr.aueb.delorean.chimp.OutputBitStream;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HuffmanXORCompressor {
    private long storedVal = 0;
    private int size;
    private final static long END_SIGN = Double.doubleToLongBits(Double.NaN);

    private final OutputBitStream out;

    public int[] leadingZerosArray = new int[65];
    public int[] trailingZerosArray = new int[65];

    public HuffmanXORCompressor() {
        out = new OutputBitStream(
                new byte[10000]);  // for elf, we need one more bit for each at the worst case
        size = 0;
    }

    public OutputBitStream getOutputStream() {
        return this.out;
    }

    //接受的是经擦除、未异或的浮点序列
    public int addValue(long value) {
        int thisSize = 0;
        long xor = value ^ storedVal;
        int leadingZeros = Long.numberOfLeadingZeros(xor);
        int TrailingZeros = Long.numberOfTrailingZeros(xor);
        if (leadingZeros == 64) {
            leadingZerosArray[64] += 1;
        } else {
            leadingZerosArray[leadingZeros] += 1;
            trailingZerosArray[TrailingZeros] += 1;
            int center = 64 - leadingZeros - TrailingZeros;
            thisSize += center >= 2 ? center - 2 : center;
        }

        size += thisSize;
        storedVal = value;
        return thisSize;
    }

    public int addValue(double value) {
        return addValue(Double.doubleToRawLongBits(value));
    }

    public void close() {
        addValue(END_SIGN);
        out.writeBit(false);
        out.flush();
    }

    public int getSize() {
        return size;
    }

    public byte[] getOut() {
        return out.getBuffer();
    }
}
