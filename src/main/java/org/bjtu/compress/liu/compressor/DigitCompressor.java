package org.bjtu.compress.liu.compressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.bjtu.compress.liu.entity.Decimal;
import org.bjtu.compress.liu.entity.DecimalSeries;
import org.bjtu.compress.liu.utils.Simple16bCompressor;

import java.util.Arrays;

/**
 * @description:
 * @author：lyx
 * @date: 2024/8/18
 */
public class DigitCompressor {

    private DecimalSeries decimalSeries;

    private int blockSize;

    private int patchSize;

    private int patchNum;

    private OutputBitStream out;


    private int size;

    public DigitCompressor(DecimalSeries decimalSeries, int blockSize, int patchSize) {
        this.decimalSeries = decimalSeries;
        this.blockSize = blockSize;
        this.patchSize = patchSize;
        patchNum = (blockSize % patchSize == 0 ? blockSize / patchSize : (blockSize / patchSize + 1));
        out = new OutputBitStream(
                new byte[20000]);
        size = 0;
    }


    public void compress() {
        int[][] digits = decimalSeries.getDigits();
        boolean[] signs = decimalSeries.getSigns();
        int decimalSeriesSize = decimalSeries.getSize();
        if (decimalSeriesSize == 0) {
            return;
        }


        // 待存储数据
        int totalDigitsLen = decimalSeries.getDigitCnt();
        int patchNum = decimalSeriesSize % patchSize == 0 ? decimalSeriesSize / patchSize : (decimalSeriesSize / patchSize + 1);
        int[][] fixDigitLen = new int[totalDigitsLen][patchNum];
        int[][] fixLen = new int[totalDigitsLen][decimalSeriesSize];

        for (int i = 0; i < totalDigitsLen; i++) {
            fixDigitLen[i][0] = fixLen[i][0] = getBitNum(digits[i][0]);
        }

        // 将数据按位进行异或
        for (int i = decimalSeriesSize - 1; i >= 1; i--) {
            for (int j = 0; j < totalDigitsLen; j++) {

                digits[j][i] ^= digits[j][i - 1];
                fixDigitLen[j][i / patchSize] = Math.max(fixDigitLen[j][i / patchSize], getBitNum(digits[j][i]));
                fixLen[j][i] = getBitNum(digits[j][i]);
            }
        }

        // 符号位存储
        for (int i = 0; i < decimalSeriesSize; i += patchSize) {
            int signsOfPatch = 0;
            for (int pIdx = 0; pIdx < patchSize; pIdx++) {
                signsOfPatch = (signsOfPatch << 1) ^ (i + pIdx < decimalSeriesSize && signs[i + pIdx] ? 0x1 : 0x0); // 不够一个patch用0补充
            }
            if (signsOfPatch == 0) {
                out.writeBit(false);
                size++;
            } else {
                out.writeBit(true);
                out.writeInt(signsOfPatch, patchSize);
                size += patchSize + 1;
            }
        }

        // 数据位存储
//        for (int i = 0; i < totalDigitsLen; i++) {
//            for (int j = 0; j < decimalSeriesSize; j += patchSize) {
//                long digitsOfPatch = 0;
//                int pIdx = j / patchSize;
//                if (fixDigitLen[i][pIdx] == 0) {
//                    out.writeBit(false);
//                    size++;
//                } else {
//                    out.writeBit(true);
//                    out.writeInt(fixDigitLen[i][pIdx], 3);
//                    size += 4;
//                    for (int k = 0; k < patchSize; k++) {
//                        digitsOfPatch = (digitsOfPatch << fixDigitLen[i][pIdx]) ^ (j + k < decimalSeriesSize ? digits[i][j + k] : 0x0);
//                    }
//                    out.writeLong(digitsOfPatch, fixDigitLen[i][pIdx] * patchSize);
//                    size += fixDigitLen[i][pIdx] * patchSize;
//                }
//            }
//        }

        for (int i = 0; i < totalDigitsLen; i++) {
            for (int j = 0; j < decimalSeriesSize; j += patchSize) {
                size += Simple16bCompressor.simple16bCompress(digits[i], fixLen[i], j, out);
            }
        }
        System.out.println(size);
    }


    public void close() {
        out.writeInt(15, 4);
        out.flush();
    }

    public OutputBitStream getOut() {
        return out;
    }

    public byte[] getBytes() {
        return out.getBuffer();
    }

    private int getBitNum(int num) {
        return 32 - Integer.numberOfLeadingZeros(num);
    }

    public String getKey() {
        return getClass().getSimpleName();
    }

    public int getSize() {
        return size;
    }

}
