package org.bjtu.compress.liu.iforest;

import org.ejml.data.DenseMatrix64F;

import java.util.List;

/**
 * @description: 异常森林类，完成异常点评估
 * @author：lyx
 * @date: 2024/9/9
 */
public class IForest {

    // 孤立森林中树的集合
    List<ITreeNode> iTrees;

    // 样本数量
    int maxSamples;

    public IForest(List<ITreeNode> iTrees, int maxSamples) {
        this.iTrees = iTrees;
        this.maxSamples = maxSamples;
    }

    public int predict(DenseMatrix64F x) {
        if (iTrees.size() == 0 || iTrees == null) {
            throw new IllegalArgumentException("Training Before predict is required!");
        }

        double sum = 0;
        for (int i = 0; i < iTrees.size(); i++) {
            sum += calPathLength(x, iTrees.get(i), 0);
        }

        double exponent = -(sum / iTrees.size()) / cost(maxSamples);
        double score = Math.pow(2, exponent);

        if (score > 0.7) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * 计算一个数据点在一棵树中的深度
     *
     * @param x
     * @param tree
     * @param pathLength
     * @return
     */
    public double calPathLength(DenseMatrix64F x, ITreeNode tree, int pathLength) {

        String simpleName = tree.getClass().getSimpleName();
        if ("TreeLeaf".equals(simpleName)) {
            int size = ((TreeLeaf) tree).getSize();
            return pathLength + cost(size);
        }

        TreeBranch treeBranch = (TreeBranch) tree;
        int splitAttr = treeBranch.getSplitAttr();
        double splitValue = treeBranch.getSplitValue();

        double value = x.get(0, splitAttr);
        if (value < splitValue) {
            ITreeNode left = treeBranch.getLeft();
            return calPathLength(x, left, pathLength + 1);
        } else {
            ITreeNode right = treeBranch.getRight();
            return calPathLength(x, right, pathLength + 1);
        }
    }

    /**
     * n个数据点下的平均高度  c(n) = 2 * H(n-1) - 2 * (n-1)/n
     *
     * @param n
     * @return
     */
    private double cost(int n) {

        if (n <= 1) {
            return 1.0d;
        }
        double hi = getHarmonic(n - 1);
        return 2 * hi - 2 * (n - 1) / n;
    }

    /**
     * 计算谐波数(harmonic number) H(i)= ln(i) + 0.5772156649
     *
     * @param i
     * @return
     */
    private double getHarmonic(int i) {
        return Math.log(i) + 0.5772156649;
    }
}
