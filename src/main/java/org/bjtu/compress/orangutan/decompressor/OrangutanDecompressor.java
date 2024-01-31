package org.bjtu.compress.orangutan.decompressor;

import org.bjtu.compress.orangutan.xordecompressor.OrangutanXORDecompressor;
import org.urbcomp.startdb.compress.elf.decompressor.IDecompressor;

import java.util.ArrayList;
import java.util.List;

public class OrangutanDecompressor implements IDecompressor {
    private final OrangutanXORDecompressor xorDecompressor;
    private final long base;

    public OrangutanDecompressor(byte[] bytes, int dp) {
        long temp = 1;
        for (int i = 0; i < dp; i++)
            temp *= 10;
        this.base = temp;
        xorDecompressor = new OrangutanXORDecompressor(bytes);
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
