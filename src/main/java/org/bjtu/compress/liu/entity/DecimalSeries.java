package org.bjtu.compress.liu.entity;

/**
 * @description: 浮点数序列表示类
 * @author：lyx
 * @date: 2024/8/9
 */
public class DecimalSeries {

    private Decimal[] decimals;

    /**
     * 浮点数序列当前大小
     */
    private int size;

    /**
     * 容量
     */
    private int capacity;

    /**
     * 最高有效数字索引
     */
    private int firstDigitIndex;

    /**
     * 最低有效数字索引
     */
    private int lastDigitIndex = Integer.MAX_VALUE;

    public DecimalSeries(int capacity) {
        this.size = 0;
        this.capacity = capacity;

        this.decimals = new Decimal[capacity];
    }

    /**
     * 添加数据
     *
     * @param decimal
     * @return
     */
    public boolean addValue(Decimal decimal) {
        if (size >= capacity) {
            return false;
        }
        decimals[size] = decimal;
        size++;


        firstDigitIndex = Math.max(firstDigitIndex, decimal.getFirstSigDigitIndex());

        lastDigitIndex = Math.min(lastDigitIndex, decimal.getLastSigDigitIndex());

        return true;
    }

    public Decimal[] getDecimals() {
        return decimals;
    }

    public int getSize() {
        return size;
    }

    public int getCapacity() {
        return capacity;
    }


    public int getFirstDigitIndex() {
        return firstDigitIndex;
    }

    public int getLastDigitIndex() {
        return lastDigitIndex;
    }

    public int[][] getDigits() {
        if (size == 0) {
            return new int[0][];
        }
        int[][] digits = new int[firstDigitIndex - lastDigitIndex + 1][size];

        for (int i = 0; i < size; i++) {
            long sigData = decimals[i].getValidData();

            int digitIdx = firstDigitIndex - decimals[i].getLastSigDigitIndex();
            while (sigData > 0) {
                digits[digitIdx][i] = (int) (sigData % 10);
                digitIdx--;
                sigData /= 10;
            }
        }
        return digits;
    }


    public boolean[] getSigns() {
        boolean[] signs = new boolean[size];
        for (int i = 0; i < size; i++) {
            signs[i] = decimals[i].isNegative();
        }
        return signs;
    }

    public long[] getValidData() {
        if (size == 0) {
            return new long[0];
        }
        long[] validData = new long[size];

        for (int i = 0; i < size; i++) {
            validData[i] = decimals[i].getValidData();
        }
        return validData;
    }

    public String[] getBinaryValidData() {
        if (size == 0) {
            return new String[0];
        }
        String[] validData = new String[size];

        for (int i = 0; i < size; i++) {
            validData[i] = Long.toBinaryString(decimals[i].getValidData());

        }
        return validData;
    }

    public int[][] getValidDigitsLoc() {
        if (size == 0) {
            return new int[0][];
        }

        int[][] validDigitsLoc = new int[size][2];
        for (int i = 0; i < size; i++) {
            validDigitsLoc[i][0] = decimals[i].getFirstSigDigitIndex();
            validDigitsLoc[i][1] = decimals[i].getLastSigDigitIndex();
        }
        return validDigitsLoc;
    }

    public double[] getDoubleValues() {
        if (size == 0) {
            return new double[0];
        }
        double[] doubles = new double[size];
        for (int i = 0; i < size; i++) {
            doubles[i] = decimals[i].getDataValue();
        }
        return doubles;
    }

    public int getValidDigitCount() {
        return firstDigitIndex - lastDigitIndex + 1;
    }

}
