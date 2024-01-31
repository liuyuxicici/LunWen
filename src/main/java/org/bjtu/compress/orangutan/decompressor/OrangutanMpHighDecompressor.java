package org.bjtu.compress.orangutan.decompressor;


import org.bjtu.compress.orangutan.xordecompressor.OrangutanMpHighXorDecomp;
import org.urbcomp.startdb.compress.elf.decompressor.IDecompressor;

import java.util.ArrayList;
import java.util.List;

public class OrangutanMpHighDecompressor implements IDecompressor {
    private final OrangutanMpHighXorDecomp xorDecompressor;
    private final long base;

    public OrangutanMpHighDecompressor(byte[] bytes, int dp) {
        long temp = 1;
        for (int i = 0; i < dp; i++)
            temp *= 10;
        this.base = temp;
        xorDecompressor = new OrangutanMpHighXorDecomp(bytes);
    }

    public List<Double> decompress() {
        List<Double> list = new ArrayList<>(1024);
        Double value = xorDecompressor.readValue();
        while (value != null) {
            if (value != 0.0) {
                value = Math.round(value * base) * 1.0 / base;
            }
            list.add(value);
            value = xorDecompressor.readValue();
        }
        return list;
    }
}
