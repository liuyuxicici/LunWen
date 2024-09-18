package org.bjtu.compress.liu.utils;

/**
 * @description:
 * @author：lyx
 * @date: 2024/9/3
 */
public class DataUtils {


    /**
     * 按行计算序列的delta
     *
     * @param nums
     * @return
     */
    public static int[] calculateDelta(int[] nums) {
        if (nums.length == 0) {
            return new int[0];
        }

        int[] delta = new int[nums.length];

        for (int i = 1; i < nums.length; i++) {
            delta[i] = nums[i] - nums[i - 1];
        }
        return delta;
    }

    /**
     * 按行计算序列的方差
     *
     * @param nums
     * @return
     */
    public static double calculateVariance(int[] nums) {

        if (nums.length == 0) {
            return 0.0d;
        }

        double variance = 0.0d;
        double avg = 0.0d;
        for (int num : nums) {
            avg += num;
        }
        avg /= nums.length;

        for (int num : nums) {
            variance += Math.pow(num - avg, 2);
        }
        variance /= nums.length;
        return variance;
    }

    /**
     * 按行计算序列的变异系数（方差/平均）
     *
     * @param nums
     * @return
     */
    public static double calculateVariableCoefficient(int[] nums) {

        if (nums.length == 0) {
            return 0.0d;
        }

        double variCoeff = 0.0d;
        double avg = 0.0d;
        for (int num : nums) {
            avg += num;
        }
        avg /= nums.length;

        for (int num : nums) {
            variCoeff += Math.pow(num - avg, 2);
        }
        variCoeff /= nums.length;
        variCoeff /= avg;
        return variCoeff;
    }

    /**
     * 按行计算序列的变异系数（方差/平均）
     *
     * @param nums
     * @return
     */
    public static double calculateVariableCoefficient(long[] nums) {

        if (nums.length == 0) {
            return 0.0d;
        }

        double variCoeff = 0.0d;
        double avg = 0.0d;
        for (long num : nums) {
            avg += num;
        }
        avg /= nums.length;

        for (long num : nums) {
            variCoeff += Math.pow(num - avg, 2);
        }
        variCoeff /= nums.length;
        variCoeff /= avg;
        return variCoeff;
    }

    public static double[] calculateColumnVariance(int[][] digits) {
        double[] vari = new double[digits.length];
        int idx = 0;
        for (int[] digit : digits) {
//            int[] delta = calculateDelta(digit);
            vari[idx++] = calculateVariance(digit);
        }

        return vari;
    }

    /**
     * 计算一个数的二进制表示的1的个数
     *
     * @param num
     * @return
     */
    public static int get1sNum(long num) {
        int count = 0;//记录1的个数
        while (num != 0) {
            num = num & (num - 1);
            count++;
        }
        return count;
    }

    public static int getBitNum(long num) {
        return 64 - Long.numberOfLeadingZeros(num);
    }

    /**
     * 倒转数据中的有效比特位
     *
     * @param original
     * @return
     */
    public static int reverseValidBits(int original) {
        int reversed = 0;
        while (original != 0) {
            reversed <<= 1; // 将结果向左移动一位
            reversed |= (original & 1); // 将原始数据的最后一位添加到结果中
            original >>>= 1; // 将原始数据向右移动一位
        }
        return reversed;
    }

    /**
     * 倒转数据中有效比特位
     *
     * @param original
     * @return
     */
    public static long reverseValidBits(long original) {
        long reversed = 0;
        while (original != 0) {
            reversed <<= 1; // 将结果向左移动一位
            reversed |= (original & 1); // 将原始数据的最后一位添加到结果中
            original >>>= 1; // 将原始数据向右移动一位
        }
        return reversed;
    }

    /**
     * 倒转数据位
     *
     * @param original
     * @return
     */
    public static long reverseBits(long original) {
        long reversed = 0;
        for (int i = 0; i < 64; i++) {
            reversed <<= 1; // 将结果向左移动一位
            reversed |= (original & 1); // 将原始数据的最后一位添加到结果中
            original >>>= 1; // 将原始数据向右移动一位
        }
        return reversed;
    }


    /**
     * 倒转数据中位
     *
     * @param original
     * @return
     */
    public static long reverseLowerKBits(long original, int k) {
        long reversed = 0;
        for (int i = 0; i < k; i++) {
            reversed <<= 1; // 将结果向左移动一位
            reversed |= (original & 1); // 将原始数据的最后一位添加到结果中
            original >>>= 1; // 将原始数据向右移动一位
        }
        return reversed;
    }

    public static long getLowerKBits(long original, int k) {

        if (k == 0 || original == 0) {
            return 0;
        }
        long lowerKBits = 0;
        long kBitMask = (1L << k) - 1;


        return kBitMask & original;
    }

}
