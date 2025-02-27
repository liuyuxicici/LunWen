package org.urbcomp.startdb.compress.elf.compressor;

import fi.iki.yak.ts.compression.gorilla.CompressorOS;

import java.util.HashMap;
import java.util.Map;

public class GorillaCompressorOS implements ICompressor {
    private final CompressorOS gorilla;


    public GorillaCompressorOS() {
        this.gorilla = new CompressorOS();
    }

    @Override
    public void addValue(double v) {
        this.gorilla.addValue(v);
    }

    @Override
    public int getSize() {
        return this.gorilla.getSize();
    }

    @Override
    public byte[] getBytes() {
        return this.gorilla.getOutputStream().getBuffer();
    }

    @Override
    public void close() {
        this.gorilla.close();
    }
    
    @Override
    public Map<Integer, Integer> getMap() {
        return gorilla.getMap();
    }
}
