package org.bjtu.compress.orangutan.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OrangutanUtils {
    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    public static int getReservedDpNumber(int dp) {
        return (int) Math.abs(Math.floor(log2(0.5 / Math.pow(10, dp))));
    }

    public static double roundUp(double x, int dp) {

        return new BigDecimal(x).setScale(dp, RoundingMode.HALF_UP).doubleValue();
    }
}
