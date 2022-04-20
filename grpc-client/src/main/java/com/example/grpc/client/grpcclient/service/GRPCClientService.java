package com.example.grpc.client.grpcclient.service;

import com.example.grpc.client.grpcclient.util.MatrixBlockUtils;
import com.example.grpc.client.grpcclient.util.MatrixUtils;
//import com.example.grpc.server.grpcserver.PingRequest;
//import com.example.grpc.server.grpcserver.PongResponse;
//import com.example.grpc.server.grpcserver.PingPongServiceGrpc;
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

    public int[][] multiplyMatrix(int A[][], int B[][], long deadline) {
        System.out.println("Processing matrix A");

        printLineByLine(A);
        List<int[][]> miniBlocksA = MatrixUtils.divideMatrixInBlocks(A);
        System.out.println("Split block A succesfully");
        System.out.println(miniBlocksA.toString());

        System.out.println("Processing matrix B");

        printLineByLine(B);
        List<int[][]> miniBlocksB = MatrixUtils.divideMatrixInBlocks(B);
        System.out.println("Split block B succesfully");
        System.out.println(miniBlocksB.toString());

        List<MatrixResponse> responseBlocks = getResult(miniBlocksA, miniBlocksB, deadline);

        int[][] finalResponse = createFinalResponse(responseBlocks, A.length, A[0].length);
        printLineByLine(finalResponse);

        return null;



//		return finalResponse;
    }

    //this method does all the calculations needed for the final result
    static List<MatrixResponse> getResult(List<int[][]> miniBlocksA, List<int[][]> miniBlocksB, long deadline) {
        List<MatrixResponse> responseMultiplicationBlocks = new ArrayList<>();
        ArrayList<MatrixServiceBlockingStub> stubs = null;

        MatrixBlock matrixABlocks[][] = MatrixBlockUtils.createArrayOfMatrixBlocks(miniBlocksA);
        MatrixBlock matrixBBlocks[][] = MatrixBlockUtils.createArrayOfMatrixBlocks(miniBlocksB);

//		System.out.println("MatrixBlock A");
//		System.out.println(A);
//		System.out.println("MatrixBlock B");
//		System.out.println(A);

        int serversNeeded = 1;
        int length = matrixABlocks.length;
        // we create all the servers as described in the getServers() function, but
        // we only use what we need
        stubs = getServerStubs();
        System.out.println("Stubs:");
        System.out.println(stubs.toString());
        for (int i = 0; i < matrixABlocks.length; i++) {
            for (int j = 0; j < matrixABlocks.length; j++) {
                for (int k = 0; k < matrixABlocks.length; k++) {
                    MatrixBlock A1 = matrixABlocks[i][k];
                    MatrixBlock A2 = matrixBBlocks[k][j];
                    if (i == 0 && j == 0 && k == 0) {
                        System.out.println("Getting deadline");
                        System.out.println("Current server");
                        System.out.println(stubs.get(0).toString());
                        continue;
                    }
                    // System.out.println("Multiplying blocks");
                    MatrixResponse C = stubs.get(0).multiplyBlock(requestFromMatrix(A1, A2));
                    responseMultiplicationBlocks.add(C);
                }
            }
        }
        System.out.println("Starting to add the blocks from the multiplication");
        // here we add the blocks from the multiplication, starting with the block
        // from server 0
        ArrayList<MatrixResponse> addBlocks = new ArrayList<>();
        MatrixResponse lastResponse = null;
        int rows = matrixABlocks.length * 2;
        int rowLength = rows / 2;
        int index = 1;
        for (int i = 0; i < responseMultiplicationBlocks.size(); i += rowLength) {
            for (int j = i; j < rowLength * index; j += 2) {
                if (j == i) {
                    lastResponse = stubs.get(0)
                            .addBlock(requestFromBlockAddMatrix(responseMultiplicationBlocks.get(j), responseMultiplicationBlocks.get(j + 1)));
                } else {
                    lastResponse = stubs.get(0).addBlock(requestFromBlockAddMatrix(lastResponse, responseMultiplicationBlocks.get(j)));
                    j--;
                }
            }
            addBlocks.add(lastResponse);
            // System.out.println("Added blocks from multiplication");
            index++;
        }
        return addBlocks;
    }

    public static ArrayList<MatrixServiceBlockingStub> getServerStubs() {
        ManagedChannel[] channels = new ManagedChannel[1];
        ArrayList<MatrixServiceBlockingStub> stubs = new ArrayList<MatrixServiceBlockingStub>();

        stubs.add(MatrixServiceGrpc.newBlockingStub(ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build()));

        return stubs;
    }

    public static MatrixRequest requestFromMatrix(MatrixBlock matrix1, MatrixBlock matrix2) {
        MatrixRequest request = MatrixRequest.newBuilder().setA(matrix1).setB(matrix2).build();
        return request;
    }

    public static MatrixRequest requestFromBlockAddMatrix(MatrixResponse matrix1, MatrixResponse matrix2) {
        MatrixRequest request = MatrixRequest.newBuilder().setA(matrix1.getC()).setB(matrix2.getC()).build();
        return request;
    }

    private static int[][] createFinalResponse(List<MatrixResponse> responseBlocks, int rows, int columns) {

        int response[][] = new int[rows][columns];


        int block = 0;
        for (int i = 0; i < rows; i +=2) {
            for (int j = 0; j <columns ; j += 2) {

                response[i][j] = responseBlocks.get(block).getC().getC00();
                response[i][j + 1] = responseBlocks.get(block).getC().getC01();
                response[i + 1][j] = responseBlocks.get(block).getC().getC10();
                response[i + 1][j + 1] = responseBlocks.get(block).getC().getC11();
                block++;
            }
        }

        return response;
    }

    public static void printMatrixObject(MatrixBlock matrix) {
        System.out.println("C00: " + matrix.getC00());
        System.out.println("C01: " + matrix.getC01());
        System.out.println("C10: " + matrix.getC10());
        System.out.println("C11: " + matrix.getC11());
    }

    private static void printLineByLine(int [][]array){
        for (int i=0; i<array.length; i++)
        {
            for (int j=0; j<array[i].length;j++)
            {
                System.out.print(array[i][j]+" ");
            }
            System.out.println("");
        }

        return;
    }

}
