package org.bjtu.compress.oran.xorcompressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.bjtu.compress.huffman.HuffmanCoding;
import org.bjtu.compress.oran.utils.Elf64Utils;

import java.util.ArrayList;
import java.util.List;

public class HuffmanXORCompressor {
    private long storedVal = 0;
    private int size;
    private final static long END_SIGN = Double.doubleToLongBits(Double.NaN);
    private final OutputBitStream out;
    private final OutputBitStream centerOut;
    private HuffmanCoding leadingHuffmanTree = new HuffmanCoding();
    private HuffmanCoding trailingHuffmanTree = new HuffmanCoding();
    private int[] leadFrequencies = new int[65];
    private int[] trailFrequencies = new int[65];
    private List<Integer> lead_raws = new ArrayList<>();
    private List<Integer> trail_raws = new ArrayList<>();
    private StringBuilder lead_compress_res = new StringBuilder();
    private StringBuilder trail_compress_res = new StringBuilder();

    private int centerTotal = 0;


    public HuffmanXORCompressor() {
        out = new OutputBitStream(
                new byte[10000]);  // for elf, we need one more bit for each at the worst case
        centerOut = new OutputBitStream(
                new byte[10000]);  // for elf, we need one more bit for each at the worst case
        size = 0;
    }

    //接受的是经擦除、未异或的浮点序列
    public int addValue(long value) {
        int thisSize = 0;
        long xor = value ^ storedVal;
        int leadingZeros = Long.numberOfLeadingZeros(xor);
        int TrailingZeros = Long.numberOfTrailingZeros(xor);

        if (leadingZeros == 64) {
            lead_raws.add(64);//保留前导零结果
            leadFrequencies[64] += 1;
        } else {
            lead_raws.add(leadingZeros);//保留前导零结果
            trail_raws.add(TrailingZeros);//保留尾部零结果

            leadFrequencies[leadingZeros] += 1;
            trailFrequencies[TrailingZeros] += 1;
            int center = 64 - leadingZeros - TrailingZeros;
            centerOut.writeLong(xor >> (TrailingZeros + 1), center - 1);//写入中心位
            thisSize += center >= 2 ? center - 2 : center;
        }
        centerTotal += thisSize;
        size += thisSize;
        storedVal = value;
        return thisSize;
    }

    private void buildTree() {
        leadingHuffmanTree.buildTree(leadFrequencies);
        trailingHuffmanTree.buildTree(trailFrequencies);
    }

    public int getHuffLength() {
        int lead_total = 0;
        int trail_total = 0;
        for (int i = 0; i < 65; i++) {
            lead_total += Elf64Utils.getLength(leadFrequencies[i]);
            trail_total += Elf64Utils.getLength(trailFrequencies[i]);
        }
        return lead_total + trail_total;
    }

    public void compress() {
        buildTree();
        for (Integer lead : lead_raws) {
            lead_compress_res.append(leadingHuffmanTree.symbolToCode.get(lead));
        }
        for (Integer trail : trail_raws) {
            trail_compress_res.append(trailingHuffmanTree.symbolToCode.get(trail));
        }
    }

    public int leadAndTrailSize() {
        return lead_compress_res.length() + trail_compress_res.length();
    }

    public int getVarint() {
        return Elf64Utils.getLength(lead_compress_res.length()) +
                +Elf64Utils.getLength(centerTotal) +
                Elf64Utils.getLength(trail_compress_res.length()) +
                getHuffLength();
    }

    public int addValue(double value) {
        return addValue(Double.doubleToRawLongBits(value));
    }

    public OutputBitStream getOutputStream() {
        return this.out;
    }

    public void close() {
        addValue(END_SIGN);
        out.writeBit(false);
        out.flush();
    }

    public int getSize() {
        return size;
    }

    public byte[] getOut() {
        return out.getBuffer();
    }
}
