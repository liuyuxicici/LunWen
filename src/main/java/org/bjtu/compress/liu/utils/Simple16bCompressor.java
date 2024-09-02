package org.bjtu.compress.liu.utils;

import gr.aueb.delorean.chimp.OutputBitStream;

/**
 * @description:
 * @author：lyx
 * @date: 2024/8/22
 */
public class Simple16bCompressor {

    public static final short[][] selector = {
            {0, 0, 0, 0, 0, 0, 0, 0},   //0  0bit
            {1, 1, 1, 1, 1, 1, 1, 1},   //1  1bit*8   =8
            {2, 2, 2, 2, 2, 2, 2, 2},   //2  2bit*8   =16
            {2, 2, 2, 2, 2, 2, 3, 3},   //3  2bit*6+3bit*2  =18
            {3, 3, 2, 2, 2, 2, 2, 2},   //4  3bit*2+2bit*6  =18
            {2, 2, 2, 2, 3, 3, 3, 3},   //5  2bit*4+3bit*4  =20
            {2, 2, 2, 2, 2, 2, 4, 4},   //6  2bit*6+4bit*2  =20
            {4, 4, 2, 2, 2, 2, 2, 2},   //7  4bit*2+2bit*6  =20
            {2, 2, 2, 2, 4, 4, 4, 4},   //8  2bit*4+4bit*4  =24
            {3, 3, 3, 3, 3, 3, 3, 3},   //9  3bit*8   =24
            {4, 4, 4, 4, 2, 2, 2, 2},   //10 4bit*4+2bit*4  =24
            {3, 3, 3, 3, 3, 3, 4, 4},   //11 3bit*6+4bit*2  =26
            {4, 4, 3, 3, 3, 3, 3, 3},   //12 4bit*2+3bit*6  =26
            {3, 3, 3, 3, 4, 4, 4, 4},   //13 3bit*4+4bit*4  =28
            {4, 4, 4, 4, 3, 3, 3, 3},   //14 4bit*4+3bit*4  =28
            {4, 4, 4, 4, 4, 4, 4, 4}    //15 4bit*8
    };

    public static int simple16bCompress(int[] nums, int[] fixLen, int offset, OutputBitStream out) {

        if (offset >= nums.length) {
            return 0;
        }
        int selectorIdx = 0;
        for (int i = 0; i < 16; i++) {
            boolean thisSelector = true;
            for (int j = 0; j < 8 && offset + j < fixLen.length; j++) {
                if (fixLen[offset + j] > selector[i][j]) {
                    thisSelector = false;
                    break;
                }
            }
            if (thisSelector == true) {
                selectorIdx = i;
                break;
            }
        }
        int thisSize = 0;
        out.writeInt(selectorIdx, 4);
        thisSize += 4;

        for (int i = 0; i < 8; i++) {

            if (offset + i < nums.length) {
                out.writeInt(nums[offset + i], selector[selectorIdx][i]);
            } else {
                out.writeInt(0, selector[selectorIdx][i]); //不足一个patch用0补充；
            }
            thisSize += selector[selectorIdx][i];
        }
        return thisSize;
    }
}
