package org.bjtu.compress.liu.decompressor;

import gr.aueb.delorean.chimp.InputBitStream;
import org.bjtu.compress.liu.decompressor.xorDecompressor.DecimalXORDecompressor;
import org.urbcomp.startdb.compress.elf.xordecompressor.ElfXORDecompressor;

import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author：lyx
 * @date: 2024/9/15
 */
public class DecimalDecompressor extends AbstractDecimalDecompressor {

    private final DecimalXORDecompressor decimalXORDecompressor;

    public DecimalDecompressor(byte[] bytes) {
        decimalXORDecompressor = new DecimalXORDecompressor(bytes);
    }


    @Override
    protected boolean[] signDecompress(int size) {
        boolean[] signs = new boolean[size];
        for (int i = 0; i < size; i += patchSize) {
            if (readBit() == 1) {

                if (readBit() == 1) {  // case 11
                    for (int pIdx = 0, cnt = i; pIdx < patchSize; pIdx++, cnt++) {
                        int signBit = readBit();
                        if (cnt < dataSize) {
                            signs[cnt] = signBit == 1 ? true : false;
                        }
                    }
                } else {   // case 10
                    for (int j = i; j < i + patchSize && j < dataSize; j++) {
                        signs[j] = true;
                    }
                }
            }

        }
        return signs;
    }

    @Override
    protected Long xorDecompress(int bias) {

        return decimalXORDecompressor.readValue(bias);
    }

    @Override
    protected int readBit() {
        InputBitStream in = decimalXORDecompressor.getInputStream();
        try {
            return in.readBit();
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }
    }

    @Override
    protected int readInt(int len) {
        InputBitStream in = decimalXORDecompressor.getInputStream();
        try {
            return in.readInt(len);
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }
    }

    @Override
    protected long readLong(int len) {
        InputBitStream in = decimalXORDecompressor.getInputStream();
        try {
            return in.readLong(len);
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }
    }

    @Override
    protected void setFractionLen(int fractionLen) {
        decimalXORDecompressor.setFractionLen(fractionLen);
    }


}
