package com.example.grpc.client.grpcclient.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class MatrixUtils {

    //create array from file
    public static int[][] makeArray (MultipartFile file){
        List<int[]> cells =new ArrayList<int[]>();
        try {
            byte[] bytes;
            String completeData;
            String[] lines ;

            bytes=file.getBytes();
            completeData = new String(bytes);
            lines = completeData.split("\r\n");
            for (String line: lines) {
                String [] cellsString=line.split(" ");
                int []cellsInt=new int[cellsString.length];
                for(int i=0; i<cellsString.length;i++) {
                    cellsInt[i]=(int)Integer.valueOf(cellsString[i]);

                }
                cells.add(cellsInt);
            }
        }
        catch (Exception e) {
            System.out.println("error");
            return null;
        }

        int [][]cellsArray=create2dArrayFromList(cells);
        return cellsArray;
    }

    public static int [][] create2dArrayFromList(List<int[]> array){
        int rows=array.size();
        int cols=array.get(0).length;
        int res[][] = new int[rows][cols];
        for (int i=0; i<rows;i++) {
            for (int j=0;j<cols;j++) {
                res[i][j]=array.get(i)[j];
            }
        }

        return res;
    }

    public static boolean isPowerOfTwo(int number) {
        return number > 0 && ((number & (number - 1)) == 0);
    }

}
