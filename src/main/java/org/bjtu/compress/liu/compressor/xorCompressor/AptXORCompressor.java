package org.bjtu.compress.liu.compressor.xorCompressor;

import gr.aueb.delorean.chimp.OutputBitStream;

import java.util.List;

public class AptXORCompressor {
    private long storedVal = 0;
    private boolean first = true;
    private int size;

    private List<Integer> storedCenterBits;

    private final static long END_SIGN = Double.doubleToLongBits(Double.NaN);

    private int storedLeadingZeros = Integer.MAX_VALUE;

//    private int storedTrailingZeros = Integer.MAX_VALUE;

    private List<Integer> storedTrailingZeros;
    public final static short[] leadingRepresentation = {0, 0, 0, 0, 0, 0, 0, 0,
            1, 1, 1, 1, 2, 2, 2, 2,
            3, 3, 4, 4, 5, 5, 6, 6,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7
    };

    public final static short[] leadingRound = {0, 0, 0, 0, 0, 0, 0, 0,
            8, 8, 8, 8, 12, 12, 12, 12,
            16, 16, 18, 18, 20, 20, 22, 22,
            24, 24, 24, 24, 28, 28, 28, 28,
            32, 32, 32, 32, 36, 36, 36, 36,
            40, 40, 40, 40, 44, 44, 44, 44,
            48, 48, 48, 48, 52, 52, 52, 52,
            56, 56, 56, 56, 60, 60, 60, 60
    };


    private final OutputBitStream out;

    public AptXORCompressor(OutputBitStream out, List<Integer> centerBits, List<Integer> trailingZeros) {
        this.out = out;
        size = 0;
        this.storedCenterBits = centerBits;
        this.storedTrailingZeros = trailingZeros;
    }
//    public AptXORCompressor(OutputBitStream out) {
//        this.out = out;
//        size = 0;
//    }

    public OutputBitStream getOutputStream() {
        return this.out;
    }

    /**
     * Adds a new long value to the series. Note, values must be inserted in order.
     *
     * @param value next floating point value in the series
     */
    public int addValue(long value) {
        if (first) {
            return writeFirst(value);
        } else {
            return compressValue(value);
        }
    }

    /**
     * Adds a new double value to the series. Note, values must be inserted in order.
     *
     * @param value next floating point value in the series
     */
    public int addValue(double value) {
        if (first) {
            return writeFirst(Double.doubleToRawLongBits(value));
        } else {
            return compressValue(Double.doubleToRawLongBits(value));
        }
    }

    private int writeFirst(long value) {
        first = false;
        storedVal = value;
        int trailingZeros = Long.numberOfTrailingZeros(value);
        out.writeInt(trailingZeros, 7);
        if (trailingZeros < 64) {
            out.writeLong(storedVal >>> (trailingZeros + 1), 63 - trailingZeros);
            size += 70 - trailingZeros;
            return 70 - trailingZeros;
        } else {
            size += 7;
            return 7;
        }
    }

    /**
     * Closes the block and writes the remaining stuff to the BitOutput.
     */
    public void close() {
        addValue(END_SIGN);
        out.writeBit(false);
        out.flush();
    }

    private int compressValue(long value) {
        int thisSize = 0;
        long xor = value;

        if (xor == 0) { // case 0: 异或结果为0
            out.writeInt(0, 1);
            thisSize += 1;

        } else {  // case 1: 异或结果不为0
            int trailingZeros = Long.numberOfTrailingZeros(xor);
            int centerBits = 64 - trailingZeros - Long.numberOfLeadingZeros(xor);

            out.writeInt(1, 1);
            thisSize += 1;

            if (centerBits >= 2) {
                out.writeLong(xor >>> (trailingZeros + 1), centerBits - 2);
                thisSize += centerBits - 2;
            }
            storedTrailingZeros.add(trailingZeros);
            storedCenterBits.add(centerBits);

        }
        size += thisSize;
        return thisSize;

    }

    public int getSize() {
        return size;
    }

    public byte[] getOut() {
        return out.getBuffer();
    }
}