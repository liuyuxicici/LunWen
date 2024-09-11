package org.bjtu.compress.liu.iforest;

/**
 * @description: 树的非叶子节点类
 * @author：lyx
 * @date: 2024/9/9
 */
public class TreeBranch extends ITreeNode {

    ITreeNode left;

    ITreeNode right;

    // 分割属性的值
    double splitValue;

    // 分割属性的下标
    int splitAttr;

    public TreeBranch(ITreeNode left, ITreeNode right, double splitValue, int splitAttr) {
        this.left = left;
        this.right = right;
        this.splitValue = splitValue;
        this.splitAttr = splitAttr;
    }

    public ITreeNode getLeft() {
        return left;
    }

    public void setLeft(ITreeNode left) {
        this.left = left;
    }

    public ITreeNode getRight() {
        return right;
    }

    public void setRight(ITreeNode right) {
        this.right = right;
    }

    public double getSplitValue() {
        return splitValue;
    }

    public void setSplitValue(double splitValue) {
        this.splitValue = splitValue;
    }

    public int getSplitAttr() {
        return splitAttr;
    }

    public void setSplitAttr(int splitAttr) {
        this.splitAttr = splitAttr;
    }
}
