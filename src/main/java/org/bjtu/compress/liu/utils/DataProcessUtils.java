package org.bjtu.compress.liu.utils;

import org.bjtu.compress.liu.entity.Decimal;
import org.bjtu.compress.liu.entity.DecimalSeries;
import org.bjtu.compress.liu.iforest.IForest;
import org.bjtu.compress.liu.iforest.IsoForest;
import org.ejml.data.DenseMatrix64F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @description: 数据处理工具类
 * @author：lyx
 * @date: 2024/9/9
 */
public class DataProcessUtils {

    public static final double[] i_F10 = {1.0, 0.1, 0.01, 0.001, 0.0001, 0.00001, 0.000001, 0.0000001};
    public static final long[] F10 = {1l, 10l, 100l, 1000l, 10000l, 100000l, 1000000l, 10000000l, 100000000l, 1000000000l,
            10000000000l, 100000000000l, 1000000000000l, 10000000000000l, 100000000000000l, 1000000000000000l, 10000000000000000l,
            100000000000000000l, 1000000000000000000l};

    /**
     * 异常数据检测、正常数据对其
     *
     * @param data        有效数据
     * @param excData     异常数据
     * @param excPosition 异常数据索引
     * @return 返回正常数据序列对其后需要记录的最低有效位和最高有效位的位置
     */
    public static DecimalSeries handleExcData(DecimalSeries data, DecimalSeries excData, List<Integer> excPosition) {
        int size = data.getSize();
        Decimal[] decimals = data.getDecimals();
        double[][] validDigitsLoc = data.getValidDigitsLoc();
        IsoForest isoForest = new IsoForest();
        IForest forest = isoForest.train(validDigitsLoc);
        int[] predict = new int[size];
        int execCount = 0;
        DecimalSeries normalDataSeries = new DecimalSeries(data.getSize());

        // 记录正常数据中最低位的位置
        int lowestValidDigitLoc = Integer.MAX_VALUE;
        // 记录正常数据中最高位的位置
        int highestValidDigitLoc = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            predict[i] = forest.predict(new DenseMatrix64F(new double[][]{{validDigitsLoc[i][0], validDigitsLoc[i][1]}}));
            // 异常数据
            if (predict[i] == -1) {
                excPosition.add(i);
                excData.addValue(decimals[i]);
                execCount++;
            } else {
                // 正常数据
//                normalData.add(data[i]);
                normalDataSeries.addValue(decimals[i]);
                lowestValidDigitLoc = Math.min(lowestValidDigitLoc, (int) validDigitsLoc[i][1]);
                highestValidDigitLoc = Math.max(highestValidDigitLoc, (int) validDigitsLoc[i][0]);
            }
        }

        // 数据对齐
//        for (int i = 0; i < normalData.size(); i++) {
//            normalData.set(i, normalData.get(i) * (long) Math.pow(10, validDigitsLoc[i][1] - lowestValidDigitLoc));
//        }

//        return new int[]{highestValidDigitLoc, lowestValidDigitLoc};
        return normalDataSeries;
    }

    /**
     * 有效数据分区
     *
     * @param data           原始有效数据
     * @param validDigitsNum 数据对齐后有效数据的位数
     * @return 分区结果
     */
    public static long[][] handleDataPartition(int[][] data, int validDigitsNum) {
        int size = data[0].length;
        List<Integer> partSizes = defaultDataPartition(validDigitsNum);

        long[][] partition = new long[partSizes.size()][size];

//        int colsCount = 0;
//        for (int i = partSizes.size() - 1; i >= 0; i--) {
//            partition[i] = getDigitsPartition(data, colsCount, colsCount + partSizes.get(i) - 1);
//            colsCount += partSizes.get(i);
//        }

        for (int i = 0; i < size; i++) {
            int digitLoc = 0;
            for (int j = 0; j < partSizes.size(); j++) {
                for (int k = 0; k < partSizes.get(j); k++) {
                    partition[j][i] = partition[j][i] * 10 + data[digitLoc++][i];
                }
            }
        }

        return partition;
    }

    /**
     * 默认分区规则
     *
     * @param validCols
     * @return
     */
    public static List<Integer> defaultDataPartition(int validCols) {
        int colCount = 0;
        List<Integer> partition = new ArrayList<>();
        while (validCols >= 3) {
            partition.add(3);
            validCols -= 3;
        }
        if (validCols > 0) {
            partition.add(validCols);
        }

        return partition;
    }

    /**
     * 数据分区
     *
     * @param data
     * @param validCols
     * @return
     */
    public static List<Integer> dataPartition(long[] data, int validCols) {
        int size = data.length;

        // 预计算区间0到i的有效数字
//        long[][] dataPtr = new long[validCols][size];
//        for (int i = 0; i < validCols; i++) {
//            for (int j = 0; j < size; j++) {
//                dataPtr[j][i] = data[j] % (long)Math.pow(10, i + 1);
//            }
//        }

        int maxMergeCols = 8;
        double[] minExcBits = new double[validCols];
        Arrays.fill(minExcBits, Double.MAX_VALUE);
        int[] subareaRoot = new int[validCols];
        long[] firstValidDigits = getValidDigits(data, 0, 0);
        double firstValidDigitsMaxBits = calculateXorAndGetAvgBits(firstValidDigits);
        minExcBits[0] = firstValidDigitsMaxBits;
        subareaRoot[0] = 0;
        for (int i = 1; i < validCols; i++) {
            int leftPtr = Math.max(i - maxMergeCols + 1, 0);
            for (int j = leftPtr; j <= i; j++) {
                double mergedAvgBits = calculateXorAndGetAvgBits(getValidDigits(data, j, i));

                if (j >= 1 && minExcBits[j - 1] + mergedAvgBits < minExcBits[i]) {
                    minExcBits[i] = minExcBits[j - 1] + mergedAvgBits;
                    subareaRoot[i] = j;
                }
                if (j == 0 && mergedAvgBits < minExcBits[i]) {
                    minExcBits[i] = mergedAvgBits;
                    subareaRoot[i] = 0;
                }

            }
        }

        List<Integer> subArea = new ArrayList<>();
        int index = validCols - 1;
        while (index >= 0) {
            subArea.add(index - subareaRoot[index] + 1);
            index = subareaRoot[index] - 1;
        }

        return subArea;
    }

    private static long[] getValidDigits(long[] data, int minValidLoc, int maxValidLoc) {
        long[] valid = new long[data.length];
        int validDigits = maxValidLoc - minValidLoc + 1;
        for (int i = 0; i < data.length; i++) {
            valid[i] = (data[i] / (long) Math.pow(10, minValidLoc)) % (long) Math.pow(10, validDigits);
        }
        return valid;
    }

    /**
     * 数组在区间内依次与前一个数进行xor, 并返回平均每个数表示所需的比特数
     *
     * @param mergingDigits
     * @return xor结果的最大值
     */
    private static double calculateXorAndGetAvgBits(long[] mergingDigits) {
        double avgBit = 0;
        for (int i = mergingDigits.length - 1; i >= 1; i--) {
            mergingDigits[i] ^= mergingDigits[i - 1];
            avgBit += getBitNum(mergingDigits[i]);
        }
        return avgBit / mergingDigits.length;
    }


    public static int getBitNum(long num) {
        return 64 - Long.numberOfLeadingZeros(num);
    }
}
