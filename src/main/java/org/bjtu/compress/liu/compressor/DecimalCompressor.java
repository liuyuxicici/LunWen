package org.bjtu.compress.liu.compressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.bjtu.compress.liu.compressor.xorCompressor.DecimalXORCompressor;
import org.bjtu.compress.liu.utils.FixLengthFORCompressor;

import java.util.List;

/**
 * @description:
 * @author：lyx
 * @date: 2024/9/14
 */
public class DecimalCompressor extends AbstractDecimalCompressor {

    private final DecimalXORCompressor decimalXORCompressor;

    private final FixLengthFORCompressor fixLengthFORCompressor;

    public DecimalCompressor() {
        fixLengthFORCompressor = new FixLengthFORCompressor(patchSize);
        decimalXORCompressor = new DecimalXORCompressor();
    }

    @Override
    public int signCompress(boolean[] sign) {
        int dataSize = sign.length;
        int thisSize = 0;
        // 符号位存储
        for (int i = 0; i < dataSize; i += patchSize) {
            long signsOfPatch = 0;
            for (int pIdx = 0; pIdx < patchSize; pIdx++) {
                signsOfPatch = (signsOfPatch << 1) ^ (i + pIdx < dataSize && sign[i + pIdx] ? 0x1 : 0x0); // 不够一个patch用0补充
            }
            if (signsOfPatch == 0) {
                writeBit(false);
                thisSize++;
            } else {
                writeBit(true);
                writeLong(signsOfPatch, patchSize);
                thisSize += patchSize + 1;
            }
        }

        return thisSize;
    }

    @Override
    protected int xorCompress(long vPrimeLong, int bias) {
        return decimalXORCompressor.addValue(vPrimeLong, bias);

    }

    @Override
    protected int biasCompress(int bias) {
        return fixLengthFORCompressor.addValue(bias);
    }


    @Override
    public byte[] getBytes() {
        return decimalXORCompressor.getOut().getBuffer();
    }

    @Override
    public void close() {
        decimalXORCompressor.close();
    }

    @Override
    protected void setFractionLen(int fractionLen) {
        decimalXORCompressor.setFractionLen(fractionLen);
    }

    @Override
    protected int writeInt(int n, int len) {
        OutputBitStream os = decimalXORCompressor.getOut();
        os.writeInt(n, len);
        return len;
    }

    @Override
    protected int writeLong(long n, int len) {
        OutputBitStream os = decimalXORCompressor.getOut();
        os.writeLong(n, len);
        return len;
    }

    @Override
    protected int writeBit(boolean bit) {
        OutputBitStream os = decimalXORCompressor.getOut();
        os.writeBit(bit);
        return 1;
    }

    @Override
    protected int getXorSize() {
        return decimalXORCompressor.getSize();
    }


}
