package org.bjtu.compress.huffman;

public class HuffmanNode {
    public int symbol;
    public int frequency;

    HuffmanNode left;

    HuffmanNode right;

    HuffmanNode(int sym, int fre) {
        this.symbol = sym;
        this.frequency = fre;
        left = null;
        right = null;
    }
}
