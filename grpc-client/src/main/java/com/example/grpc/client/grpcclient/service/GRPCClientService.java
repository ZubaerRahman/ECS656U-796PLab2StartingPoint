package com.example.grpc.client.grpcclient.service;

import com.example.grpc.client.grpcclient.util.MatrixBlockUtils;
import com.example.grpc.client.grpcclient.util.MatrixUtils;
import com.example.grpc.server.grpcserver.MatrixRequest;
import com.example.grpc.server.grpcserver.MatrixResponse;
import com.example.grpc.server.grpcserver.MatrixBlock;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc.MatrixServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GRPCClientService {

    public int[][] multiplyMatrix(int A[][], int B[][]) {
        List<int[][]> twoByTwoBlocksA = MatrixUtils.divideMatrixInTwoByTwoBlocks(A);
        List<int[][]> twoByTwoBlocksB = MatrixUtils.divideMatrixInTwoByTwoBlocks(B);

        List<MatrixResponse> responseBlocks = getMultiplicationResult(twoByTwoBlocksA, twoByTwoBlocksB);

        int[][] finalResponse = createFinalResponse(responseBlocks, A.length, A[0].length);
        MatrixUtils.printLineByLine(finalResponse);

		return finalResponse;
    }

    //this method does all the calculations needed for the final result
    static List<MatrixResponse> getMultiplicationResult(List<int[][]> twoByTwoBlocksA, List<int[][]> twoByTwoBlocksB) {
        List<MatrixResponse> responseMultiplicationBlocks = new ArrayList<>();
        ArrayList<MatrixServiceBlockingStub> stubs = null;

        MatrixBlock matrixABlocks[][] = MatrixBlockUtils.create2DArrayOfMatrixBlocks(twoByTwoBlocksA);
        MatrixBlock matrixBBlocks[][] = MatrixBlockUtils.create2DArrayOfMatrixBlocks(twoByTwoBlocksB);

        int serversNeeded = 1;
        int length = matrixABlocks.length;

        // currently gets only one stub, originally designed to get multiple
        stubs = getServerStubs();

        for (int i = 0; i < matrixABlocks.length; i++) {
            for (int j = 0; j < matrixABlocks.length; j++) {
                for (int k = 0; k < matrixABlocks.length; k++) {
                    MatrixBlock A1 = matrixABlocks[i][k];
                    MatrixBlock A2 = matrixBBlocks[k][j];
                    MatrixResponse C = stubs.get(0).multiplyBlock(MatrixBlockUtils.createMatrixRequestFromMatrix(A1, A2));
                    responseMultiplicationBlocks.add(C);
                }
            }
        }

        System.out.println("Starting to add the blocks from the multiplication");

        ArrayList<MatrixResponse> responseadditionBlocks = new ArrayList<>();
        MatrixResponse lastResponse = null;
        int rows = matrixABlocks.length * 2;
        int rowLength = rows / 2;
        int index = 1;
        for (int i = 0; i < responseMultiplicationBlocks.size(); i += rowLength) {
            // no adding needed if only one response block
            if (responseMultiplicationBlocks.size() == 1) {
                responseadditionBlocks.add(responseMultiplicationBlocks.get(0));
                break;
            }
            for (int j = i; j < rowLength * index; j += 2) {
                if (j == i) {
                    lastResponse = stubs.get(0).addBlock(MatrixBlockUtils.requestFromBlockAddMatrix(responseMultiplicationBlocks.get(j), responseMultiplicationBlocks.get(j + 1)));
                } else {
                    lastResponse = stubs.get(0).addBlock(MatrixBlockUtils.requestFromBlockAddMatrix(lastResponse, responseMultiplicationBlocks.get(j)));
                    j--;
                }
            }
            responseadditionBlocks.add(lastResponse);
            index++;
        }
        return responseadditionBlocks;
    }

    public static ArrayList<MatrixServiceBlockingStub> getServerStubs() {
        ArrayList<MatrixServiceBlockingStub> stubs = new ArrayList<MatrixServiceBlockingStub>();

        stubs.add(MatrixServiceGrpc.newBlockingStub(ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build()));

        return stubs;
    }

    private int[][] createFinalResponse(List<MatrixResponse> responseBlocks, int rows, int columns) {
        int response[][] = new int[rows][columns];

        int block = 0;
        for (int i = 0; i < rows; i +=2) {
            for (int j = 0; j < columns; j += 2) {

                response[i][j] = responseBlocks.get(block).getC().getC00();
                response[i][j + 1] = responseBlocks.get(block).getC().getC01();
                response[i + 1][j] = responseBlocks.get(block).getC().getC10();
                response[i + 1][j + 1] = responseBlocks.get(block).getC().getC11();
                block++;
            }
        }

        return response;
    }



}
