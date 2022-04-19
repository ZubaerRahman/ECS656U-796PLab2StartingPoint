package com.example.grpc.client.grpcclient.util;

import com.example.grpc.server.grpcserver.MatrixRequest;
import com.example.grpc.server.grpcserver.MatrixResponse;
import com.example.grpc.server.grpcserver.MatrixBlock;

import java.util.Arrays;
import java.util.List;

public class MatrixBlockUtils {

    public static MatrixBlock[][] createArrayOfMatrixBlocks(List<int[][]> miniBlocks) {

        int sqr = (int) (Math.sqrt(Double.parseDouble("" + miniBlocks.size())));

        MatrixBlock C[][] = new MatrixBlock[sqr][sqr];
        int p = 0;
        int rowLength = sqr;
        for (int i = 0; i < rowLength; i++) {
            Arrays.deepToString(miniBlocks.get(i));
            for (int j = 0; j < rowLength; j++) {
                C[i][j] = makeMatrixBlock(miniBlocks.get(p));
                p++;
            }
        }
        return C;
    }

    public static MatrixBlock makeMatrixBlock(int[][] array) {
        MatrixBlock C = MatrixBlock.newBuilder().setC00(array[0][0]).setC01(array[0][1]).setC10(array[1][0]).setC11(array[1][1])
                .build();

        return C;
    }

}
