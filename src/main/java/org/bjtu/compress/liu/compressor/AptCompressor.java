package org.bjtu.compress.liu.compressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.bjtu.compress.liu.entity.Decimal;
import org.bjtu.compress.liu.entity.DecimalSeries;
import org.bjtu.compress.liu.utils.DataUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: Adaptive precision block compression
 * @author：lyx
 * @date: 2024/10/8
 */
public class AptCompressor {

    protected final OutputBitStream out;

    private final int blockSize;

    private final int patchSize;

    private int size = 0;


    public AptCompressor(int blockSize, int patchSize) {
        this.out = new OutputBitStream(new byte[100000]);
        this.blockSize = blockSize;
        this.patchSize = patchSize;
    }

    public void compressValue(DecimalSeries decimalSeries) throws IOException {
        Decimal[] decimals = decimalSeries.getDecimals();

        int referPrecision = 0;
        long preQuantization = 0L;
        List<Integer> precisionList = new ArrayList<>();
        List<Long> residualList = new ArrayList<>();
        for (Decimal decimal : decimals) {
            // 1. 计算精度
            int precision = -decimal.getLastSigDigitIndex();
            long quantization = decimal.getValidData();
            if (precision < referPrecision) { // 当前浮点数精度小于前一浮点数精度
                quantization *= (long) Math.pow(10, referPrecision - precision);
                precision = referPrecision;

            }
            if (precision > referPrecision) {// 当前浮点数精度大于前一浮点数精度
                preQuantization *= (long) Math.pow(10, precision - referPrecision);
            }


            // 2. 预测、量化、计算残差
            long xor = quantization ^ preQuantization;
            xor = (xor << 1) ^ (xor >> 63);
            residualList.add(xor);
            precisionList.add(precision);
            preQuantization = quantization;
            referPrecision = precision;
        }

        // 3. 精度压缩
        int precisionSize = precisionCompress(precisionList);
        // 4. 残差压缩
        OutputBitStream residualsOut = new OutputBitStream(new byte[10000]);
        int residualSize = residualCompress(residualList, residualsOut);

        size += precisionSize + residualSize;
        out.write(residualsOut.getBuffer(), 0, residualSize);

        // 5. 关闭输出流，结束压缩
        close();
    }

    private int precisionCompress(List<Integer> precisions) {
        int thisSize = 0;
        int currPrecision = 0;
        int currRunLength = 0;

        for (int precision : precisions) {
            if (currPrecision != precision) {
                if (currRunLength != 0) { // 写入precision和run length
                    out.writeInt(currPrecision, 5);
                    thisSize += 5;
                    while (currRunLength != 0) {
                        out.writeInt(currRunLength, 7);
                        currRunLength >>>= 7;
                        if (currRunLength != 0) {
                            out.writeBit(true);
                        } else {
                            out.writeBit(false);
                        }
                        thisSize += 8;
                    }
                }
                currPrecision = precision;
                currRunLength = 1;
            } else {
                currRunLength++;
            }
        }

        out.writeInt(currPrecision, 5);
        thisSize += 5;
        while (currRunLength != 0) {
            out.writeInt(currRunLength, 7);
            currRunLength >>>= 7;
            if (currRunLength != 0) {
                out.writeBit(true);
            } else {
                out.writeBit(false);
            }


            thisSize += 8;
        }
        return thisSize;
    }

