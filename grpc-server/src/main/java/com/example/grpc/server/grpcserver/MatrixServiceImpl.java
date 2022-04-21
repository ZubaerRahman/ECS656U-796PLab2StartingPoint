package com.example.grpc.server.grpcserver;


import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
@GrpcService
public class MatrixServiceImpl extends MatrixServiceGrpc.MatrixServiceImplBase
{
	private static final int MAX = 4;

	public MatrixBlock arrayToMatrixBlock(int[][] array) {
		MatrixBlock C = MatrixBlock.newBuilder()
				.setC00(array[0][0])
				.setC01(array[0][1])
				.setC10(array[1][0])
				.setC11(array[1][1])
				.build();

		return C;
	}

	private static void printMatrix(int[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println("");
		}
		return;
	}

	private int[][] performMatrixBlockMultiplication(int matrixA[][], int matrixB[][]) {
		int[][] resultMatrixC = new int[MAX][MAX];
		resultMatrixC[0][0] = matrixA[0][0] * matrixB[0][0] + matrixA[0][1] * matrixB[1][0];
		resultMatrixC[0][1] = matrixA[0][0] * matrixB[0][1] + matrixA[0][1] * matrixB[1][1];
		resultMatrixC[1][0] = matrixA[1][0] * matrixB[0][0] + matrixA[1][1] * matrixB[1][0];
		resultMatrixC[1][1] = matrixA[1][0] * matrixB[0][1] + matrixA[1][1] * matrixB[1][1];
		return resultMatrixC;
	}

	private int[][] performMatrixBlockAddition(int matrixA[][], int matrixB[][]) {
		int resultMatrixC[][] = new int[MAX][MAX];
		for (int i = 0; i < resultMatrixC.length; i++) {
			for (int j = 0; j < resultMatrixC.length; j++) {
				resultMatrixC[i][j] = matrixA[i][j] + matrixB[i][j];
			}
		}
		return C;
	}

	@Override
	public void addBlock(MatrixRequest request, StreamObserver<MatrixResponse> reply) {
		System.out.println("Request received from client:\n" + request);
		int[][] matrixA = convertMatrixBlockToArray(request.getA());
		int[][] matrixB = convertMatrixBlockToArray(request.getB());
		int[][] newMatrix = performMatrixBlockAddition(matrixA, matrixB);
		MatrixResponse response = MatrixResponse.newBuilder()
				.setC(arrayToMatrixBlock(newMatrix))
				.build();
		reply.onNext(response);
		reply.onCompleted();
	}

	@Override
	public void multiplyBlock(MatrixRequest request, StreamObserver<MatrixResponse> reply) {
		System.out.println("Request received from client:\n" + request);
		int[][] matrixA = convertMatrixBlockToArray(request.getA());
		printMatrix(matrixA);
		int[][] matrixB = convertMatrixBlockToArray(request.getB());
		printMatrix(matrixB);
		int[][] newMatrix = performMatrixBlockMultiplication(matrixA, matrixB);
		MatrixResponse response = MatrixResponse.newBuilder()
				.setC(arrayToMatrixBlock(newMatrix))
				.build();
		System.out.println("Response matrix is: \n" + response);
		reply.onNext(response);
		reply.onCompleted();
	}

	private int[][] convertMatrixBlockToArray(MatrixBlock matrixBlocks) {
		int[][] result = new int[MAX][MAX];
		result[0][0] = matrixBlocks.getC00();
		result[0][1] = matrixBlocks.getC01();
		result[1][0] = matrixBlocks.getC10();
		result[1][1] = matrixBlocks.getC11();
		return result;
	}

}
