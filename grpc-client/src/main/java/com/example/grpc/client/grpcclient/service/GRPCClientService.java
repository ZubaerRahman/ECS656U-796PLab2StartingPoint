package com.example.grpc.client.grpcclient.service;

import com.example.grpc.server.grpcserver.PingRequest;
import com.example.grpc.server.grpcserver.PongResponse;
import com.example.grpc.server.grpcserver.PingPongServiceGrpc;
import com.example.grpc.server.grpcserver.MatrixRequest;
import com.example.grpc.server.grpcserver.MatrixReply;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.catalina.connector.Response;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GRPCClientService {
//    public String ping() {
//        	ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
//                .usePlaintext()
//                .build();
//		PingPongServiceGrpc.PingPongServiceBlockingStub stub
//                = PingPongServiceGrpc.newBlockingStub(channel);
//		PongResponse helloResponse = stub.ping(PingRequest.newBuilder()
//                .setPing("")
//                .build());
//		channel.shutdown();
//		return helloResponse.getPong();
//    }
//    public String add(){
//		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",9090)
//		.usePlaintext()
//		.build();
//		MatrixServiceGrpc.MatrixServiceBlockingStub stub
//		 = MatrixServiceGrpc.newBlockingStub(channel);
//		MatrixReply A=stub.addBlock(MatrixRequest.newBuilder()
//			.setA00(1)
//			.setA01(2)
//			.setA10(5)
//			.setA11(6)
//			.setB00(1)
//			.setB01(2)
//			.setB10(5)
//			.setB11(6)
//			.build());
//		String resp=A.getC00()+A.getC01()+A.getC10()+A.getC11()+"";
//		return resp;
//    }
//
//	public String multiply(){
//		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",9090)
//				.usePlaintext()
//				.build();
//		MatrixServiceGrpc.MatrixServiceBlockingStub stub
//				= MatrixServiceGrpc.newBlockingStub(channel);
//		MatrixReply A=stub.multiplyBlock(MatrixRequest.newBuilder()
//				.setA00(1)
//				.setA01(2)
//				.setA10(5)
//				.setA11(6)
//				.setB00(2)
//				.setB01(3)
//				.setB10(6)
//				.setB11(7)
//				.build());
//		String resp= A.getC00()+A.getC01()+A.getC10()+A.getC11()+"";
//		return resp;
//	}

	public int[][] multiplyMatrix(int A[][], int B[][], long deadline) {
		System.out.println("Processing matrix A");

		printLineByLine(A);
		List<int[][]> miniBlocksA = divideMatrixInBlocks(A);
		System.out.println("Split block A succesfully");
		System.out.println(miniBlocksA.toString());

		System.out.println("Processing matrix B");

		printLineByLine(B);
		List<int[][]> miniBlocksB = divideMatrixInBlocks(B);
		System.out.println("Split block B succesfully");
		System.out.println(miniBlocksB.toString());

		return null;

//		List<Response> responseBlocks = getResult(miniBlocksA, miniBlocksB, deadline);

//		int[][] finalResponse = createFinalResponse(responseBlocks, A.length, A[0].length);
//		printLineByLine(finalResponse);

//		return finalResponse;
	}

	static List<int[][]> divideMatrixInBlocks(int A[][]) {
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
