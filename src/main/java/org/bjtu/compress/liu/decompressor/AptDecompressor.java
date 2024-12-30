package org.bjtu.compress.liu.decompressor;

import gr.aueb.delorean.chimp.InputBitStream;
import org.bjtu.compress.liu.utils.DataUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author：lyx
 * @date: 2024/12/21
 */
public class AptDecompressor {

    private final int blockSize;

    private final int patchSize;

    private final InputBitStream in;

    public AptDecompressor(int blockSize, int patchSize, InputBitStream in) {
        this.blockSize = blockSize;
        this.patchSize = patchSize;
        this.in = in;
    }

    public double[] decompress() throws IOException {
        double[] values = new double[blockSize];

        // 1、精度解压
        List<Integer> precisions = new ArrayList<>(blockSize);
        int size = 0;
        while (size < blockSize) {
            int precision = in.readInt(5);
            int runLength = 0;
            int bytesCnt = 0;
            do {
                runLength ^= in.readInt(7) << (bytesCnt * 7);
                bytesCnt++;
            } while (in.readBit() == 1);

            size += runLength;

            while (runLength > 0) {
                precisions.add(precision);
                runLength--;
            }
        }

        // 2、残差数据解压
        //2.1残差数据centerBits解压
        List<Integer> centerBitsList = new ArrayList<>(blockSize - 1);

        while (centerBitsList.size() < blockSize - 1) {
            int bitNum = in.readInt(6);
            int minValue = in.readInt(bitNum);
            int fixLength = in.readInt(6);

            for (int i = 0; i < patchSize && centerBitsList.size() < blockSize - 1; i++) {
                int centerBits = in.readInt(fixLength) + minValue;
                centerBitsList.add(centerBits);
            }
        }


        //2.2残差数据有效位解压
        List<Long> residualList = new ArrayList<>(blockSize);
        int firstValidLen = in.readInt(7);
        long first = 0L;
        if (firstValidLen > 1) {
            first = (1L << (firstValidLen - 1)) ^ in.readLong(firstValidLen - 1);
        } else if (firstValidLen == 1) {
            first = 1L;
        }
        residualList.add(first);

        int storedTrailingZeros = Integer.MAX_VALUE;
        long residualValue = 0L;
        while (residualList.size() < blockSize) {
            int centerBits = centerBitsList.get(residualList.size() - 1);
            if (centerBits > 0) {
                switch (in.readBit()) {
                    case 1:

                        storedTrailingZeros = in.readInt(6);

                    case 0:  // case 0

                        residualValue = ((1L << (centerBits - 1)) ^ in.readLong(centerBits - 1)) << storedTrailingZeros;
                        residualList.add(residualValue);
                        break;
                }


            } else {
                residualList.add(0L);
            }

        }

        // 3.根据精度和残差数据恢复原数据
        long quantization = 0L;
        long preQuantization = 0L;
        double val = 0.0d;
        int compressIdx = 0;
        int precision = 0, referPrecision = 0;
        for (long residual : residualList) {
            residual = (residual >>> 1) ^ -(residual & 1L);

            precision = precisions.get(compressIdx);
            if (precision > referPrecision) {
                preQuantization *= (long) Math.pow(10, precision - referPrecision);
            }

            quantization = residual ^ preQuantization;

            val = getOriginalValue(quantization, precision);
            values[compressIdx] = val;

            preQuantization = quantization;
            referPrecision = precision;
            compressIdx++;
        }

        return values;
    }

    private double getOriginalValue(long quantization, int precision) {

        double value = 0.0d;
        if (quantization != 0) {


            StringBuffer str = new StringBuffer(Long.toString(Math.abs(quantization)));
            int decimalIndex = str.length() - precision;
            if (decimalIndex > 0) {

                str.insert(str.length() - precision, '.');
            } else {
                str.insert(0, "0.");
                while (decimalIndex < 0) {
                    str.insert(2, 0);
                    decimalIndex++;
                }
            }

            value = Double.parseDouble(str.toString());

        }

        if (quantization < 0) {
            value = -value;
        }
        return value;
    }
}
