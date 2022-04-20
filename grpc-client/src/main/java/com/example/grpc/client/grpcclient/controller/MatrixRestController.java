package com.example.grpc.client.grpcclient.controller;

import com.example.grpc.client.grpcclient.error.FileStorageException;
import com.example.grpc.client.grpcclient.service.GRPCClientService;
import com.example.grpc.client.grpcclient.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Collectors;

import static com.example.grpc.client.grpcclient.util.MatrixUtils.isPowerOfTwo;
import static com.example.grpc.client.grpcclient.util.MatrixUtils.makeArray;

@Controller
public class MatrixRestController {

    int [][] matrixA;
    int [][] matrixB;
    long deadline;
    boolean matrixAUploaded=false;
    boolean matrixBUploaded=false;

    private final StorageService storageService;
    private final GRPCClientService grpcClientService;

    Logger logger = LoggerFactory.getLogger(MatrixRestController.class);

    @Autowired
    public MatrixRestController(StorageService storageService, GRPCClientService grpcClientService) {
        this.storageService = storageService;
        this.grpcClientService = grpcClientService;
    }

    //endpoint trigerred on file upload
    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        logger.info("Loading upload file page....");
        model.addAttribute("files",
                storageService.loadAll()
                        .map(path -> MvcUriComponentsBuilder
                                .fromMethodName(MatrixRestController.class, "serveFile", path.getFileName().toString())
                                .build().toUri().toString())
                        .collect(Collectors.toList()));

        return "uploadForm";
    }

    //handling file upload and checking for correct dimensions of the matrix
    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        System.out.println(file);
        byte[] bytes;
        String completeData;
        String[] lines;

        logger.info("Processing file upload....");

        if (!file.isEmpty()) {
            try {
                //if we are uploading MATRIX A
                if (!matrixAUploaded) {
                    //creating array from the file
                    matrixA = makeArray(file);
                    //is null when exception was thrown, then show error
                    if (matrixA == null) {
                        logger.error("File doesn't contain any matrix!");
                        redirectAttributes.addFlashAttribute("message", "InvalidMatrix " + file.getOriginalFilename() + "!");
                        return "redirect:/";
                    }
                    //else check for matrix to be NxN with correct dimensions
                    else {
                        if (!isMatrixIsValidAndSquare(matrixA)) {
                            logger.error("Invalid matrix A uploaded!");
                            redirectAttributes.addFlashAttribute("message", "InvalidMatrix !");
                            return "redirect:/";
                        }
                        //if matrix satisfies all our criterias save
                        else {
                            matrixAUploaded = true;
                            storageService.storeFile(file);
                            logger.info("Completed Matrix A upload!");
                        }

                    }
                }
                //if we are uploading MATRIX B
                else if (!matrixBUploaded) {
                    //creating array from the file
                    matrixB = makeArray(file);
                    //is null when exception was thrown, then show error
                    if (matrixB == null) {
                        logger.error("File doesn't contain any matrix!");
                        redirectAttributes.addFlashAttribute("message", "InvalidMatrix " + file.getOriginalFilename() + "!");
                        return "redirect:/";
                    } else {
                        if (!isMatrixIsValidAndSquare(matrixB)) {
                            logger.error("Invalid matrix B uploaded!");
                            redirectAttributes.addFlashAttribute("message", "InvalidMatrix !");
                            return "redirect:/";
                        } else {
                            //if matrix satisfies all our criteria save
                            matrixBUploaded = true;
                            storageService.storeFile(file);
                            logger.info("Completed Matrix B upload!");
                        }

                    }
                }
                //in case other Exception were caught
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("message", "InvalidMatrix !");
                return "redirect:/";
            }
        }

        //if we saved one of the matrix, show OKAY message
        if (matrixAUploaded || matrixBUploaded) {
            redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
            return "redirect:/";
        }
        redirectAttributes.addFlashAttribute("message", "InvalidMatrix !");
        return "redirect:/";
    }

    //listing the uploaded files
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        logger.info("Serving file {} ...", filename);
        Resource file = storageService.loadAsResource(filename);
        logger.info("Returning file {} ...", filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    //if we are doing a classic calculation (no deadline)
    @RequestMapping(value = "/", method = RequestMethod.POST, params = "standard")
    public String standardMatrixCalculation(HttpServletRequest request, Model uiModel, RedirectAttributes redirectAttributes) {

        //pass infinite if no deadline
        int [][]resArray=grpcClientService.multiplyMatrix(matrixA, matrixB, Long.MAX_VALUE);

        redirectAttributes.addAttribute("resArray", resArray);

        return "redirect:/result/{resArray}";
    }

    @RequestMapping(value="/result/{array}", method=RequestMethod.GET)
    @ResponseBody
    public String displayMatrixResult(@PathVariable int[] array)
    {
        int rows=(int)Math.sqrt((double)array.length);
        int columns=(int)Math.sqrt((double)array.length);

        StringBuilder table= new StringBuilder("<table style='border:1px solid black; border-spacing:0;'> ");
        int element=0;
        for (int i=0; i<rows;i++) {
            table.append("<tr>");
            for(int j=0;j<columns;j++) {
                table.append("<td style='border:1px solid black;'>").append(array[element]).append("</td>");
                element++;
            }
            table.append("</tr>");
        }

        table.append("</table>");

        return table.toString();
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<?> handleStorageFileNotFound(FileStorageException exc) {
        return ResponseEntity.notFound().build();
    }

    private boolean isMatrixIsValidAndSquare(int [][] matrix) {
        int lines = matrix.length;
        int columns = matrix[0].length;

        return !(lines < 1 || columns < 1 || lines != columns || !isPowerOfTwo(lines) || !isPowerOfTwo(columns));
    }
}