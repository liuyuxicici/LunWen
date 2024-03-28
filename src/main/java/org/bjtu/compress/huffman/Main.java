package org.bjtu.compress.huffman;

import java.util.Map;

import org.bjtu.compress.oran.utils.Elf64Utils;

public class Main {
    public static void main(String[] args) {
//        HuffmanCoding huffman = new HuffmanCoding();
//        int[] symbols = {0, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 64};
//        int[] frequencies = {4754, 2524, 366, 14981, 5164,
//                14756, 16644, 12467, 7598, 3949, 2033, 609, 14055};
//        int[] fres = new int[65];
//        for (int i = 0; i < symbols.length; i++) {
//            fres[symbols[i]] = frequencies[i];
//        }
//        huffman.buildTree(fres);
//        long total = 0;
//        for (int i = 0; i < symbols.length; i++) {
//            total += frequencies[i] * huffman.symbolToCode.get(symbols[i]).length();
//        }
//        System.out.println(total);
//        System.out.println("符号与哈夫曼编码的映射:");
//        for (Map.Entry<Integer, String> entry : huffman.symbolToCode.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }
        System.out.println(Elf64Utils.getLength(0));
    }
}
