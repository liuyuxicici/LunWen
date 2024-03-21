package org.urbcomp.startdb.compress.elf.decompressor;

import gr.aueb.delorean.chimp.InputBitStream;
import org.urbcomp.startdb.compress.elf.xordecompressor.ElfXORDecompressor;
import org.urbcomp.startdb.compress.elf.xordecompressor.OranMpLowXorDecomp;

import java.io.IOException;

public class OranLowDecompressor extends AbstractElfDecompressor {
    private final OranMpLowXorDecomp xorDecompressor;

    public OranLowDecompressor(byte[] bytes) {
        xorDecompressor = new OranMpLowXorDecomp(bytes);
    }

    @Override
    protected Double xorDecompress() {
        return xorDecompressor.readValue();
    }

    @Override
    protected int readInt(int len) {
        InputBitStream in = xorDecompressor.getInputStream();
        try {
            return in.readInt(len);
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage());
        }
    }
}
