package org.urbcomp.startdb.compress.elf.xordecompressor;

import gr.aueb.delorean.chimp.InputBitStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OranMpLowXorDecomp {
    private long storedVal = 0;
    private int storedLeadingZeros = Integer.MAX_VALUE;
    private int storedTrailingZeros = Integer.MAX_VALUE;
    private boolean first = true;
    private boolean endOfStream = false;

    private final InputBitStream in;

    private final static long END_SIGN = Double.doubleToLongBits(Double.NaN);

    private final static short[] leadingRepresentation = {0, 8, 12, 16, 18, 20, 22, 24};

    public OranMpLowXorDecomp(byte[] bs) {
        in = new InputBitStream(bs);
    }

    public List<Double> getValues() {
        List<Double> list = new ArrayList<>(1024);
        Double value = readValue();
        while (value != null) {
            list.add(value);
            value = readValue();
        }
        return list;
    }

    public InputBitStream getInputStream() {
        return in;
    }

    /**
     * Returns the next pair in the time series, if available.
     *
     * @return Pair if there's next value, null if series is done.
     */
    public Double readValue() {
        try {
            next();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        if (endOfStream) {
            return null;
        }
        return Double.longBitsToDouble(storedVal);
    }

    private void next() throws IOException {
        if (first) {
            first = false;
            int trailingZeros = in.readInt(7);
            if (trailingZeros < 64) {
                storedVal = ((in.readLong(63 - trailingZeros) << 1) + 1) << trailingZeros;
            } else {
                storedVal = 0;
            }
            if (storedVal == END_SIGN) {
                endOfStream = true;
            }
        } else {
            nextValue();
        }
    }

    private void nextValue() throws IOException {
        long value;
        int centerBits, leadAndCenter;
        int flag = in.readInt(2);
        switch (flag) {
            case 3:
                // case 11
                leadAndCenter = in.readInt(9);
                storedLeadingZeros = leadingRepresentation[leadAndCenter >>> 6];
                centerBits = leadAndCenter & 0x3f;
                if (centerBits == 0) {
                    centerBits = 64;
                }
                storedTrailingZeros = 64 - storedLeadingZeros - centerBits;
                value = ((in.readLong(centerBits - 1) << 1) + 1) << storedTrailingZeros;
                value = storedVal ^ value;
                if (value == END_SIGN) {
                    endOfStream = true;
                } else {
                    storedVal = value;
                }
                break;
            case 2:
                // case 10
                leadAndCenter = in.readInt(6); //7 改 6:  leading-3 center-3
                storedLeadingZeros = leadingRepresentation[leadAndCenter >>> 3]; //4 改 3: 取出前导零
                centerBits = leadAndCenter & 0x7; //0xf 改 0x7 四位改三位
                if (centerBits == 0) {
                    centerBits = 8;
                }
                storedTrailingZeros = 64 - storedLeadingZeros - centerBits;
                value = ((in.readLong(centerBits - 1) << 1) + 1) << storedTrailingZeros; //意思是之前省去了一位
                value = storedVal ^ value; //拿到了这个解压的数值
                if (value == END_SIGN) {
                    endOfStream = true;
                } else {
                    storedVal = value;
                }
                break;
            case 1:
                // case 01, we do nothing, the same value as before
                break;
            default:
                // case 00
                centerBits = 64 - storedLeadingZeros - storedTrailingZeros;
                value = in.readLong(centerBits) << storedTrailingZeros;
                value = storedVal ^ value;
                if (value == END_SIGN) {
                    endOfStream = true;
                } else {
                    storedVal = value;
                }
                break;
        }
    }
}
