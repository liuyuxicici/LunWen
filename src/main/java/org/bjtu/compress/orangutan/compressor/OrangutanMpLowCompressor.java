package org.bjtu.compress.orangutan.compressor;

import org.bjtu.compress.orangutan.utils.OrangutanUtils;
import org.urbcomp.startdb.compress.elf.compressor.ICompressor;
import org.bjtu.compress.orangutan.xorcompressor.OrangutanMpLowXorComp;

public class OrangutanMpLowCompressor implements ICompressor {
    private final OrangutanMpLowXorComp xorCompressor;

    private final int reservedDpNumber;

    public OrangutanMpLowCompressor(int dp) {
        xorCompressor = new OrangutanMpLowXorComp();
        this.reservedDpNumber = OrangutanUtils.getReservedDpNumber(dp);
    }

    public void addValue(double v) {
        long vLong = Double.doubleToRawLongBits(v);
        long vPrimeLong = vLong;

        int e = ((int) (vLong >> 52)) & 0x7ff;
        int reservedBits = reservedDpNumber + e - 1023;

        if (reservedBits < 52) {
            int eraseBits = 52 - reservedBits;
            long mask = 0xffffffffffffffffL << eraseBits;
            vPrimeLong = mask & vLong;
        }

        xorCompressor.addValue(vPrimeLong);
    }

    public int getSize() {
        return xorCompressor.getSize();
    }

    public byte[] getBytes() {
        return xorCompressor.getOut();
    }

    public void close() {
        xorCompressor.close();
    }
}
