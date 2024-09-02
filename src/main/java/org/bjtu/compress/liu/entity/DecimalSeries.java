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
            return null;
        }
        int[][] digits = new int[firstDigitIndex - lastDigitIndex + 1][size];

        for (int i = 0; i < size; i++) {
            long sigData = decimals[i].getSigData();

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

    public int getDigitCnt() {
        return firstDigitIndex - lastDigitIndex + 1;
    }

}
