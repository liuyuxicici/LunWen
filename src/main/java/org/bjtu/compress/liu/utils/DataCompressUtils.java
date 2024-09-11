package org.bjtu.compress.liu.utils;

/**
 * @description:
 * @author：lyx
 * @date: 2024/9/3
 */
public class DataCompressUtils {


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

    public static double[] calculateColumnVariance(int[][] digits) {
        double[] vari = new double[digits.length];
        int idx = 0;
        for (int[] digit : digits) {
//            int[] delta = calculateDelta(digit);
            vari[idx++] = calculateVariance(digit);
        }

        return vari;
    }

    public static int getBitNum(long num) {
        return 64 - Long.numberOfLeadingZeros(num);
    }

}
