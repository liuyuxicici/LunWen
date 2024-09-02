package org.bjtu.compress;

import com.github.kutschkem.fpc.FpcCompressor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.io.compress.brotli.BrotliCodec;
import org.apache.hadoop.hbase.io.compress.lz4.Lz4Codec;
import org.apache.hadoop.hbase.io.compress.xerial.SnappyCodec;
import org.apache.hadoop.hbase.io.compress.xz.LzmaCodec;
import org.apache.hadoop.hbase.io.compress.zstd.ZstdCodec;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.bjtu.compress.liu.DataReader;
import org.bjtu.compress.liu.compressor.DigitCompressor;
import org.bjtu.compress.liu.compressor.HundredDataCompressor;
import org.bjtu.compress.liu.decompressor.DigitDecompressor;
import org.bjtu.compress.liu.entity.DecimalSeries;
import org.bjtu.compress.oran.compressor.HuffCompressor;
import org.junit.jupiter.api.Test;
import org.urbcomp.startdb.compress.elf.compressor.*;
import org.urbcomp.startdb.compress.elf.decompressor.*;
import org.urbcomp.startdb.compress.elf.doubleprecision.FileReader;
import org.urbcomp.startdb.compress.elf.doubleprecision.ResultStructure;

import java.io.*;
import java.nio.ByteBuffer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCompressor {
    private static final String FILE_PATH = "src/test/resources/testData/";

    private static final String[] FILENAMES = {
            //time series
//            "/City-temp.csv",
//            "/IR-bio-temp.csv",
//            "/Dew-point-temp.csv",
//            "/Wind-Speed.csv",
//            "/Stocks-UK.csv",
//            "/Stocks-USA.csv",
//            "/Stocks-DE.csv",
//            "/PM10-dust.csv",
//            "/Bitcoin-price.csv",
//            "/Air-pressure.csv",
//            "/Bird-migration.csv",
//            "/Basel-temp.csv",
//            "/Basel-wind.csv",
//            "/Air-sensor.csv",
//            "/Air-pressure.csv",
//            //normal series
//            "/SSD-bench.csv",
//            "/electric_vehicle_charging.csv",
//            "/Food-price.csv",
//            "/City-lat.csv",
//            "/City-lon.csv",
//            "/Blockchain-tr.csv",
//            "/POI-lat.csv",
//            "/POI-lon.csv"

            "air_sensor_f.csv",
            "arade4.csv",
            "basel_temp_f.csv",
            "basel_wind_f.csv",
            "bird_migration_f.csv",
            "bitcoin_f.csv",
            "bitcoin_transactions_f.csv",
            "city_temperature_f.csv",
            "cms1.csv",
            "cms25.csv",
            "cms9.csv",
            "food_prices.csv",
            "gov10.csv",
            "gov26.csv",
            "gov30.csv",
            "gov31.csv",
            "gov40.csv",
            "medicare1.csv",
            "medicare9.csv",
            "neon_air_pressure.csv",
            "neon_bio_temp_c.csv",
            "neon_dew_point_temp.csv",
            "neon_pm10_dust.csv",
            "neon_wind_dir.csv",
            "nyc29.csv",
            "poi_lat.csv",
            "poi_lon.csv",
            "ssd_hdd_benchmarks_f.csv",
            "stocks_de.csv",
            "stocks_uk.csv",
            "stocks_usa_c.csv",

    };
    private static final String STORE_RESULT = "src/test/resources/result/result.csv";

    private static final double TIME_PRECISION = 1000.0;
    List<Map<String, ResultStructure>> allResult = new ArrayList<>();

    @Test
    public void testCompressor() throws IOException {
        for (String filename : FILENAMES) {
            Map<String, List<ResultStructure>> result = new HashMap<>();
            System.out.println(":" + filename);
            testELFCompressor(filename, result);
            testHuffCompressor(filename, result);
            testFPC(filename, result);
//            testSnappy(filename, result);
//            testZstd(filename, result);
//            testLZ4(filename, result);
//            testBrotli(filename, result);
//            testXz(filename, result);
            testDigitCompressor(filename, result);
            testHundredDigitCompressor(filename, result);
            for (Map.Entry<String, List<ResultStructure>> kv : result.entrySet()) {
                Map<String, ResultStructure> r = new HashMap<>();
                r.put(kv.getKey(), computeAvg(kv.getValue()));
                allResult.add(r);
            }
            if (result.isEmpty()) {
                System.out.println("The result of the file " + filename +
                        " is empty because the amount of data is less than one block, and the default is at least 1000.");
            }
        }
        storeResult();

    }

    private void testHundredDigitCompressor(String filename, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        int blockSize = 1024;
        int patchSize = 8;
        DataReader dataReader = new DataReader(FILE_PATH + filename, blockSize, patchSize);

        float totalBlocks = 0;
        DecimalSeries decimalSeries;
        double[] values;

        HashMap<String, List<Double>> totalCompressionTime = new HashMap<>();
        HashMap<String, List<Double>> totalDecompressionTime = new HashMap<>();
        HashMap<String, Long> key2TotalSize = new HashMap<>();

        while ((decimalSeries = dataReader.nextBlock2Decimals()) != null) {
            totalBlocks += 1;

            HundredDataCompressor hundredDataCompressor = new HundredDataCompressor(decimalSeries, blockSize, patchSize);

            double encodingDuration;
            double decodingDuration;
            long start = System.nanoTime();

            hundredDataCompressor.compress();
            hundredDataCompressor.close();


            encodingDuration = System.nanoTime() - start;

            byte[] result = hundredDataCompressor.getBytes();

            int size = hundredDataCompressor.getSize();

            DigitDecompressor digitDecompressor = new DigitDecompressor(result);

            String key = hundredDataCompressor.getKey();
            if (!totalCompressionTime.containsKey(key)) {
                totalCompressionTime.put(key, new ArrayList<>());
                totalDecompressionTime.put(key, new ArrayList<>());
                key2TotalSize.put(key, 0L);
            }


            start = System.nanoTime();
            List<Double> uncompressedValues = digitDecompressor.decompress();

            decodingDuration = System.nanoTime() - start;
//            for (int j = 0; j < values.length; j++) {
//                assertEquals(values[j], uncompressedValues.get(j), "Value did not match " + compressor.getKey()
//                        + " block:" + totalBlocks
//                        + " filename:" + fileName + ",line:" + j);
//            }
            totalCompressionTime.get(key).add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.get(key).add(decodingDuration / TIME_PRECISION);
            key2TotalSize.put(key, hundredDataCompressor.getSize() + key2TotalSize.get(key));
        }

        for (Map.Entry<String, Long> kv : key2TotalSize.entrySet()) {
            String key = kv.getKey();
            Long totalSize = kv.getValue();
            ResultStructure r = new ResultStructure(filename, key,
                    totalSize / (totalBlocks * blockSize * 64.0),
                    totalCompressionTime.get(key),
                    totalDecompressionTime.get(key)

            );
            if (!resultCompressor.containsKey(key)) {
                resultCompressor.put(key, new ArrayList<>());
            }
            resultCompressor.get(key).add(r);
        }
    }

    private void testDigitCompressor(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        int blockSize = 1024;
        int patchSize = 8;
        DataReader dataReader = new DataReader(FILE_PATH + fileName, blockSize, patchSize);

        float totalBlocks = 0;
        DecimalSeries decimalSeries;
        double[] values;

        HashMap<String, List<Double>> totalCompressionTime = new HashMap<>();
        HashMap<String, List<Double>> totalDecompressionTime = new HashMap<>();
        HashMap<String, Long> key2TotalSize = new HashMap<>();

        while ((decimalSeries = dataReader.nextBlock2Decimals()) != null) {
            totalBlocks += 1;

            DigitCompressor digitCompressor = new DigitCompressor(decimalSeries, blockSize, patchSize);

            double encodingDuration;
            double decodingDuration;
            long start = System.nanoTime();

            digitCompressor.compress();
            digitCompressor.close();


            encodingDuration = System.nanoTime() - start;

            byte[] result = digitCompressor.getBytes();

            int size = digitCompressor.getSize();

            DigitDecompressor digitDecompressor = new DigitDecompressor(result);

            String key = digitCompressor.getKey();
            if (!totalCompressionTime.containsKey(key)) {
                totalCompressionTime.put(key, new ArrayList<>());
                totalDecompressionTime.put(key, new ArrayList<>());
                key2TotalSize.put(key, 0L);
            }


            start = System.nanoTime();
            List<Double> uncompressedValues = digitDecompressor.decompress();

            decodingDuration = System.nanoTime() - start;
//            for (int j = 0; j < values.length; j++) {
//                assertEquals(values[j], uncompressedValues.get(j), "Value did not match " + compressor.getKey()
//                        + " block:" + totalBlocks
//                        + " filename:" + fileName + ",line:" + j);
//            }
            totalCompressionTime.get(key).add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.get(key).add(decodingDuration / TIME_PRECISION);
            key2TotalSize.put(key, digitCompressor.getSize() + key2TotalSize.get(key));
        }

        for (Map.Entry<String, Long> kv : key2TotalSize.entrySet()) {
            String key = kv.getKey();
            Long totalSize = kv.getValue();
            ResultStructure r = new ResultStructure(fileName, key,
                    totalSize / (totalBlocks * blockSize * 64.0),
                    totalCompressionTime.get(key),
                    totalDecompressionTime.get(key)

            );
            if (!resultCompressor.containsKey(key)) {
                resultCompressor.put(key, new ArrayList<>());
            }
            resultCompressor.get(key).add(r);
        }

    }

    private void printMap(Map<Integer, Integer> mp) {
        for (int i = 0; i < 66; i++) {
            System.out.println(i + ":" + mp.get(i));
        }
    }


    private void testELFCompressor(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws FileNotFoundException {
        FileReader fileReader = new FileReader(FILE_PATH + fileName);

        float totalBlocks = 0;
        double[] values;

        HashMap<String, List<Double>> totalCompressionTime = new HashMap<>();
        HashMap<String, List<Double>> totalDecompressionTime = new HashMap<>();
        HashMap<String, Long> key2TotalSize = new HashMap<>();

        while ((values = fileReader.nextBlock()) != null) {
            totalBlocks += 1;
            ICompressor[] compressors = new ICompressor[]{
                    new GorillaCompressorOS(),
//                    new ElfOnGorillaCompressorOS(),
                    new ChimpCompressor(),
//                    new ElfOnChimpCompressor(),
                    new ChimpNCompressor(128),
//                    new ElfOnChimpNCompressor(128),
                    new ElfCompressor(),
            };
            for (int i = 0; i < compressors.length; i++) {
                double encodingDuration;
                double decodingDuration;
                long start = System.nanoTime();
                ICompressor compressor = compressors[i];
                for (double value : values) {
                    compressor.addValue(value);
                }
                compressor.close();

                encodingDuration = System.nanoTime() - start;

                byte[] result = compressor.getBytes();
                IDecompressor[] decompressors = new IDecompressor[]{
                        new GorillaDecompressorOS(result),
//                        new ElfOnGorillaDecompressorOS(result),
                        new ChimpDecompressor(result),
//                        new ElfOnChimpDecompressor(result),
                        new ChimpNDecompressor(result, 128),
//                        new ElfOnChimpNDecompressor(result, 128),
                        new ElfDecompressor(result)
                };

                IDecompressor decompressor = decompressors[i];

                start = System.nanoTime();
                List<Double> uncompressedValues = decompressor.decompress();
                decodingDuration = System.nanoTime() - start;

                for (int j = 0; j < values.length; j++) {
                    assertEquals(values[j], uncompressedValues.get(j), "Value did not match" + compressor.getKey());
                }

                String key = compressor.getKey();
                if (!totalCompressionTime.containsKey(key)) {
                    totalCompressionTime.put(key, new ArrayList<>());
                    totalDecompressionTime.put(key, new ArrayList<>());
                    key2TotalSize.put(key, 0L);
                }
                totalCompressionTime.get(key).add(encodingDuration / TIME_PRECISION);
                totalDecompressionTime.get(key).add(decodingDuration / TIME_PRECISION);
                key2TotalSize.put(key, compressor.getSize() + key2TotalSize.get(key));
            }
        }

        for (Map.Entry<String, Long> kv : key2TotalSize.entrySet()) {
            String key = kv.getKey();
            Long totalSize = kv.getValue();
            ResultStructure r = new ResultStructure(fileName, key,
                    totalSize / (totalBlocks * FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                    totalCompressionTime.get(key),
                    totalDecompressionTime.get(key)
            );
            if (!resultCompressor.containsKey(key)) {
                resultCompressor.put(key, new ArrayList<>());
            }
            resultCompressor.get(key).add(r);
        }
    }

    private void testHuffCompressor(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        org.urbcomp.startdb.compress.elf.doubleprecision.FileReader fileReader = new org.urbcomp.startdb.compress.elf.doubleprecision.FileReader(FILE_PATH + fileName);

        float totalBlocks = 0;
        double[] values;

        HashMap<String, List<Double>> totalCompressionTime = new HashMap<>();
        HashMap<String, List<Double>> totalDecompressionTime = new HashMap<>();
        HashMap<String, Long> key2TotalSize = new HashMap<>();

        while ((values = fileReader.nextBlock()) != null) {
            //System.out.println("-----------new_block-----------");
            totalBlocks += 1;
            HuffCompressor compressor = new HuffCompressor();
            double encodingDuration;
            double decodingDuration;
            long start = System.nanoTime();
            for (double value : values) {
                compressor.addValue(value);
            }

            compressor.compress();

            int LeadingAndTrailing = compressor.leadAndTrailSize();

            compressor.close();

            encodingDuration = System.nanoTime() - start;

            start = System.nanoTime();
            decodingDuration = System.nanoTime() - start;
            String key = compressor.getKey();
            if (!totalCompressionTime.containsKey(key)) {
                totalCompressionTime.put(key, new ArrayList<>());
                totalDecompressionTime.put(key, new ArrayList<>());
                key2TotalSize.put(key, 0L);
            }
            totalCompressionTime.get(key).add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.get(key).add(decodingDuration / TIME_PRECISION);
            key2TotalSize.put(key, compressor.getSize() + LeadingAndTrailing + compressor.getVarint() + key2TotalSize.get(key));
        }
        for (Map.Entry<String, Long> kv : key2TotalSize.entrySet()) {
            String key = kv.getKey();
            Long totalSize = kv.getValue();
            ResultStructure r = new ResultStructure(fileName, key,
                    totalSize / (totalBlocks * org.urbcomp.startdb.compress.elf.doubleprecision.FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                    totalCompressionTime.get(key),
                    totalDecompressionTime.get(key)
            );
            if (!resultCompressor.containsKey(key)) {
                resultCompressor.put(key, new ArrayList<>());
            }
            resultCompressor.get(key).add(r);
        }
    }

    private void testFPC(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws FileNotFoundException {
        org.urbcomp.startdb.compress.elf.doubleprecision.FileReader fileReader = new org.urbcomp.startdb.compress.elf.doubleprecision.FileReader(FILE_PATH + fileName);
        float totalBlocks = 0;
        long totalSize = 0;
        double[] values;
        List<Double> totalCompressionTime = new ArrayList<>();
        List<Double> totalDecompressionTime = new ArrayList<>();

        while ((values = fileReader.nextBlock()) != null) {
            double encodingDuration = 0;
            double decodingDuration = 0;
            FpcCompressor fpc = new FpcCompressor();

            ByteBuffer buffer = ByteBuffer.allocate(org.urbcomp.startdb.compress.elf.doubleprecision.FileReader.DEFAULT_BLOCK_SIZE * 10);
            // Compress
            long start = System.nanoTime();
            fpc.compress(buffer, values);
            encodingDuration += System.nanoTime() - start;

            totalSize += buffer.position() * 8L;
            totalBlocks += 1;

            buffer.flip();

            FpcCompressor decompressor = new FpcCompressor();

            double[] dest = new double[org.urbcomp.startdb.compress.elf.doubleprecision.FileReader.DEFAULT_BLOCK_SIZE];
            start = System.nanoTime();
            decompressor.decompress(buffer, dest);
            decodingDuration += System.nanoTime() - start;
            assertArrayEquals(dest, values);
            totalCompressionTime.add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.add(decodingDuration / TIME_PRECISION);
        }
        if (!totalCompressionTime.isEmpty()) {
            String key = "FPC";
            ResultStructure r = new ResultStructure(fileName, key,
                    totalSize / (totalBlocks * org.urbcomp.startdb.compress.elf.doubleprecision.FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                    totalCompressionTime,
                    totalDecompressionTime
            );
            if (!resultCompressor.containsKey(key)) {
                resultCompressor.put(key, new ArrayList<>());
            }
            resultCompressor.get(key).add(r);
        }
    }

    private void testSnappy(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        org.urbcomp.startdb.compress.elf.doubleprecision.FileReader fileReader = new org.urbcomp.startdb.compress.elf.doubleprecision.FileReader(FILE_PATH + fileName);
        float totalBlocks = 0;
        long totalSize = 0;
        double[] values;
        List<Double> totalCompressionTime = new ArrayList<>();
        List<Double> totalDecompressionTime = new ArrayList<>();

        while ((values = fileReader.nextBlock()) != null) {
            double encodingDuration = 0;
            double decodingDuration = 0;

            Configuration conf = HBaseConfiguration.create();
            // ZStandard levels range from 1 to 22.
            // Level 22 might take up to a minute to complete. 3 is the Hadoop default, and will be fast.
            conf.setInt(CommonConfigurationKeys.IO_COMPRESSION_CODEC_ZSTD_LEVEL_KEY, 3);
            SnappyCodec codec = new SnappyCodec();
            codec.setConf(conf);

            ByteBuffer bb = ByteBuffer.allocate(values.length * 8);
            // Compress
            long start = System.nanoTime();
            for (double d : values) {
                bb.putDouble(d);
            }
            byte[] input = bb.array();
            org.apache.hadoop.io.compress.Compressor compressor = codec.createCompressor();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressionOutputStream out = codec.createOutputStream(baos, compressor);
            out.write(input);
            out.close();
            encodingDuration += System.nanoTime() - start;
            final byte[] compressed = baos.toByteArray();
            totalSize += compressed.length * 8L;
            totalBlocks++;

            final byte[] plain = new byte[input.length];
            org.apache.hadoop.io.compress.Decompressor decompressor = codec.createDecompressor();
            start = System.nanoTime();
            CompressionInputStream in = codec.createInputStream(new ByteArrayInputStream(compressed), decompressor);
            IOUtils.readFully(in, plain, 0, plain.length);
            in.close();
            double[] uncompressed = toDoubleArray(plain);
            decodingDuration += System.nanoTime() - start;
            // Decompressed bytes should equal the original
            for (int i = 0; i < values.length; i++) {
                assertEquals(values[i], uncompressed[i], "Value did not match");
            }
            totalCompressionTime.add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.add(decodingDuration / TIME_PRECISION);
        }
        if (!totalCompressionTime.isEmpty()) {
            String key = "Snappy";
            ResultStructure r = new ResultStructure(fileName, key,
                    totalSize / (totalBlocks * org.urbcomp.startdb.compress.elf.doubleprecision.FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                    totalCompressionTime,
                    totalDecompressionTime
            );
            if (!resultCompressor.containsKey(key)) {
                resultCompressor.put(key, new ArrayList<>());
            }
            resultCompressor.get(key).add(r);
        }
    }

    private void testZstd(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        org.urbcomp.startdb.compress.elf.doubleprecision.FileReader fileReader = new org.urbcomp.startdb.compress.elf.doubleprecision.FileReader(FILE_PATH + fileName);
        float totalBlocks = 0;
        long totalSize = 0;
        double[] values;
        List<Double> totalCompressionTime = new ArrayList<>();
        List<Double> totalDecompressionTime = new ArrayList<>();

        while ((values = fileReader.nextBlock()) != null) {
            double encodingDuration = 0;
            double decodingDuration = 0;

            Configuration conf = HBaseConfiguration.create();
            // ZStandard levels range from 1 to 22.
            // Level 22 might take up to a minute to complete. 3 is the Hadoop default, and will be fast.
            conf.setInt(CommonConfigurationKeys.IO_COMPRESSION_CODEC_ZSTD_LEVEL_KEY, 3);
            ZstdCodec codec = new ZstdCodec();
            codec.setConf(conf);

            ByteBuffer bb = ByteBuffer.allocate(values.length * 8);
            // Compress
            long start = System.nanoTime();
            for (double d : values) {
                bb.putDouble(d);
            }
            byte[] input = bb.array();
            org.apache.hadoop.io.compress.Compressor compressor = codec.createCompressor();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressionOutputStream out = codec.createOutputStream(baos, compressor);
            out.write(input);
            out.close();
            encodingDuration += System.nanoTime() - start;
            final byte[] compressed = baos.toByteArray();
            totalSize += compressed.length * 8L;
            totalBlocks++;

            final byte[] plain = new byte[input.length];
            org.apache.hadoop.io.compress.Decompressor decompressor = codec.createDecompressor();
            start = System.nanoTime();
            CompressionInputStream in = codec.createInputStream(new ByteArrayInputStream(compressed), decompressor);
            IOUtils.readFully(in, plain, 0, plain.length);
            in.close();
            double[] uncompressed = toDoubleArray(plain);
            decodingDuration += System.nanoTime() - start;
            // Decompressed bytes should equal the original
            for (int i = 0; i < values.length; i++) {
                assertEquals(values[i], uncompressed[i], "Value did not match");
            }
            totalCompressionTime.add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.add(decodingDuration / TIME_PRECISION);
        }
        if (!totalCompressionTime.isEmpty()) {
            String key = "Zstd";
            ResultStructure r = new ResultStructure(fileName, key,
                    totalSize / (totalBlocks * org.urbcomp.startdb.compress.elf.doubleprecision.FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                    totalCompressionTime,
                    totalDecompressionTime
            );
            if (!resultCompressor.containsKey(key)) {
                resultCompressor.put(key, new ArrayList<>());
            }
            resultCompressor.get(key).add(r);
        }
    }

    private void testLZ4(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        org.urbcomp.startdb.compress.elf.doubleprecision.FileReader fileReader = new org.urbcomp.startdb.compress.elf.doubleprecision.FileReader(FILE_PATH + fileName);
        float totalBlocks = 0;
        long totalSize = 0;
        double[] values;
        List<Double> totalCompressionTime = new ArrayList<>();
        List<Double> totalDecompressionTime = new ArrayList<>();

        while ((values = fileReader.nextBlock()) != null) {
            double encodingDuration = 0;
            double decodingDuration = 0;

            Lz4Codec codec = new Lz4Codec();

            ByteBuffer bb = ByteBuffer.allocate(values.length * 8);
            // Compress
            long start = System.nanoTime();
            for (double d : values) {
                bb.putDouble(d);
            }
            byte[] input = bb.array();
            org.apache.hadoop.io.compress.Compressor compressor = codec.createCompressor();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressionOutputStream out = codec.createOutputStream(baos, compressor);
            out.write(input);
            out.close();
            encodingDuration += System.nanoTime() - start;
            final byte[] compressed = baos.toByteArray();
            totalSize += compressed.length * 8L;
            totalBlocks++;

            final byte[] plain = new byte[input.length];
            org.apache.hadoop.io.compress.Decompressor decompressor = codec.createDecompressor();
            start = System.nanoTime();
            CompressionInputStream in = codec.createInputStream(new ByteArrayInputStream(compressed), decompressor);
            IOUtils.readFully(in, plain, 0, plain.length);
            in.close();
            double[] uncompressed = toDoubleArray(plain);
            decodingDuration += System.nanoTime() - start;
            // Decompressed bytes should equal the original
            for (int i = 0; i < values.length; i++) {
                assertEquals(values[i], uncompressed[i], "Value did not match");
            }
            totalCompressionTime.add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.add(decodingDuration / TIME_PRECISION);
        }
        if (!totalCompressionTime.isEmpty()) {
            String key = "LZ4";
            ResultStructure r = new ResultStructure(fileName, key,
                    totalSize / (totalBlocks * org.urbcomp.startdb.compress.elf.doubleprecision.FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                    totalCompressionTime,
                    totalDecompressionTime
            );
            if (!resultCompressor.containsKey(key)) {
                resultCompressor.put(key, new ArrayList<>());
            }
            resultCompressor.get(key).add(r);
        }
    }

    private void testBrotli(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        org.urbcomp.startdb.compress.elf.doubleprecision.FileReader fileReader = new org.urbcomp.startdb.compress.elf.doubleprecision.FileReader(FILE_PATH + fileName);
        float totalBlocks = 0;
        long totalSize = 0;
        double[] values;
        List<Double> totalCompressionTime = new ArrayList<>();
        List<Double> totalDecompressionTime = new ArrayList<>();

        while ((values = fileReader.nextBlock()) != null) {
            double encodingDuration = 0;
            double decodingDuration = 0;

            BrotliCodec codec = new BrotliCodec();

            ByteBuffer bb = ByteBuffer.allocate(values.length * 8);
            // Compress
            long start = System.nanoTime();
            for (double d : values) {
                bb.putDouble(d);
            }
            byte[] input = bb.array();
            org.apache.hadoop.io.compress.Compressor compressor = codec.createCompressor();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressionOutputStream out = codec.createOutputStream(baos, compressor);
            out.write(input);
            out.close();
            encodingDuration += System.nanoTime() - start;
            final byte[] compressed = baos.toByteArray();
            totalSize += compressed.length * 8L;
            totalBlocks++;

            final byte[] plain = new byte[input.length];
            org.apache.hadoop.io.compress.Decompressor decompressor = codec.createDecompressor();
            start = System.nanoTime();
            CompressionInputStream in = codec.createInputStream(new ByteArrayInputStream(compressed), decompressor);
            IOUtils.readFully(in, plain, 0, plain.length);
            in.close();
            double[] uncompressed = toDoubleArray(plain);
            decodingDuration += System.nanoTime() - start;
            // Decompressed bytes should equal the original
            for (int i = 0; i < values.length; i++) {
                assertEquals(values[i], uncompressed[i], "Value did not match");
            }
            totalCompressionTime.add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.add(decodingDuration / TIME_PRECISION);
        }
        if (!totalCompressionTime.isEmpty()) {
            String key = "Brotli";
            ResultStructure r = new ResultStructure(fileName, key,
                    totalSize / (totalBlocks * org.urbcomp.startdb.compress.elf.doubleprecision.FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                    totalCompressionTime,
                    totalDecompressionTime
            );
            if (!resultCompressor.containsKey(key)) {
                resultCompressor.put(key, new ArrayList<>());
            }
            resultCompressor.get(key).add(r);
        }
    }

    private void testXz(String fileName, Map<String, List<ResultStructure>> resultCompressor) throws IOException {
        org.urbcomp.startdb.compress.elf.doubleprecision.FileReader fileReader = new org.urbcomp.startdb.compress.elf.doubleprecision.FileReader(FILE_PATH + fileName);
        float totalBlocks = 0;
        long totalSize = 0;
        double[] values;
        List<Double> totalCompressionTime = new ArrayList<>();
        List<Double> totalDecompressionTime = new ArrayList<>();

        while ((values = fileReader.nextBlock()) != null) {
            double encodingDuration = 0;
            double decodingDuration = 0;

            Configuration conf = new Configuration();
            // LZMA levels range from 1 to 9.
            // Level 9 might take several minutes to complete. 3 is our default. 1 will be fast.
            conf.setInt(LzmaCodec.LZMA_LEVEL_KEY, 3);
            LzmaCodec codec = new LzmaCodec();
            codec.setConf(conf);

            ByteBuffer bb = ByteBuffer.allocate(values.length * 8);
            // Compress
            long start = System.nanoTime();
            for (double d : values) {
                bb.putDouble(d);
            }
            byte[] input = bb.array();
            org.apache.hadoop.io.compress.Compressor compressor = codec.createCompressor();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressionOutputStream out = codec.createOutputStream(baos, compressor);
            out.write(input);
            out.close();
            encodingDuration += System.nanoTime() - start;
            final byte[] compressed = baos.toByteArray();
            totalSize += compressed.length * 8L;
            totalBlocks++;

            final byte[] plain = new byte[input.length];
            org.apache.hadoop.io.compress.Decompressor decompressor = codec.createDecompressor();
            start = System.nanoTime();
            CompressionInputStream in = codec.createInputStream(new ByteArrayInputStream(compressed), decompressor);
            IOUtils.readFully(in, plain, 0, plain.length);
            in.close();
            double[] uncompressed = toDoubleArray(plain);
            decodingDuration += System.nanoTime() - start;
            // Decompressed bytes should equal the original
            for (int i = 0; i < values.length; i++) {
                assertEquals(values[i], uncompressed[i], "Value did not match");
            }
            totalCompressionTime.add(encodingDuration / TIME_PRECISION);
            totalDecompressionTime.add(decodingDuration / TIME_PRECISION);
        }
        if (!totalCompressionTime.isEmpty()) {
            String key = "Xz";
            ResultStructure r = new ResultStructure(fileName, key,
                    totalSize / (totalBlocks * FileReader.DEFAULT_BLOCK_SIZE * 64.0),
                    totalCompressionTime,
                    totalDecompressionTime
            );
            if (!resultCompressor.containsKey(key)) {
                resultCompressor.put(key, new ArrayList<>());
            }
            resultCompressor.get(key).add(r);
        }
    }

    private void storeResult() throws IOException {
        String filePath = STORE_RESULT;
        File file = new File(filePath).getParentFile();
        if (!file.exists() && !file.mkdirs()) {
            throw new IOException("Create directory failed: " + file);
        }
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(ResultStructure.getHead());
            for (Map<String, ResultStructure> result : allResult) {
                for (ResultStructure ls : result.values()) {
                    fileWriter.write(ls.toString());
                }
            }
        }
    }

    private ResultStructure computeAvg(List<ResultStructure> lr) {
        int num = lr.size();
        double compressionTime = 0;
        double maxCompressTime = 0;
        double minCompressTime = 0;
        double mediaCompressTime = 0;
        double decompressionTime = 0;
        double maxDecompressTime = 0;
        double minDecompressTime = 0;
        double mediaDecompressTime = 0;
        for (ResultStructure resultStructure : lr) {
            compressionTime += resultStructure.getCompressionTime();
            maxCompressTime += resultStructure.getMaxCompressTime();
            minCompressTime += resultStructure.getMinCompressTime();
            mediaCompressTime += resultStructure.getMediaCompressTime();
            decompressionTime += resultStructure.getDecompressionTime();
            maxDecompressTime += resultStructure.getMaxDecompressTime();
            minDecompressTime += resultStructure.getMinDecompressTime();
            mediaDecompressTime += resultStructure.getMediaDecompressTime();
        }
        return new ResultStructure(lr.get(0).getFilename(),
                lr.get(0).getCompressorName(),
                lr.get(0).getCompressorRatio(),
                compressionTime / num,
                maxCompressTime / num,
                minCompressTime / num,
                mediaCompressTime / num,
                decompressionTime / num,
                maxDecompressTime / num,
                minDecompressTime / num,
                mediaDecompressTime / num
        );
    }

    private static double[] toDoubleArray(byte[] byteArray) {
        int times = Double.SIZE / Byte.SIZE;
        double[] doubles = new double[byteArray.length / times];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = ByteBuffer.wrap(byteArray, i * times, times).getDouble();
        }
        return doubles;
    }
}
