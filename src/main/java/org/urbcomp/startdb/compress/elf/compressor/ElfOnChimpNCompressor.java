package org.urbcomp.startdb.compress.elf.compressor;

import gr.aueb.delorean.chimp.ChimpN;
import gr.aueb.delorean.chimp.OutputBitStream;

import java.util.Map;

public class ElfOnChimpNCompressor extends AbstractElfCompressor {
    private final ChimpN chimpN;

    @Override
    public Map<Integer, Integer> getMap() {
        return null;
    }

    public ElfOnChimpNCompressor(int previousValues) {
        chimpN = new ChimpN(previousValues);
    }

    @Override
    protected int writeInt(int n, int len) {
        OutputBitStream os = chimpN.getOutputStream();
        os.writeInt(n, len);
        return len;
    }

    @Override
    protected int writeBit(boolean bit) {
        OutputBitStream os = chimpN.getOutputStream();
        os.writeBit(bit);
        return 1;
    }

    @Override
    protected int xorCompress(long vPrimeLong) {
        return chimpN.addValue(vPrimeLong);
    }

    @Override
    public byte[] getBytes() {
        return chimpN.getOut();
    }

    @Override
    public void close() {
        // we write one more bit here, for marking an end of the stream.
        writeInt(2, 2); // case 10
        chimpN.close();
    }

    public void setBias(int bias) {
    }
}
