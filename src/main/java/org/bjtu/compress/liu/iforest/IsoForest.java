package org.bjtu.compress.liu.iforest;

import org.ejml.data.DenseMatrix64F;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @description: 构建孤立森林
 * @author：lyx
 * @date: 2024/9/9
 */
public class IsoForest {

    public static IForest iForest;


    /**
     * 加载数据
     *
     * @param data
     * @return
     */
    public DenseMatrix64F loadData(double[][] data) {
        DenseMatrix64F denseMatrix64F = new DenseMatrix64F(data);
        return denseMatrix64F;
    }

    /**
     * 获取训练样本
     *
     * @param dataset
     * @param subSampleCount
     * @return
     */
    public DenseMatrix64F getSubSample(DenseMatrix64F dataset, int subSampleCount) {
        int features = dataset.numCols;
        DenseMatrix64F subSamples = new DenseMatrix64F(subSampleCount, features);
        int rows = dataset.numRows;
        int subSampleRange = rows / subSampleCount;
        Random random = new Random();
        for (int i = 0, sampleIndex = random.nextInt(subSampleRange);
             i < subSampleCount && sampleIndex < rows;
             i++, sampleIndex = i * subSampleRange + random.nextInt(subSampleRange)) {
            for (int j = 0; j < features; j++) {
                subSamples.set(i, j, dataset.get(i, j));
            }
        }
        return subSamples;
    }

    /**
     * 训练孤立森林
     *
     * @param data
     * @return
     */
    public IForest train(double[][] data) {
        DenseMatrix64F dataset = loadData(data);
        int rows = dataset.numRows;

        // 参数设置
        // 孤立树的数量
        int numTrees = 10;
        // 数据特征数
        int numFeatures = dataset.numCols;
        // 样本最大采样数
        int maxSamples = 256;
        // 样本数量
        int subSampleSize = Math.min(maxSamples, rows);
        // 树的最大深度
        int maxTreeHeight = (int) Math.ceil(bottomChanging(subSampleSize, 2));
        // 孤立森林
        List<ITreeNode> iTrees = new ArrayList<>();

        for (int i = 0; i < numTrees; i++) {
            // 获取样本
            DenseMatrix64F subSample = getSubSample(dataset, subSampleSize);
            // 使用样本生成一棵树
            ITreeNode tree = growTree(subSample, maxTreeHeight, numFeatures, 0);
            // 加入孤立森林
            iTrees.add(tree);
        }
        return new IForest(iTrees, maxSamples);
    }

    /**
     * 计算所需构建树的最大深度
     *
     * @param n
     * @param bottom
     * @return
     */
    private double bottomChanging(int n, int bottom) {

        double log = Math.log10(n) / Math.log10(bottom);
        return log;
    }

    /**
     * 生成一颗孤立树
     *
     * @param data
     * @param maxTreeHeight
     * @param numFeatures
     * @param currTreeHeight
     * @return
     */
    private ITreeNode growTree(DenseMatrix64F data, int maxTreeHeight, int numFeatures, int currTreeHeight) {
        // 当前树的深度大于标准树的深度， 或样本数量为1时 停止构造树， 返回叶子节点
        if (currTreeHeight >= maxTreeHeight || data.numRows <= 1) {
            return new TreeLeaf(data.numRows);
        }
        Random random = new Random();

        // 随机选择一个属性
        int feature = random.nextInt(numFeatures);
        int rows = data.numRows;
        // 随机选择一条数据
        int randomRow = random.nextInt(rows);
        double splitPoint = data.get(randomRow, feature);
        List<Integer> rightList = new ArrayList<Integer>();
        List<Integer> leftList = new ArrayList<Integer>();

        for (int i = 0; i < rows; i++) {
            if (data.get(i, feature) >= splitPoint) {
                rightList.add(i);
            } else {
                leftList.add(i);
            }
        }

        DenseMatrix64F left = new DenseMatrix64F(leftList.size(), numFeatures);
        DenseMatrix64F right = new DenseMatrix64F(rightList.size(), numFeatures);

        // 构建左子树
        for (int i = 0; i < leftList.size(); i++) {
            for (int j = 0; j < numFeatures; j++) {
                left.set(i, j, data.get(i, j));
            }
        }

        // 构建右子树
        for (int i = 0; i < rightList.size(); i++) {
            for (int j = 0; j < numFeatures; j++) {
                right.set(i, j, data.get(i, j));
            }
        }
        return new TreeBranch(growTree(left, maxTreeHeight, numFeatures, currTreeHeight + 1),
                growTree(right, maxTreeHeight, numFeatures, currTreeHeight + 1),
                splitPoint,
                feature);
    }
}
