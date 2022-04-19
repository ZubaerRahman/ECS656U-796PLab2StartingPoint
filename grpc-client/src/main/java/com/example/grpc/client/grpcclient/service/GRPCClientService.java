package com.example.grpc.client.grpcclient.service;

import com.example.grpc.server.grpcserver.PingRequest;
import com.example.grpc.server.grpcserver.PongResponse;
import com.example.grpc.server.grpcserver.PingPongServiceGrpc;
import com.example.grpc.server.grpcserver.MatrixRequest;
import com.example.grpc.server.grpcserver.MatrixResponse;
import com.example.grpc.server.grpcserver.MatrixBlocks;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.catalina.connector.Response;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
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
		printLineByLine(miniBlocksA.get(0));
		printLineByLine(miniBlocksA.get(1));
		printLineByLine(miniBlocksA.get(2));
		printLineByLine(miniBlocksA.get(3));

		System.out.println("Processing matrix B");

		printLineByLine(B);
		List<int[][]> miniBlocksB = divideMatrixInBlocks(B);
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
	static List<Response> getResult(List<int[][]> miniBlocksA, List<int[][]> miniBlocksB, long deadline) {
		List<Response> responseMultiplicationBlocks = new ArrayList<>();
		ArrayList<MultiplyServiceBlockingStub> stubs =null;

		MatrixBlocks A[][] = create2dBlocks(miniBlocksA);
		MatrixBlocks B[][] = create2dBlocks(miniBlocksB);

		System.out.println("MatrixBlocks A");
		System.out.println(A);
		System.out.println("MatrixBlocks B");
		System.out.println(A);

		return null;
	}

	static MatrixBlocks[][] create2dBlocks(List<int[][]> miniBlocks) {

		int sqr = (int) (Math.sqrt(Double.parseDouble("" + miniBlocks.size())));

		MatrixBlocks C[][] = new MatrixBlocks[sqr][sqr];
		int p = 0;
		int rowLength = sqr;
		for (int i = 0; i < rowLength; i++) {
			Arrays.deepToString(miniBlocks.get(i));
			for (int j = 0; j < rowLength; j++) {
				C[i][j] = makeBlocks(miniBlocks.get(p));
				p++;
			}
		}
		return C;
	}

	public static MatrixBlocks makeBlocks(int[][] array) {
		MatrixBlocks C = MatrixBlocks.newBuilder().setA1(array[0][0]).setB1(array[0][1]).setC1(array[1][0]).setD1(array[1][1])
				.build();

		return C;
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
