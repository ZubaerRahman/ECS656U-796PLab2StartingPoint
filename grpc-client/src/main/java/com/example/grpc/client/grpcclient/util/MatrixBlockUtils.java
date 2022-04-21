package com.example.grpc.client.grpcclient.util;

import com.example.grpc.server.grpcserver.MatrixRequest;
import com.example.grpc.server.grpcserver.MatrixResponse;
import com.example.grpc.server.grpcserver.MatrixBlock;

import java.util.Arrays;
import java.util.List;

public class MatrixBlockUtils {

    public static MatrixBlock[][] create2DArrayOfMatrixBlocks(List<int[][]> miniBlocks) {

        //how many rows will the matrix blocks array have
        int matrixBlocksWidth = (int) (Math.sqrt(Double.parseDouble("" + miniBlocks.size())));

        MatrixBlock[][] twoDimentionalMatrixBlockArray = new MatrixBlock[matrixBlocksWidth][matrixBlocksWidth];

        for (int i = 0; i < matrixBlocksWidth; i++) {
            Arrays.deepToString(miniBlocks.get(i));
            for (int j = 0; j < matrixBlocksWidth; j++) {
                twoDimentionalMatrixBlockArray[i][j] = makeMatrixBlock(miniBlocks.get(j));
            }
        }

        return twoDimentionalMatrixBlockArray;
    }

    public static MatrixBlock makeMatrixBlock(int[][] array) {
        // build matrixblock from an array representing a 2x2 matrix
        MatrixBlock C = MatrixBlock.newBuilder()
                .setC00(array[0][0]).setC01(array[0][1])
                .setC10(array[1][0]).setC11(array[1][1])
                .build();

        return C;
    }

}
