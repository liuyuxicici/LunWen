package org.bjtu.compress.oran.compressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.bjtu.compress.oran.xorcompressor.HuffmanXORCompressor;


public class HuffCompressor extends AbstractCompressor {
    private final HuffmanXORCompressor xorCompressor;

    public HuffCompressor() {
        xorCompressor = new HuffmanXORCompressor();
    }

    protected int writeInt(int n, int len) {
        OutputBitStream os = xorCompressor.getOutputStream();
        os.writeInt(n, len);
        return len;
    }

    public void compress() {
        this.xorCompressor.compress();
    }

    public int getVarint() {
        return this.xorCompressor.getVarint();
    }

    public void getLeadAndTrail() {
        xorCompressor.leadAndTrailSize();
    }

    protected int writeBit(boolean bit) {
        OutputBitStream os = xorCompressor.getOutputStream();
        os.writeBit(bit);
        return 1;
    }

    protected int xorCompress(long vPrimeLong) {
        return xorCompressor.addValue(vPrimeLong);
    }

    public byte[] getBytes() {
        return xorCompressor.getOut();
    }

    public void close() {
        // we write one more bit here, for marking an end of the stream.
        writeInt(2, 2);  // case 10
        xorCompressor.close();
    }

    public String getKey() {
        return getClass().getSimpleName();
    }

    public int leadAndTrailSize() {
        return xorCompressor.leadAndTrailSize();
    }
}
