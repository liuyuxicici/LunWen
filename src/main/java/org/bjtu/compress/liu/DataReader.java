package org.bjtu.compress.liu;


import org.bjtu.compress.liu.entity.Decimal;
import org.bjtu.compress.liu.entity.DecimalSeries;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 读取数据
 * @author：lyx
 * @date: 2024/4/28
 */
public class DataReader {

    private BufferedReader bufferedReader;


    private final int blockSize;

    private final int patchSize;


    public static final int DEFAULT_BLOCK_SIZE = 1024;
    public static final int DEFAULT_PATCH_SIZE = 64;


    public DataReader(Reader in, int blockSize, int patchSize) {
        bufferedReader = new BufferedReader(in);
        this.blockSize = blockSize;
        this.patchSize = patchSize;
    }

    public DataReader(String filePath, int blockSize, int patchSize) throws FileNotFoundException {
        this(new FileReader(filePath), blockSize, patchSize);
    }

    public DataReader(String filePath) throws FileNotFoundException {
        this(filePath, DEFAULT_BLOCK_SIZE, DEFAULT_PATCH_SIZE);
    }


    /**
     * 读取下一块浮点数数据，分别存储整数部分和小数部分
     *
     * @return
     */
    public DecimalSeries nextBlock2Decimals() {
        DecimalSeries decimalSeries = new DecimalSeries(blockSize);
        int counter = 0;
        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    decimalSeries.addValue(new Decimal(line));
                    counter++;
                    if (counter == blockSize) {
                        return decimalSeries;
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 读取原始数据
     *
     * @return 返回value数组
     * @throws IOException
     */
    public double[] nextBlock2Double() throws IOException {
        double[] values = new double[blockSize];
        int counter = 0;
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    values[counter++] = Double.parseDouble(line);
                    if (counter == blockSize) {
                        return values;
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
