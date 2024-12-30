package org.bjtu.compress.liu.entity;

import org.apache.commons.lang.StringUtils;

/**
 * @description: 浮点数数据表示类
 * @author：lyx
 * @date: 2024/8/9
 */
public class Decimal {

    private double dataValue;


    /**
     * 数据的符号串表示
     */
    private String decimalStr;

    /**
     * 整数部分数值
     */
    private long integerValue;

    /**
     * 小数部分数值
     */
    private long fractionValue;

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
    private long validData;

    public Decimal(String decimalStr) throws NumberFormatException {
        this.decimalStr = decimalStr;
        this.dataValue = Double.parseDouble(decimalStr);
        String[] splitStringByDecimal = splitStringByDecimal(decimalStr);
        integerValue = Math.abs(Long.parseLong(splitStringByDecimal[0]));
        fractionValue = Long.parseLong(splitStringByDecimal[1]);

        if (StringUtils.isNotBlank(decimalStr)) {


            int decimalIndex = decimalStr.contains(".") ? decimalStr.indexOf('.') : decimalStr.length();
            decimalStr = decimalStr.replace(".", "");

            // 去除数字前的0
            int index = 0;
            while (index < decimalStr.length() &&
                    (decimalStr.charAt(index) == '0' || decimalStr.charAt(index) == '-' || decimalStr.charAt(index) == '+')) {
                index++;
            }

            firstSigDigitIndex = decimalIndex - index - 1;
            lastSigDigitIndex = decimalIndex - decimalStr.length();

            long num = Long.parseLong(decimalStr);
            if (num < 0) {
                isNegative = true;
            } else {
                isNegative = false;
            }
            validData = num;
        }

    }

    public Decimal(boolean isNegative, long integerValue, long fractionValue, int leastValidPos) {
        StringBuffer value = new StringBuffer();
        if (isNegative) {
            value.append("-");
        }
        value.append(integerValue);
        value.append(".");
        if (leastValidPos >= 0) {

        } else {
            int fractionValueDigitLen = Long.toString(fractionValue).length();
            while (fractionValueDigitLen < -leastValidPos) {
                value.append(0);
                fractionValueDigitLen++;
            }
            value.append(fractionValue);
        }

        dataValue = Double.parseDouble(value.toString());
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

    public long getValidData() {
        return validData;
    }

    public boolean isNegative() {
        return isNegative;
    }

    public double getDataValue() {
        return dataValue;
    }

    public long getIntegerValue() {
        return integerValue;
    }

    public long getFractionValue() {
        return fractionValue;
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
