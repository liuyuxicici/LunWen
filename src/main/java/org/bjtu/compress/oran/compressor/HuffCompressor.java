package org.bjtu.compress.oran.compressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.bjtu.compress.huffman.HuffmanCoding;
import org.bjtu.compress.oran.xorcompressor.HuffmanXORCompressor;


public class HuffCompressor extends AbstractCompressor {
    private final HuffmanXORCompressor xorCompressor;
    public HuffmanCoding leadingHuffmanTree = new HuffmanCoding();
    public HuffmanCoding trailingHuffmanTree = new HuffmanCoding();

    public HuffCompressor() {
        xorCompressor = new HuffmanXORCompressor();
    }

    protected int writeInt(int n, int len) {
        OutputBitStream os = xorCompressor.getOutputStream();
        os.writeInt(n, len);
        return len;
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

    public void buildTree() {
        leadingHuffmanTree.buildTree(xorCompressor.leadingZerosArray);
        trailingHuffmanTree.buildTree(xorCompressor.trailingZerosArray);
    }

    public int leadAndTrailSize() {
        int lead = 0, trail = 0;
        //计算存储前导零的哈夫曼编码长度
        for (int i = 0; i < xorCompressor.leadingZerosArray.length; i++) {
            if (xorCompressor.leadingZerosArray[i] > 0) {
                lead += xorCompressor.leadingZerosArray[i] * leadingHuffmanTree.symbolToCode.get(i).length();
            }
        }
        //计算存储后导零的哈夫曼编码长度
        for (int i = 0; i < xorCompressor.trailingZerosArray.length; i++) {
            if (xorCompressor.trailingZerosArray[i] > 0) {
                trail += xorCompressor.trailingZerosArray[i] * trailingHuffmanTree.symbolToCode.get(i).length();
            }
        }
        return lead + trail;
    }

    public String getKey() {
        return getClass().getSimpleName();
    }
}
