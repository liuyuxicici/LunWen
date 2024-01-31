package org.bjtu.compress.orangutan.decompressor;

import org.bjtu.compress.orangutan.xordecompressor.OrangutanXORDecompressor;
import org.urbcomp.startdb.compress.elf.decompressor.IDecompressor;

import java.util.ArrayList;
import java.util.List;

public class OrangutanDecompressor implements IDecompressor {
    private final OrangutanXORDecompressor xorDecompressor;
    private final long base;
    private final int dp;

    public OrangutanDecompressor(byte[] bytes, int dp) {
        long temp = 1;
        for (int i = 0; i < dp; i++)
            temp *= 10;
        this.base = temp;
        this.dp = dp;
        xorDecompressor = new OrangutanXORDecompressor(bytes);
    }

    public List<Double> decompress() {
        List<Double> list = new ArrayList<>(1024);
        Double value = xorDecompressor.readValue();
        while (value != null) {
            if (dp < 16) {
                value = Math.round(value * base) * 1.0 / base;
            }
            list.add(value);
            value = xorDecompressor.readValue();
        }
        return list;
    }
}
