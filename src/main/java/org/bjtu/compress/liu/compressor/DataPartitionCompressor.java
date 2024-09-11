package org.bjtu.compress.liu.compressor;

import gr.aueb.delorean.chimp.OutputBitStream;
import org.bjtu.compress.huffman.HuffmanCoding;
import org.bjtu.compress.liu.entity.DecimalSeries;
import org.bjtu.compress.liu.utils.DataCompressUtils;

import java.util.List;

/**
 * @description: 数据列式分区压缩
 * @author：lyx
 * @date: 2024/9/9
 */
public class DataPartitionCompressor extends AbstractDataPartitionCompressor {

    HuffmanCoding huffmanCoding = new HuffmanCoding();

    public DataPartitionCompressor(int blockSize, int patchSize) {
        size = 0;
        out = new OutputBitStream(
                new byte[20000]);
        this.blockSize = blockSize;
        this.patchSize = patchSize;
    }


    @Override
    public int signCompress(boolean[] sign) {
        int dataSize = sign.length;
        int thisSize = 0;
        // 符号位存储
        for (int i = 0; i < dataSize; i += patchSize) {
            int signsOfPatch = 0;
            for (int pIdx = 0; pIdx < patchSize; pIdx++) {
                signsOfPatch = (signsOfPatch << 1) ^ (i + pIdx < dataSize && sign[i + pIdx] ? 0x1 : 0x0); // 不够一个patch用0补充
            }
            if (signsOfPatch == 0) {
                out.writeBit(false);
                thisSize++;
            } else {
                out.writeBit(true);
                out.writeInt(signsOfPatch, patchSize);
                thisSize += patchSize + 1;
            }
        }
        return thisSize;
    }

    @Override
    public int dataPartitionCompress(long[][] partition) {
        int dataSize = partition[0].length;
        int partSize = partition.length;

        int patchNum = dataSize % patchSize == 0 ? dataSize / patchSize : (dataSize / patchSize + 1);
        int[][] fixNumLen = new int[partSize][patchNum];
        int[] fixNumFrequencies = new int[11];

        int thisSize = 0;
        for (int i = 0; i < partSize; i++) {
            for (int j = dataSize - 1; j >= 1; j--) {
                partition[i][j] ^= partition[i][j - 1];
                fixNumLen[i][j / patchSize] = Math.max(fixNumLen[i][j / patchSize], DataCompressUtils.getBitNum(partition[i][j]));
                if (j / patchSize == 0) {
                    fixNumFrequencies[fixNumLen[i][j / patchSize]]++;
                }
            }
        }
//        huffmanCoding.buildTree(fixNumFrequencies);
        for (int i = 0; i < partSize; i++) {
            for (int j = 0; j < dataSize; j++) {
//                out.writeLong(partition[i][j], fixNumLen[i][j / patchSize]);
//                thisSize += fixNumLen[i][j / patchSize];
//                thisSize += 4;
//                thisSize++;
                thisSize += huffmanCoding.symbolToCode.getOrDefault(fixNumLen[i][j / patchSize], "").length();
                if (fixNumLen[i][j / patchSize] != 0) {
//                    thisSize += 4;
                    thisSize += fixNumLen[i][j / patchSize];
                }
            }
        }


        System.out.println(thisSize);
        return thisSize;
    }

    @Override
    public int excDataCompress(DecimalSeries excData, List<Integer> excPosition) {
        return 0;
    }
}
