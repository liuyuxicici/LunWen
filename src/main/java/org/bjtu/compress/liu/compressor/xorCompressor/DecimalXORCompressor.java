package org.bjtu.compress.liu.compressor.xorCompressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.bjtu.compress.liu.compressor.DataCompressorSupport;
import org.bjtu.compress.liu.utils.DataUtils;

/**
 * @description:
 * @author：lyx
 * @date: 2024/9/14
 */
public class DecimalXORCompressor {

    private boolean first = true;

    private final static long END_SIGN = Double.doubleToLongBits(Double.NaN);

    private long storedVal = Integer.MAX_VALUE;

    private int size = 0;

    private OutputBitStream out;

    private int fractionLen = 0;


    private final double LOG2_10 = 1.0 / Math.log10(2);

    private int storedTrailingZeros = Integer.MAX_VALUE;

    private int storedTrailingZeroBits = 0;

    private int storedTrailingZerosCount = 0;


    private int penalty = 0;

    public DecimalXORCompressor() {
        out = new OutputBitStream(
                new byte[100000]);  // for elf, we need one more bit for each at the worst case

    }


    public int addValue(long value, int bias) {

        int nbias = 0;

        if (first) {
            nbias = writeFirst(value, bias);
        } else {
            nbias = compressValue(value, bias);
        }
        return nbias;
    }


    /**
     * 写入第一个数据
     *
     * @param value
     * @param bias
     * @return
     */
    private int writeFirst(long value, int bias) {
        first = false;
        storedVal = value;
        int trailingZeros = Long.numberOfTrailingZeros(value);

        int validLen = bias + fractionLen;

        out.writeLong(value, validLen);
        size += validLen;
        return bias;
    }

    /**
     * 压缩数据
     *
     * @param value
     * @param bias
     * @return
     */
    private int compressValue(long value, int bias) {
        int validLen = bias + fractionLen;
        long xor = value ^ storedVal; // 与前一位数据的后bias+validLen位异或


        if (xor == 0) { // case 10: 异或结果为0
            out.writeInt(2, 2);
            size += 2;

            return 0;
        } else {

            int nbias = 64 - Long.numberOfLeadingZeros(xor) - fractionLen;
            int trailingZeros = Long.numberOfTrailingZeros(xor);


            // case 0: 尾部0的数据比前一个存储的数据更大或相同， 存储有效位
            if (trailingZeros >= storedTrailingZeros
                    && penalty >= -storedTrailingZerosCount * 0.0
            ) {
                storedTrailingZerosCount++;

                out.writeInt(0, 1);
                size += 1;
                if (trailingZeros == storedTrailingZeros) {
                    penalty++;
                } else {
                    penalty--;
                }

            } else { // case 11 : 存储尾部零比前一个数据更小的数据
                storedTrailingZerosCount = 0;
                penalty = 0;


                out.writeInt(3, 2);
//                System.out.println("11");
                size += 2;
//                thisSize += 2;
                storedTrailingZeroBits = (int) Math.ceil(Math.log10(nbias + fractionLen) * LOG2_10);
                out.writeInt(trailingZeros, storedTrailingZeroBits);
                size += storedTrailingZeroBits;
                storedTrailingZeros = trailingZeros;
            }

            int centerBits = nbias + fractionLen - storedTrailingZeros - 1;


//            if (centerBits > 0) {
            out.writeLong(xor >>> (storedTrailingZeros), centerBits);
            size += centerBits;
//            }


            storedVal = value;
            return nbias;
        }
    }

    public void close() {
        out.writeLong(0L, 64);
        out.writeBit(false);
        out.flush();
    }

    public int getSize() {
        return size;
    }

    public OutputBitStream getOut() {
        return out;
    }

    public void setFractionLen(int fractionLen) {

        this.fractionLen = fractionLen;

    }
}
