package org.bjtu.compress.liu.decompressor;

import org.urbcomp.startdb.compress.elf.decompressor.IDecompressor;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author：lyx
 * @date: 2024/8/23
 */
public abstract class AbstractDigitDecompressor implements IDecompressor {

    protected int blockSize;

    protected int patchSize;


    @Override
    public List<Double> decompress() {
        // 解压头部
        blockSize = readInt(16);
        patchSize = readInt(8);

        // 解压数据部分
        List<Double> doubles = new ArrayList<>();
        Double value;

        while ((value = nextValue()) != null) {
            doubles.add(value);
        }
        return doubles;
    }


    private Double nextValue() {


        return digitDecompress();
    }

    protected abstract int readInt(int len);

    protected abstract Double digitDecompress();

}
