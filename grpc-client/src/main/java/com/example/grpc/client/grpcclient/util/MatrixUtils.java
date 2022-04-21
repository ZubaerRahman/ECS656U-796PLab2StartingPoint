package com.example.grpc.client.grpcclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;


import java.util.ArrayList;
import java.util.List;

public class MatrixUtils {

    static Logger logger = LoggerFactory.getLogger(MatrixUtils.class);

    public static int[][] createTwoDimentionalArrayFromFileData (MultipartFile file){
        List<int[]> matrixCells = new ArrayList<>();
        try {
            byte[] bytes;
            String completeData;
            String[] lineArray ;
            bytes=file.getBytes();
            completeData = new String(bytes);
            lineArray = completeData.split("\r\n");

            for (String singleLine: lineArray) {
                String [] matrixCellsStrings=singleLine.split(" ");
                int[] matrixCellValues = new int[matrixCellsStrings.length];
                for(int i = 0; i < matrixCellsStrings.length; i++) {
                    matrixCellValues[i] = Integer.parseInt(matrixCellsStrings[i]);

                }
                matrixCells.add(matrixCellValues);
            }
        }
        catch (Exception e) {
            logger.error("An error has occurred converting the file to a matrix.");
            return null;
        }

        return create2dArrayFromList(matrixCells);
    }

    public static List<int[][]> divideMatrixInTwoByTwoBlocks(int A[][]) {
        List<int[][]> twoByTwoBlockList = new ArrayList<>();

        int mainMatrixWidth = A.length;
        int blockWidth = 2;

        for (int i = 0; i < mainMatrixWidth - blockWidth + 1; i += 2) {
            for (int j = 0; j < mainMatrixWidth - blockWidth + 1; j += 2) {
                boolean[][] cellValuesPresent = new boolean[2][2];
                int[][] twoByTwoBlock = new int[2][2];
                for (int p = i; p < blockWidth + i; p++) {
                    int step = 0;
                    for (int q = j; q < blockWidth + j; q++) {

                        if (step == 0 && !cellValuesPresent[0][0]) {
                            twoByTwoBlock[0][0] = A[p][q];
                            cellValuesPresent[0][0] = true;
                            step++;
                            continue;

                        }
                        if (step == 0 && !cellValuesPresent[1][0]) {
                            twoByTwoBlock[1][0] = A[p][q];
                            cellValuesPresent[1][0] = true;
                            step++;
                            continue;
                        }
                        if (step == 1 && !cellValuesPresent[0][1]) {
                            twoByTwoBlock[0][1] = A[p][q];
                            cellValuesPresent[0][1] = true;
                            step++;
                            continue;
                        }
                        if (step == 1 && !cellValuesPresent[1][1]) {
                            twoByTwoBlock[1][1] = A[p][q];
                            cellValuesPresent[1][1] = true;
                            step++;
                        }
                    }
                }

                twoByTwoBlockList.add(twoByTwoBlock);
            }
        }

        return twoByTwoBlockList;
    }

    public static int [][] create2dArrayFromList(List<int[]> list){
        int rows=list.size();
        int columns=list.get(0).length;
        int[][] twoDimensionalArray = new int[rows][columns];
        for (int i=0; i<rows;i++) {
            for (int j=0;j<columns;j++) {
                twoDimensionalArray[i][j]=list.get(i)[j];
            }
        }
        return twoDimensionalArray;
    }

    public static boolean isPowerOfTwo(int number) {
        return number > 0 && ((number & (number - 1)) == 0);
    }

    public static boolean isMatrixIsValidAndSquare(int [][] matrix) {
        int lines = matrix.length;
        int columns = matrix[0].length;

        boolean matrixIsValidAndSquare = !(lines < 1 || columns < 1 || lines != columns || !isPowerOfTwo(lines) || !isPowerOfTwo(columns));

        return matrixIsValidAndSquare;
    }

}
