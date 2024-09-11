package org.bjtu.compress.liu.iforest;

/**
 * @description: 树的叶子节点类
 * @author：lyx
 * @date: 2024/9/9
 */
public class TreeLeaf extends ITreeNode {

    int size;

    public TreeLeaf(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
