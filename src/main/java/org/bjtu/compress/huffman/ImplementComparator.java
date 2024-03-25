package org.bjtu.compress.huffman;

import org.bjtu.compress.huffman.HuffmanNode;

import java.util.Comparator;

public class ImplementComparator implements Comparator<HuffmanNode> {
    public int compare(HuffmanNode x, HuffmanNode y) {
        return x.frequency - y.frequency;
    }
}
