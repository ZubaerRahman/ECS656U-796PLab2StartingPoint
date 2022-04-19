package com.example.grpc.client.grpcclient.service;

import com.example.grpc.client.grpcclient.util.MatrixBlockUtils;
import com.example.grpc.client.grpcclient.util.MatrixUtils;
import com.example.grpc.server.grpcserver.PingRequest;
import com.example.grpc.server.grpcserver.PongResponse;
import com.example.grpc.server.grpcserver.PingPongServiceGrpc;
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
    public String ping() {
        	ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
		PingPongServiceGrpc.PingPongServiceBlockingStub stub
                = PingPongServiceGrpc.newBlockingStub(channel);
		PongResponse helloResponse = stub.ping(PingRequest.newBuilder()
                .setPing("")
                .build());
		channel.shutdown();
		return helloResponse.getPong();
    }

	public int[][] multiplyMatrix(int A[][], int B[][], long deadline) {
		System.out.println("Processing matrix A");

		printLineByLine(A);
		List<int[][]> miniBlocksA = MatrixUtils.divideMatrixInBlocks(A);
		System.out.println("Split block A succesfully");
		System.out.println(miniBlocksA.toString());
		printLineByLine(miniBlocksA.get(0));
		printLineByLine(miniBlocksA.get(1));
		printLineByLine(miniBlocksA.get(2));
		printLineByLine(miniBlocksA.get(3));

		System.out.println("Processing matrix B");

		printLineByLine(B);
		List<int[][]> miniBlocksB = MatrixUtils.divideMatrixInBlocks(B);
		System.out.println("Split block B succesfully");
		System.out.println(miniBlocksB.toString());
		printLineByLine(miniBlocksB.get(0));
		printLineByLine(miniBlocksB.get(1));
		printLineByLine(miniBlocksB.get(2));
		printLineByLine(miniBlocksB.get(3));

		List<MatrixResponse> responseBlocks = getResult(miniBlocksA, miniBlocksB, deadline);

		return null;

//		int[][] finalResponse = createFinalResponse(responseBlocks, A.length, A[0].length);
//		printLineByLine(finalResponse);

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
		int currentServer = 0;
		int length = matrixABlocks.length;
		// we create all the servers as described in the getServers() function, but
		// we only use what we need
		stubs = getServers();
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
						System.out.println(stubs.get(currentServer).toString());
						serversNeeded = getDeadline(A1, A2, responseMultiplicationBlocks, stubs.get(currentServer), (miniBlocksA.size() * miniBlocksA.size()),
								deadline);
						continue;
					}
					// System.out.println("Multiplying blocks");
					MatrixResponse C = stubs.get(currentServer).multiplyBlock(requestFromMatrix(A1, A2));
					currentServer++;
					if (currentServer == serversNeeded) {
						currentServer = 0;
					}
					responseMultiplicationBlocks.add(C);
				}
			}
		}
		System.out.println("Starting to add the blocks from the multiplication");
		// here we add the blocks from the multiplication, starting with the block
		// from server 0
		currentServer = 0;
		ArrayList<MatrixResponse> addBlocks = new ArrayList<>();
		MatrixResponse lastResponse = null;
		int rows = matrixABlocks.length * 2;
		int rowLength = rows / 2;
		int index = 1;
		for (int i = 0; i < responseMultiplicationBlocks.size(); i += rowLength) {
			for (int j = i; j < rowLength * index; j += 2) {
				if (j == i) {
					lastResponse = stubs.get(currentServer)
							.addBlock(requestFromBlockAddMatrix(responseMultiplicationBlocks.get(j), responseMultiplicationBlocks.get(j + 1)));
				} else {
					lastResponse = stubs.get(currentServer).addBlock(requestFromBlockAddMatrix(lastResponse, responseMultiplicationBlocks.get(j)));
					j--;
				}
			}
			addBlocks.add(lastResponse);
			// System.out.println("Added blocks from multiplication");
			index++;
			currentServer++;
			// once we reach the last server, we start from server 0 again
			if (currentServer == serversNeeded) {
				currentServer = 0;
			}
		}
		return addBlocks;
	}

	static int getDeadline(MatrixBlock A1, MatrixBlock A2, List<MatrixResponse> responses, MatrixServiceBlockingStub stub,
						   int numberOfBlocks, double deadline) {
		 System.out.println("Deadline Matrix A1:");
		 printMatrixObject(A1);
		 System.out.println("Deadline Matrix A2:");
		 printMatrixObject(A2);
		System.out.println("Starting to get deadline");
		// int deadlineMilis = (int) (deadline * 1000);
		double startTime = System.currentTimeMillis();
		System.out.println("Start time: " + startTime + ", running multiplyBlock");
		MatrixResponse temp = stub.multiplyBlock(requestFromMatrix(A1, A2));
		responses.add(temp);
		System.out.println("Ran multiplyBlock sucessfully");
		double endTime = System.currentTimeMillis();
		double footprint = endTime - startTime;
		System.out.println("Footprint is " + (int) footprint);
		double totalTime = (numberOfBlocks - 1) * footprint;
		System.out.println("Total time is " + (int) totalTime);
		// double newDeadline = (int) deadline - footprint;
		// System.out.println("New deadline is " + (int) newDeadline);
		int serversNeeded = (int) (totalTime / deadline);

		System.out.println("Elapsed time for 1 block: " + footprint);
		System.out.println("Total elapsed time: " + totalTime);
		System.out.println("Number of blocks: " + numberOfBlocks);

		if (serversNeeded > 4) {
			serversNeeded = 4;
			System.out.println("Number of needed servers exceeds 4, setting to maximum of 4 servers");
		} else if (serversNeeded <= 1) {
			serversNeeded = 1;
			System.out.println("Number of needed servers is less than 1, setting to 1 server");
		}
		System.out.println("The number of needed servers is " + serversNeeded);
		return serversNeeded;
	}

	public static ArrayList<MatrixServiceBlockingStub> getServers() {
		ManagedChannel[] channels = new ManagedChannel[4];
		ArrayList<MatrixServiceBlockingStub> stubs = new ArrayList<MatrixServiceBlockingStub>();

		String[] servers = new String[4];
		servers[0] = "34.122.39.117";
		servers[1] = "35.202.188.249";
		servers[2] = "35.239.56.179";
		servers[3] = "104.154.250.92";
//		servers[4] = "10.128.0.15";
//		servers[5] = "10.128.0.16";
//		servers[6] = "10.128.0.17";
//		servers[7] = "10.128.0.21";

		for (int i = 0; i < servers.length; i++) {
			channels[i] = ManagedChannelBuilder.forAddress(servers[i], 9091).usePlaintext().build();
			stubs.add(MatrixServiceGrpc.newBlockingStub(channels[i]));
		}
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
