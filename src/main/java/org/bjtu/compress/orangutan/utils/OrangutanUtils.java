package org.bjtu.compress.orangutan.utils;


import javax.sound.midi.Soundbank;
import java.util.HashMap;
import java.util.Map;

public class OrangutanUtils {
    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    public static int getReservedDpNumber(int dp) {
        return (int) Math.abs(Math.floor(log2(0.5 / Math.pow(10, dp))));
    }

    public static int getBias(double[] values, int dp) {
        final int reservedDpNumber = getReservedDpNumber(dp);
        int[] nums = new int[65];

        long storedValue = Double.doubleToRawLongBits(values[0]);
        //统计centerBits的数量分布
        for (int i = 1; i < values.length; i++) {
            long zeroedLong = Zeroing(values[i], reservedDpNumber);
            long xor = zeroedLong ^ storedValue;

            int leadingZeros = Long.numberOfLeadingZeros(xor);
            int trailingZeros = Long.numberOfTrailingZeros(xor);
            if (leadingZeros == 64) {
                continue;
            }
            nums[64 - leadingZeros - trailingZeros]++;
            storedValue = zeroedLong;
        }
        //滑动窗口计算最优起点
        int total = 0;
        for (int i = 0; i < 8; i++) {
            total += nums[i];
        }
        int maxTotal = total;
        int maxIndex = 0;
        for (int i = 1; i < 58; i++) {
            total += -nums[i - 1] + nums[i + 7];
            if (total > maxTotal) {
                maxTotal = total;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public static long Zeroing(double v, int reservedDpNumber) {
        long vLong = Double.doubleToRawLongBits(v);
        long vPrimeLong = vLong;

        int e = ((int) (vLong >> 52)) & 0x7ff;
        int reservedBits = reservedDpNumber + e - 1023;

        if (reservedBits < 52) {
            int eraseBits = 52 - reservedBits;
            long mask = 0xffffffffffffffffL << eraseBits;
            vPrimeLong = mask & vLong;
        }
        return vPrimeLong;
    }
}
