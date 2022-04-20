//package com.example.grpc.client.grpcclient;
//
//import com.example.grpc.client.grpcclient.service.GRPCClientService;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.beans.factory.annotation.Autowired;
//
//@RestController
//public class PingPongEndpoint {
//
//	GRPCClientService1 grpcClientService;
//	@Autowired
//    	public PingPongEndpoint(GRPCClientService grpcClientService) {
//        	this.grpcClientService = grpcClientService;
//    	}
//	@GetMapping("/ping")
//    	public String ping() {
//        	return grpcClientService.ping();
//    	}
////        @GetMapping("/add")
////	public String add() {
////		return grpcClientService.add();
////	}
////	@GetMapping("/multiply")
////	public String multiply() {
////		return grpcClientService.multiply();
////	}
//}
