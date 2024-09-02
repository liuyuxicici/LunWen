package org.bjtu.compress.liu.analysis;

import org.bjtu.compress.liu.DataReader;
import org.bjtu.compress.liu.entity.DecimalSeries;

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

    public static void main(String[] args) throws FileNotFoundException {
        int blockSize = 1024;
        int patchSize = 8;

        for (String filename : FILENAMES) {
            DataReader dataReader = new DataReader(FILE_PATH + filename, blockSize, patchSize);
            DecimalSeries decimalSeries;
            while ((decimalSeries = dataReader.nextBlock2Decimals()) != null) {
                new DataAnalysis().analyzeDigitLength(decimalSeries);
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
