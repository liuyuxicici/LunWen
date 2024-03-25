package org.bjtu.compress.huffman;

import org.bjtu.compress.huffman.HuffmanNode;
import org.bjtu.compress.huffman.ImplementComparator;

import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffmanCoding {
    // 用于存储符号与其哈夫曼编码的映射
    public Map<Integer, String> symbolToCode = new HashMap<>();
    public Map<String, Integer> codeToSymbol = new HashMap<>();

    HuffmanNode root = null;

    // 构建哈夫曼树
    public void buildTree(int[] frequencies) {
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<HuffmanNode>(new ImplementComparator());
        // 初始化所有非零频率的节点并加入优先队列
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) { // 只处理非零频率的符号
                HuffmanNode hn = new HuffmanNode(i, frequencies[i]);
                hn.left = null;
                hn.right = null;
                queue.add(hn);
            }
        }

        // 特殊情况处理：如果队列中只有一个符号，直接作为根节点，无需合并
        if (queue.size() == 1) {
            root = queue.poll();
            symbolToCode.put(root.symbol, "0");
            codeToSymbol.put("0", root.symbol);
            return;
        }

        // 构建哈夫曼树
        while (queue.size() > 1) {
            HuffmanNode x = queue.poll();
            HuffmanNode y = queue.poll();

            HuffmanNode f = new HuffmanNode(-1, x.frequency + y.frequency);
            f.left = x;
            f.right = y;
            root = f;
            queue.add(f);
        }

        // 生成哈夫曼编码
        if (root != null) {
            generateCode(root, new StringBuilder());
        }
    }

    private void generateCode(HuffmanNode node, StringBuilder prefix) {
        if (node != null) {
            if (node.left == null && node.right == null) {
                symbolToCode.put(node.symbol, prefix.toString());
            } else {
                generateCode(node.left, new StringBuilder(prefix).append('0'));
                generateCode(node.right, new StringBuilder(prefix).append('1'));
            }
        }
    }

}
