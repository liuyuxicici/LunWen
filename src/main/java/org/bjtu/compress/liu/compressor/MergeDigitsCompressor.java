package org.bjtu.compress.liu.compressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.bjtu.compress.liu.entity.DecimalSeries;

/**
 * @description:
 * @author：lyx
 * @date: 2024/8/27
 */
public class MergeDigitsCompressor {

    private DecimalSeries decimalSeries;

    private int blockSize;

    private int patchSize;

    private int patchNum;

    private OutputBitStream out;

    private int size;

    private int columnSize = 4;


    public MergeDigitsCompressor(DecimalSeries decimalSeries, int blockSize, int patchSize, int columnSize) {
        this.decimalSeries = decimalSeries;
        this.blockSize = blockSize;
        this.patchSize = patchSize;
        patchNum = (blockSize % patchSize == 0 ? blockSize / patchSize : (blockSize / patchSize + 1));
        out = new OutputBitStream(
                new byte[20000]);
        size = 0;
        this.columnSize = columnSize;
    }

    public void compress() {
        int[][] digits = decimalSeries.getDigits();
        boolean[] signs = decimalSeries.getSigns();
        int decimalSeriesSize = decimalSeries.getSize();
        if (decimalSeriesSize == 0) {
            return;
        }

        int totalDigitsLen = decimalSeries.getValidDigitCount();

        int hundredNumCnt = totalDigitsLen % columnSize == 0 ? totalDigitsLen / columnSize : 1 + totalDigitsLen / columnSize;
        int patchNum = decimalSeriesSize % patchSize == 0 ? decimalSeriesSize / patchSize : (decimalSeriesSize / patchSize + 1);

        int[][] hundredNum = new int[hundredNumCnt][decimalSeriesSize];
        int[][] fixNumLen = new int[hundredNumCnt][patchNum];

        // 处理数据，每三位数据组成一个百位的数据进行存储
        for (int i = decimalSeriesSize - 1; i >= 0; i--) {
            int patchIdx = i / patchSize;
            for (int digitIdx = 0, j = 0; j < hundredNumCnt; j++) {
                for (int k = 0; k < columnSize && digitIdx < totalDigitsLen; k++) {
                    hundredNum[j][i] = hundredNum[j][i] * 10 + digits[digitIdx++][i];
                }
//                fixNumLen[j][patchIdx] = Math.max(fixNumLen[j][patchIdx], getBitNum(hundredNum[j][i]));
            }
        }

        for (int i = decimalSeriesSize - 1; i > 0; i--) {
            for (int j = 0; j < hundredNumCnt; j++) {
                hundredNum[j][i] ^= hundredNum[j][i - 1];
                fixNumLen[j][i / patchSize] = Math.max(fixNumLen[j][i / patchSize], getBitNum(hundredNum[j][i]));
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
        for (int i = 0; i < hundredNumCnt; i++) {
            for (int j = 0; j < decimalSeriesSize; j += patchSize) {
                int pIdx = j / patchSize;
                if (fixNumLen[i][pIdx] == 0) {
                    out.writeBit(false);
                    size++;
                } else {
                    out.writeBit(true);
                    out.writeInt(fixNumLen[i][pIdx], 4);
                    size += 5;

                    for (int k = 0; k < patchSize; k++) {
                        if (j + k < decimalSeriesSize) {
                            out.writeInt(hundredNum[i][j + k], fixNumLen[i][pIdx]);
                        } else {
                            out.writeInt(0, fixNumLen[i][pIdx]);
                        }
                        size += fixNumLen[i][pIdx];
                    }
                }
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
