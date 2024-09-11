package org.bjtu.compress.liu.compressor;

import org.bjtu.compress.liu.entity.DecimalSeries;
import org.bjtu.compress.liu.utils.DataProcessUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author：lyx
 * @date: 2024/8/27
 */
public abstract class AbstractDataPartitionCompressor extends DataCompressorSupport implements ICompressor {


    public void doCompress(DecimalSeries data) {
        // 1、加载数据
        long[] validData = data.getValidData();
        double[][] validDigitsLoc = data.getValidDigitsLoc();
        boolean[] signs = data.getSigns();
        // 2、数据预处理——异常点检测和数据对齐
        DecimalSeries excData = new DecimalSeries(data.getSize() / 2);
        List<Integer> excPosition = new ArrayList<>();
        DecimalSeries normalData = DataProcessUtils.handleExcData(data, excData, excPosition);
//        DecimalSeries normalData = data;
        // 3、数据处理——数据分列
        long[][] partition = DataProcessUtils.handleDataPartition(normalData.getDigits(), normalData.getValidDigitCount());
        // 4.符号位压缩、正常数据分列压缩
        size += signCompress(signs);
        size += dataPartitionCompress(partition);
        // 5.异常数据压缩
        excDataCompress(excData, excPosition);
        size += excData.getSize() * 64;
        // 6.向输出流输出结束符号，关闭输出流
        close();
    }

    /**
     * 符号位压缩
     *
     * @param sign
     * @return 返回压缩后的比特数
     */
    public abstract int signCompress(boolean[] sign);

    /**
     * 数据分列压缩
     *
     * @param partition
     * @return 返回压缩的比特数
     */
    public abstract int dataPartitionCompress(long[][] partition);

    /**
     * 异常数据压缩
     *
     * @param excData
     * @param excPosition
     * @return 返回压缩的比特数
     */
    public abstract int excDataCompress(DecimalSeries excData, List<Integer> excPosition);

    private void close() {
        out.writeInt(0, 32);
        out.flush();
    }

}
