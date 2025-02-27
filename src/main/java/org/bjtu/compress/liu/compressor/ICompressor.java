package org.bjtu.compress.liu.compressor;

/**
 * @description: 压缩器接口
 * @author：lyx
 * @date: 2024/9/9
 */
public interface ICompressor {
    int getSize();

    byte[] getBytes();

    void close();

    default String getKey() {
        return getClass().getSimpleName();
    }
}
