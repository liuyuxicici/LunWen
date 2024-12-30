package org.bjtu.compress.liu.analysis;

import org.apache.jena.base.Sys;
import org.apache.jena.vocabulary.VOID;
import org.bjtu.compress.liu.DataReader;
import org.bjtu.compress.liu.entity.Decimal;
import org.bjtu.compress.liu.entity.DecimalSeries;
import org.bjtu.compress.liu.precision.ryu.RyuDouble;
import org.bjtu.compress.liu.utils.DataProcessUtils;
import org.bjtu.compress.liu.utils.DataUtils;
import sun.misc.DoubleConsts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ejml.ops.EjmlUnitTests.assertEquals;

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
            "City-lat.csv",
            "City-lon.csv",

    };

    public static void main(String[] args) throws IOException {

//        analysisPrecision();

//        DataUtils.getPrecision(1026.888);

//        System.out.println((long) (1026.888 * 10E6) / 10E6);

        int count = 1;
        int a = (count++);

        System.out.println(a);
       
    }


    private static Map<String, List<Integer>> analysisFirstValidDigit() throws FileNotFoundException {
        Map<String, List<Integer>> result = new HashMap<>();
        for (String filename : FILENAMES) {
            System.out.println(filename);
            List<Integer> firstDigitCnt = new ArrayList<>(10);
            for (int i = 0; i < 10; i++) {
                firstDigitCnt.add(0);
            }
            DataReader dataReader = new DataReader(FILE_PATH + filename, 1024, 64);
            DecimalSeries decimalSeries;

            while ((decimalSeries = dataReader.nextBlock2Decimals()) != null) {
                Decimal[] decimals = decimalSeries.getDecimals();

                for (Decimal decimal : decimals) {
                    String decimalStr = Long.toString(decimal.getValidData());
                    if (decimalStr.length() > 0) {
                        int first = decimalStr.charAt(0) - '0';
                        firstDigitCnt.set(first, firstDigitCnt.get(first) + 1);
                    }
                }

            }
            if (!result.containsKey(filename)) {
                result.put(filename, firstDigitCnt);
            } else {
                for (int i = 0; i < 10; i++) {
                    result.get(filename).set(i, result.get(filename).get(i) + firstDigitCnt.get(i));
                }

            }
        }
        return result;
    }

    public static void analysisPrecision() throws IOException {
        for (String filename : FILENAMES) {
            System.out.println(filename);

            DataReader dataReader = new DataReader(FILE_PATH + filename, 1024, 64);
            DecimalSeries decimalSeries;
            double[] values = null;
            List<Long> precision = new ArrayList<>();
            while ((values = dataReader.nextBlock2Double()) != null) {

                int referPrecision = 0;


                for (double value : values) {
                    long[] integerAndPrecision = DataUtils.getIntegerAndPrecision(value, referPrecision);
                    precision.add(integerAndPrecision[1]);
                    referPrecision = (int) integerAndPrecision[1];
                }
            }
        }

    }

}