    private int residualCompress(List<Long> residualList, OutputBitStream residualOut) {
        int thisSize = 0;

//        List<Integer> trailingZeros = new ArrayList<>(blockSize);
//        List<Integer> centerBits = new ArrayList<>(blockSize);
//        AptXORCompressor aptXORCompressor = new AptXORCompressor(residualOut, centerBits, trailingZeros);
//        for (long r : residual) {
//            aptXORCompressor.addValue(r);
//        }
//        thisSize += aptXORCompressor.getSize();
//
//
//        OutputBitStream centerBitsOut = new OutputBitStream(new byte[10000]);
//        thisSize += fixLengthCoding(centerBits, centerBitsOut);
//
//        OutputBitStream trailingOut = new OutputBitStream(new byte[10000]);
//        thisSize += fixLengthCoding(trailingZeros, trailingOut);

        int storedTrailingZeros = Integer.MAX_VALUE;
        boolean first = true;

        List<Integer> centerBitsList = new ArrayList<>(blockSize - 1);
//        List<Integer> trailingBitsList = new ArrayList<>(blockSize - 1);

        for (long value : residualList) {
            // 第一个压缩的数据
            if (first) {
                first = false;
                // 数据有效位个数
                int validBits = 64 - Long.numberOfLeadingZeros(value);
                residualOut.writeInt(validBits, 7);
                if (validBits > 1) {
                    residualOut.writeLong(value, validBits - 1);
                }
                thisSize += validBits + 6;
            } else { // 非首个压缩数据
                if (value == 0) { //  残差为0
//                    residualOut.writeInt(2, 2);
//                    thisSize += 2;
                    centerBitsList.add(0);
                } else { // 压缩数据不为0
                    int trailingZeros = Long.numberOfTrailingZeros(value);

                    if (trailingZeros >= storedTrailingZeros) {// case 0 : 压缩数据尾部零个数大于存储的尾部零个数，复用
                        residualOut.writeBit(false);

                        thisSize++;
                    } else { // case 1 : 压缩数据尾部零个数小于存储的尾部零个数，重写
                        storedTrailingZeros = trailingZeros;
                        residualOut.writeBit(true);
                        residualOut.writeInt(storedTrailingZeros, 6); // 尾部零个数小于64


                        thisSize += 7;
                    }


                    // 写入数据有效位
                    int centerBits = 64 - Long.numberOfLeadingZeros(value) - storedTrailingZeros;
//                    int centerBits = 64 - Long.numberOfLeadingZeros(value) - trailingZeros;
                    centerBitsList.add(centerBits);
//                    trailingBitsList.add(trailingZeros);
                    if (centerBits > 1) {
                        residualOut.writeLong(value >>> storedTrailingZeros, centerBits - 1);
//                        residualOut.writeLong(value >>> (trailingZeros + 1), centerBits - 2);
                        thisSize += centerBits - 1;
                    }
                }
            }
        }
        residualOut.flush();

        thisSize += fixLengthCoding(centerBitsList, out);
//        thisSize += fixLengthCoding(trailingBitsList, out);
        return thisSize;
    }

    /**
     * 固定长度编码
     *
     * @param list
     * @param fixLengthCodingOut
     * @return
     */
    private int fixLengthCoding(List<Integer> list, OutputBitStream fixLengthCodingOut) {
        int thisSize = 0;
        int listSize = list.size();
        for (int i = 0; i < listSize; i += patchSize) {
            int pEnd = i + patchSize;
            int minValue = Integer.MAX_VALUE;
            //计算一个patch中的的最小值
            for (int j = i; j < pEnd && j < listSize; j++) {
                minValue = Math.min(minValue, list.get(j));
            }

            //计算patch中所有值与最小值的差
            int fixLength = 0;
            for (int j = i; j < pEnd && j < listSize; j++) {
                int delta = list.get(j) - minValue;
                list.set(j, delta);
                fixLength = Math.max(fixLength, DataUtils.getBitNum(delta));
            }

            //写入最小值
            fixLengthCodingOut.writeInt(DataUtils.getBitNum(minValue), 6);
            fixLengthCodingOut.writeInt(minValue, DataUtils.getBitNum(minValue));


            //写入固定长度
            fixLengthCodingOut.writeInt(fixLength, 6);
            thisSize += DataUtils.getBitNum(minValue) + 12;
            //固定长度编码
            for (int j = i; j < pEnd && j < listSize; j++) {
                fixLengthCodingOut.writeInt(list.get(j), fixLength);
                thisSize += fixLength;
            }
        }
        return thisSize;
    }

    public void close() {
        out.writeLong(0L, 64);
        out.writeBit(false);
        out.flush();
    }

    public int getSize() {
        return size;
    }

    public byte[] getBytes() {
        return out.getBuffer();
    }

    public String getKey() {
        return getClass().getSimpleName();
    }
}
