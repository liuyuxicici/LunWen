package org.bjtu.compress.liu.compressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.bjtu.compress.liu.entity.Decimal;
import org.bjtu.compress.liu.entity.DecimalSeries;
import org.bjtu.compress.liu.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author：lyx
 * @date: 2024/9/13
 */
public abstract class AbstractDecimalCompressor implements ICompressor {
    int size = 0;

    protected int blockSize = 1024;
    protected int patchSize = 32;

    private List<Integer> bias = new ArrayList<>(blockSize);

    protected int leastSignificantPos;

    protected int fractionLen;


    public int compressValue(DecimalSeries data) {
        // 1、数据采样，取出分布最多的数据最低有效位
        int dataSize = data.getSize();
        writeInt(dataSize, 20);
        size += 20;
        leastSignificantPos = data.getLastDigitIndex();

        fractionLen = leastSignificantPos < 0 ? (int) Math.ceil(-leastSignificantPos / Math.log10(2)) : 0;
        setFractionLen(fractionLen);
//        out.writeInt(-leastSignificantPos, 32);
        writeInt((leastSignificantPos << 1) ^ (leastSignificantPos >> 31), 10);
        size += 10;
        // 2、数据低位为0，舍去

        // 3、数据编码// 4、数据异或压缩
        size += signCompress(data.getSigns());
        Decimal[] decimals = data.getDecimals();
        for (Decimal decimal : decimals) {
            encodeValue(decimal);
        }
        // 5、前缀偏移压缩
        size += getXorSize();
        // 6、结束压缩，关闭输出流
        close();


        return 0;
    }


    public void encodeValue(Decimal v) {
        long integerValue = v.getIntegerValue();
        // 数据有效位的位置
        int lastSigDigitIndex = v.getLastSigDigitIndex();
        long fractionValue = v.getFractionValue() * (int) Math.pow(10, lastSigDigitIndex - leastSignificantPos);

        int integerBitValue = DataUtils.getBitNum(integerValue);
        int fractionBitValue = DataUtils.getBitNum(fractionValue);

        long encodedVal = 0;

        if (integerValue != 0) { // 整数部分不为0


            encodedVal = integerValue;
        }
        encodedVal = (encodedVal << fractionLen);
        if (fractionValue != 0) { // 小数部分不为0
            encodedVal ^= (DataUtils.reverseValidBits(fractionValue) << (fractionLen - fractionBitValue));

        }

        int nbias = xorCompress(encodedVal, integerBitValue);
        size += biasCompress(nbias);
        bias.add(nbias);

    }

    public List<Integer> getBias() {
        return bias;
    }

    public abstract int signCompress(boolean[] sign);

    protected abstract int xorCompress(long vPrimeLong, int bias);

    protected abstract int biasCompress(int bias);

    protected abstract void setFractionLen(int fractionLen);

    protected abstract int writeInt(int n, int len);

    protected abstract int writeLong(long n, int len);

    protected abstract int writeBit(boolean bit);

    protected abstract int getXorSize();

    public int getSize() {
        return size;
    }

}
