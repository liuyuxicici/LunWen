package org.bjtu.compress.liu.utils;

import org.apache.commons.lang.StringUtils;
import org.bjtu.compress.liu.precision.ryu.RyuDouble;
import sun.misc.DoubleConsts;

/**
 * @description:
 * @author：lyx
 * @date: 2024/9/3
 */
public class DataUtils {

    private final static double[] map10iP =
            new double[]{1.0, 1.0E1, 1.0E2, 1.0E3, 1.0E4, 1.0E5, 1.0E6, 1.0E7,
                    1.0E8, 1.0E9, 1.0E10, 1.0E11, 1.0E12, 1.0E13, 1.0E14,
                    1.0E15, 1.0E16, 1.0E17, 1.0E18, 1.0E19, 1.0E20};

    private final static double[] map10iN =
            new double[]{1.0, 1.0E-1, 1.0E-2, 1.0E-3, 1.0E-4, 1.0E-5, 1.0E-6, 1.0E-7,
                    1.0E-8, 1.0E-9, 1.0E-10, 1.0E-11, 1.0E-12, 1.0E-13, 1.0E-14,
                    1.0E-15, 1.0E-16, 1.0E-17, 1.0E-18, 1.0E-19, 1.0E-20};

    private final static double LG_2 = Math.log10(2);

    private final static double LOG_2_10 = Math.log(10) / Math.log(2);

    private final static int[] f =
            new int[]{0, 4, 7, 10, 14, 17, 20, 24, 27, 30, 34, 37, 40, 44, 47, 50, 54, 57,
                    60, 64, 67};

    private final static double[] factor_neg =
            new double[]{1, 16, 128, 1024};

    private final static double[] factor_pos =
            new double[]{1.0d, 0.5d, 0.25d, 0.125d, 0.0625d, 0.03125d, 0.015625d, 0.0078125d};

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

