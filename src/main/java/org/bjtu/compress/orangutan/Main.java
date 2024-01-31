package org.bjtu.compress.orangutan;


import org.bjtu.compress.orangutan.utils.OrangutanUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<Double> list = new ArrayList<>();
        try {
            FileReader fr = new FileReader("E:\\LunWen\\src\\test\\resources\\ElfTestData\\Air-pressure.csv");//声明读取的文件
            BufferedReader br = new BufferedReader(fr);//声明读取行数据
            String line = br.readLine(); //行数据
            while (line != null) {
                if (!line.equals("\"\"")) {
                    Double x = Double.parseDouble(line);
                    list.add(x);
                }
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

        double[] values = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            values[i] = list.get(i);
        }
        System.out.println(OrangutanUtils.getBias(values, 5));
    }


}
