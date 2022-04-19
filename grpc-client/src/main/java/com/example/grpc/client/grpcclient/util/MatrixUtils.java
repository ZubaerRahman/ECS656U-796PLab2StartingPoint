package com.example.grpc.client.grpcclient.util;

import org.springframework.web.multipart.MultipartFile;


import java.util.ArrayList;
import java.util.Arrays;
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

    public static List<int[][]> divideMatrixInBlocks(int A[][]) {
        List<int[][]> subArrays = new ArrayList<>();

        int k = 2;
        int n = A.length;

        for (int i = 0; i < n - k + 1; i += 2) {

            // column of first cell in current
            // sub-square of size k x k
            for (int j = 0; j < n - k + 1; j += 2) {

                // current sub-square

                boolean[][] assigned = new boolean[2][2];
                int miniBlock[][] = new int[2][2];
                for (int p = i; p < k + i; p++) {
                    int cycle = 0;
                    for (int q = j; q < k + j; q++) {

                        if (cycle == 0 && !assigned[0][0]) {
                            miniBlock[0][0] = A[p][q];
                            assigned[0][0] = true;
                            cycle++;
                            continue;

                        }
                        if (cycle == 0 && !assigned[1][0]) {
                            miniBlock[1][0] = A[p][q];
                            assigned[1][0] = true;
                            cycle++;
                            continue;
                        }
                        if (cycle == 1 && !assigned[0][1]) {
                            miniBlock[0][1] = A[p][q];
                            assigned[0][1] = true;
                            cycle++;
                            continue;
                        }
                        if (cycle == 1 && !assigned[1][1]) {
                            miniBlock[1][1] = A[p][q];
                            assigned[1][1] = true;
                            cycle++;
                            continue;
                        }
                    }
                }
                subArrays.add(miniBlock);
            }
        }

        return subArrays;

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