    private static double get10iP(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("The argument should be greater than 0");
        }
        if (i >= map10iP.length) {
            return Double.parseDouble("1.0E" + i);
        } else {
            return map10iP[i];
        }
    }

    public static double get10iN(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("The argument should be greater than 0");
        }
        if (i >= map10iN.length) {
            return Double.parseDouble("1.0E-" + i);
        } else {
            return map10iN[i];
        }
    }

    public static int getFAlpha(int alpha) {
        if (alpha < 0) {
            throw new IllegalArgumentException("The argument should be greater than 0");
        }
        if (alpha >= f.length) {
            return (int) Math.ceil(alpha * LOG_2_10);
        } else {
            return f[alpha];
        }
    }

    public static double getFactor(int precision) {

        if (precision < 0) {
            precision = -precision;
        }
        return get10iP(precision);
//        if (precision < 0) {
//            precision = -precision;
//            if (precision >= factor_neg.length) {
//                return Math.pow(2, getFAlpha(precision));
//            } else {
//                return factor_neg[precision];
//            }
//        } else {
//            if (precision >= factor_pos.length) {
//                return Math.pow(0.5, precision);
//            } else {
//                return factor_pos[precision];
//            }
//        }

    }

    public static double getRecoveryValue(double value, int precision) {
        double factor = getFactor(precision);
        String l1 = Long.toBinaryString(Double.doubleToRawLongBits(value));
        double rVal = (long) (value * factor) / factor;
        String l2 = Long.toBinaryString(Double.doubleToLongBits(rVal));
        if (precision < 0 && rVal != value) {
            double iP = get10iP(-precision);
            double v = (long) (rVal * iP) / iP;
            rVal = v + get10iN(-precision);
        }

        return rVal;

//        double rVal = 0.0d;
//        double iP = 0.0d;
//        if (precision < 0) {
//            iP = get10iP(-precision);
//            rVal = (long) (value * iP) / iP;
//        } else {
//            iP = get10iP(precision);
//            rVal = (long) (value / iP) * iP;
//        }
//        return rVal;
    }

    public static boolean isPrecision(double d, int referPrecision) {

        return getRecoveryValue(d, referPrecision) == d;
    }

    /**
     * 精度计算
     *
     * @param v 双精度浮点数
     * @return 浮点数精度
     */
    public static int getPrecision(double v) {

//        long biasedBit = ((DoubleConsts.EXP_BIT_MASK & Double.doubleToRawLongBits(d)) >> 52);
//        int mostSP = (int) Math.floor((biasedBit - 1022) * LG_2);

//        int left = mostSP - 17, right = mostSP, mid = (left + right) >> 1;
//        while (left <= right) {
//            if (isPrecision(d, mid)) {
//                left = mid + 1;
//            } else {
//                right = mid - 1;
//            }
//            mid = (left + right) >> 1;
//        }
//        return left - 1 >= mostSP - 17 ? left - 1 : mostSP - 17;

        int precision = 0;
        String doubleStr = RyuDouble.doubleToString(v);
        if (StringUtils.contains(doubleStr, "E")) {
            String[] doubleAndExp = doubleStr.split("E");
            precision = doubleAndExp[0].length() - 2 + Integer.parseInt(doubleAndExp[1]);
        } else {
            String[] integerAndFraction = doubleStr.split("\\.");
            if (Integer.parseInt(integerAndFraction[1]) == 0) {
                precision = 0;
            } else {
                precision -= integerAndFraction[1].length();
            }

        }
        return precision;
    }

    public static long[] getIntegerAndPrecision(double v, int referPrecision) {
        long[] integerAndPrecision = new long[2];
        int precision = 0;
        long integer = 0;
        String doubleStr = RyuDouble.doubleToString(v);
        if (StringUtils.contains(doubleStr, "E")) {
            String[] doubleAndExp = doubleStr.split("E");
            precision = doubleAndExp[0].length() - 2 - Integer.parseInt(doubleAndExp[1]);
            integer = Long.parseLong(doubleAndExp[0].replaceAll("\\.", ""));
        } else {
            String[] integerAndFraction = doubleStr.split("\\.");

            if (Long.parseLong(integerAndFraction[1]) == 0) {
                precision = 0;
                integer = Long.parseLong(integerAndFraction[0]);
            } else {
                precision = integerAndFraction[1].length();
                integer = Long.parseLong(doubleStr.replaceAll("\\.", ""));
            }
        }
        long scalar = (long) Math.pow(10, referPrecision - precision);
        if (precision < referPrecision && (integer * scalar) / scalar == integer) { // 确保原始数据不越界

            integerAndPrecision[0] = integer * scalar;
            integerAndPrecision[1] = referPrecision;
        } else {
            integerAndPrecision[0] = integer;
            integerAndPrecision[1] = precision;
        }
        return integerAndPrecision;
    }

    public static long[] getIntegerAndPrecisionOriginal(double v, int referPrecision) {
        long[] integerAndPrecision = new long[2];
        int precision = 0;
        long integer = 0;
        String doubleStr = Double.toString(v);
        if (StringUtils.contains(doubleStr, "E")) {
            String[] doubleAndExp = doubleStr.split("E");
            precision = doubleAndExp[0].length() - 2 + Integer.parseInt(doubleAndExp[1]);
            integer = Long.parseLong(doubleAndExp[0].replace(".", ""));
        } else {
            String[] integerAndFraction = doubleStr.split("\\.");
            integer = Long.parseLong(doubleStr.replaceAll("\\.", ""));
            if (Long.parseLong(integerAndFraction[1]) == 0) {
                precision = 0;
            } else {
                precision -= integerAndFraction[1].length();
            }
        }
        double scalar = Math.pow(10, precision - referPrecision);
        if (precision > referPrecision && (long) (integer * scalar) / scalar == integer) {
            integerAndPrecision[0] = (long) (integer * scalar);
            integerAndPrecision[1] = referPrecision;
        } else {
            integerAndPrecision[0] = integer;
            integerAndPrecision[1] = precision;
        }
        return integerAndPrecision;
    }

}
