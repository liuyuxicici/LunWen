package org.bjtu.compress.liu.entity;

import org.apache.commons.lang.StringUtils;

/**
 * @description: 浮点数数据表示类
 * @author：lyx
 * @date: 2024/8/9
 */
public class Decimal {

    /**
     * 数据的符号串表示
     */
    private String decimalStr;

    /**
     * 符号位表示
     */
    private boolean isNegative;

    /**
     * 最高有效位索引
     */
    private int firstSigDigitIndex;

    /**
     * 最低有效位索引
     */
    private int lastSigDigitIndex;

    /**
     * 有效数字
     */
    private long sigData;

    public Decimal(String decimalStr) throws NumberFormatException {
        this.decimalStr = decimalStr;
        if (StringUtils.isNotBlank(decimalStr)) {


            int decimalIndex = decimalStr.contains(".") ? decimalStr.indexOf('.') : decimalStr.length();
            decimalStr = decimalStr.replace(".", "");

            // 去除数字前的0
            int index = 0;
            while (index < decimalStr.length() &&
                    (decimalStr.charAt(index) == '0' || decimalStr.charAt(index) == '-' || decimalStr.charAt(index) == '+')) {
                index++;
            }

            firstSigDigitIndex = decimalIndex - index;
            lastSigDigitIndex = decimalIndex - decimalStr.length() + 1;

            long num = Long.parseLong(decimalStr);
            if (num < 0) {
                isNegative = true;
            } else {
                isNegative = false;
            }
            sigData = Math.abs(num);
        }

    }


    public String getDecimalStr() {
        return decimalStr;
    }

    public void setDecimalStr(String decimalStr) {
        this.decimalStr = decimalStr;
    }

    public int getFirstSigDigitIndex() {
        return firstSigDigitIndex;
    }

    public int getLastSigDigitIndex() {
        return lastSigDigitIndex;
    }

    public long getSigData() {
        return sigData;
    }

    public boolean isNegative() {
        return isNegative;
    }

    private static String[] splitStringByDecimal(String input) {
        if (input.contains(".")) {
            return input.split("\\.");
        } else {
            return new String[]{input, "0"};
        }
    }

    private String reverseString(String str) {
        return new StringBuilder(str).reverse().toString();
    }
}
