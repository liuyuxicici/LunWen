package org.bjtu.compress.liu.decompressor.xorDecompressor;

import gr.aueb.delorean.chimp.InputBitStream;

import java.io.IOException;

/**
 * @description:
 * @author：lyx
 * @date: 2024/9/15
 */
public class DecimalXORDecompressor {

    private long storedVal = 0;

    private int storedTrailingZeros = Integer.MAX_VALUE;

    private int storedTrailingZeroBits = 0;

    private boolean first = true;
    private boolean endOfStream = false;

    private final InputBitStream in;

    private final double LOG2_10 = 1.0 / Math.log10(2);

    private final static long END_SIGN = 0;

    private int fractionLen = 0;

    public DecimalXORDecompressor(byte[] bytes) {
        in = new InputBitStream(bytes);
    }

    public InputBitStream getInputStream() {
        return in;
    }

    public Long readValue(int bias) {
        try {
            next(bias);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        if (endOfStream) {
            return null;
        }
        return storedVal;
    }

    private void next(int bias) throws IOException {
        if (first) {
            first = false;

            int validLen = bias + fractionLen;

            storedVal = in.readLong(validLen);

//            if (storedVal == END_SIGN) {
//                endOfStream = true;
//            }
        } else {
            nextValue(bias);
        }
    }

    private void nextValue(int bias) throws IOException {
        long xor = 0;


        switch (in.readBit()) {
            case 1:
                if (in.readBit() == 1) {  // case 11;
                    storedTrailingZeroBits = (int) Math.ceil(Math.log10(bias + fractionLen) * LOG2_10);
                    storedTrailingZeros = in.readInt(storedTrailingZeroBits);
                } else {  // case 10
                    break;
                }

            case 0:   // case 0
                int centerBits = bias + fractionLen - storedTrailingZeros - 1;
                xor = ((1L << centerBits) ^ in.readLong(centerBits)) << storedTrailingZeros;
                storedVal ^= xor;
                break;
        }
    }

    public void setFractionLen(int fractionLen) {
        this.fractionLen = fractionLen;
    }
}
