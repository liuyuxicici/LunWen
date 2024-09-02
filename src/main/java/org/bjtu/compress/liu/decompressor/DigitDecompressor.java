package org.bjtu.compress.liu.decompressor;

import gr.aueb.delorean.chimp.InputBitStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 解压算法
 * @author：lyx
 * @date: 2024/8/19
 */
public class DigitDecompressor {

    private int blockSize;

    private int patchSize;
    /**
     * 整数位数
     */
    private int sigIntDigitNum;

    /**
     * 小数位数
     */
    private int sigFracDigitNum;

    private final InputBitStream in;


    public DigitDecompressor(byte[] bytes) {
        in = new InputBitStream(bytes);
    }


    public List<Double> decompress() throws IOException {
        blockSize = 1024;
        patchSize = 8;

        int patchNum = blockSize % patchSize == 0 ? (blockSize / patchSize) : (blockSize / patchSize + 1);


        sigIntDigitNum = in.readInt(8);
        sigFracDigitNum = in.readInt(8);

        boolean[] sign = new boolean[blockSize];
        int[] signHeads = new int[patchNum];
        // 解压符号部分
        for (int i = 0; i < patchNum; i++) {
            signHeads[i] = in.readBit();
        }
//        int signDataSize = in.readInt(8) * 8;
        for (int i = 0; i < patchNum; i++) {
            long signs = 0;
            if (signHeads[i] == 1) {
                signs = in.readLong(patchSize);
            }
            for (int j = patchSize - 1; j >= 0; j--) {
                sign[i * patchSize + j] = signs % 2 == 1 ? true : false;
                signs >>= 1;
            }
        }

        int[][] digits = new int[blockSize][sigIntDigitNum + sigFracDigitNum];
        int[][] digitHeads = new int[patchNum][sigIntDigitNum + sigFracDigitNum];
        for (int i = 0; i < sigIntDigitNum + sigFracDigitNum; i++) {
            digits[0][i] = in.readInt(4);
            for (int j = 0; j < patchNum; j++) {
                digitHeads[j][i] = in.readBit();
            }
            for (int j = 0; j < patchNum; j++) {
                if (digitHeads[j][i] != 0) {
                    int fixLen = in.readInt(3);
                    for (int k = 0; k < patchSize; k++) {
                        digits[j * patchSize + k][i] = in.readInt(fixLen) ^ (j * patchSize + k == 0 ? digits[0][i] : digits[j * patchSize + k - 1][i]);
                    }
                }
            }
        }
        List<Double> decompressed = new ArrayList<>(blockSize);
        // 处理数据
//        for (int i = 0; i < blockSize; i++) {
//            StringBuilder sb = new StringBuilder();
//            for (int j = 0; j < sigIntDigitNum; j++) {
//                sb.append(digits[i][j]);
//            }
//            sb.append(".");
//            for (int j = sigIntDigitNum; j < sigIntDigitNum + sigFracDigitNum; j++) {
//                sb.append(digits[i][j]);
//            }
//            decompressed.add(Double.parseDouble(sb.toString()));
//        }

        return decompressed;
    }


}
