package org.bjtu.compress.orangutan;

import org.bjtu.compress.orangutan.compressor.*;
import org.bjtu.compress.orangutan.decompressor.*;
import org.urbcomp.startdb.compress.elf.compressor.ICompressor;
import org.urbcomp.startdb.compress.elf.decompressor.IDecompressor;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        double x = 64.21;
        double y = 64.25;
        long Lx = Double.doubleToRawLongBits(x);
        long Ly = Double.doubleToRawLongBits(y);
        long Lz = Lx & Ly;
        double z = Double.longBitsToDouble(Lz);
        System.out.println(z);
    }


}
