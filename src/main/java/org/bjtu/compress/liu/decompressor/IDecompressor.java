package org.bjtu.compress.liu.decompressor;

import java.util.List;

/**
 * @description: 解压缩接口
 * @author：lyx
 * @date: 2024/9/15
 */
public interface IDecompressor {
    List<Double> decompress(List<Integer> bias);
}
