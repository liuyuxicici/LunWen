package org.bjtu.compress.liu.analysis;

import org.bjtu.compress.liu.DataReader;
import org.bjtu.compress.liu.entity.DecimalSeries;
import org.bjtu.compress.liu.utils.DataProcessUtils;
import org.bjtu.compress.liu.utils.DataUtils;
import sun.misc.DoubleConsts;

import java.io.FileNotFoundException;

/**
 * @description: 数据分析
 * @author：lyx
 * @date: 2024/8/27
 */
public class DataAnalysis {
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
//            "arade4.csv",
//            "basel_temp_f.csv",
//            "basel_wind_f.csv",
//            "bird_migration_f.csv",
//            "bitcoin_f.csv",
//            "bitcoin_transactions_f.csv",
//            "city_temperature_f.csv",
//            "cms1.csv",
//            "cms25.csv",
//            "cms9.csv",
//            "food_prices.csv",
//            "gov10.csv",
//            "gov26.csv",
//            "gov30.csv",
//            "gov31.csv",
//            "gov40.csv",
//            "medicare1.csv",
//            "medicare9.csv",
//            "neon_air_pressure.csv",
//            "neon_bio_temp_c.csv",
//            "neon_dew_point_temp.csv",
//            "neon_pm10_dust.csv",
//            "neon_wind_dir.csv",
//            "nyc29.csv",
//            "poi_lat.csv",
//            "poi_lon.csv",
//            "ssd_hdd_benchmarks_f.csv",
//            "stocks_de.csv",
//            "stocks_uk.csv",
//            "stocks_usa_c.csv",

    };

    public static void main(String[] args) throws FileNotFoundException {
        int blockSize = 1024;
        int patchSize = 8;

        for (String filename : FILENAMES) {
            System.out.println(filename);
            DataReader dataReader = new DataReader(FILE_PATH + filename, blockSize, patchSize);
            DecimalSeries decimalSeries;
            int[] binary1Count = new int[64];
            while ((decimalSeries = dataReader.nextBlock2Decimals()) != null) {

//                double[][] validDigitsLoc = decimalSeries.getValidDigitsLoc();
//                IsoForest isoForest = new IsoForest();
//                IForest forest = isoForest.train(validDigitsLoc);
//                int[] predict = new int[decimalSeries.getSize()];
//                for (int i = 0; i < validDigitsLoc.length; i++) {
//                    DenseMatrix64F denseMatrix64F = new DenseMatrix64F(1, 2);
//                    denseMatrix64F.set(0, 0, validDigitsLoc[i][0]);
//                    denseMatrix64F.set(0, 1, validDigitsLoc[i][1]);
//                    predict[i] = forest.predict(denseMatrix64F);
//                }
//                System.out.println(predict);
//                long[] validData = decimalSeries.getValidData();
//                for (long validDatum : validData) {
//                    int sNum = DataUtils.get1sNum(validDatum);
//                    binary1Count[sNum]++;
//                    System.out.println(sNum + " ");
//                }
                int x = -17;
                int zigzag = (x << 1) ^ (x >> 31);
                int decode = (zigzag >> 1) ^ -(zigzag & 1);
                System.out.println(decode);
            }

        }

    }

    private void analyzeDigitLength(DecimalSeries decimalSeries) {
        int[][] digits = decimalSeries.getDigits();
        int[][] fixLength = new int[digits.length][decimalSeries.getSize()];

        for (int i = 0; i < digits.length; i++) {
            for (int j = 0; j < decimalSeries.getSize(); j++) {
                fixLength[i][j] = getBitNum(digits[i][j]);
            }
        }
        System.out.println(fixLength);
    }


    private int getBitNum(int num) {
        return 32 - Integer.numberOfLeadingZeros(num);
    }
}
