package com.example.grpc.client.grpcclient.controller;

import com.example.grpc.client.grpcclient.error.FileStorageException;
import com.example.grpc.client.grpcclient.service.GRPCClientService;
import com.example.grpc.client.grpcclient.service.FSStorageService;
import com.example.grpc.client.grpcclient.util.MatrixUtils;
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

@Controller
public class MatrixRestController {

    int [][] matrixA;
    int [][] matrixB;
    boolean completedMatrixAUpload=false;
    boolean completedMatrixBUpload=false;

    private final FSStorageService storageService;
    private final GRPCClientService grpcClientService;

    Logger logger = LoggerFactory.getLogger(MatrixRestController.class);

    @Autowired
    public MatrixRestController(FSStorageService storageService, GRPCClientService grpcClientService) {
        this.storageService = storageService;
        this.grpcClientService = grpcClientService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        logger.info("Loading upload file page...");
        model.addAttribute("files",
                storageService.loadAll().map(path -> MvcUriComponentsBuilder
                                .fromMethodName(MatrixRestController.class, "serveFile", path.getFileName().toString())
                                .build().toUri().toString())
                        .collect(Collectors.toList()));

        return "uploadForm";
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        logger.info("Processing file upload....");

        // on the first upload we will store the matrix as matrix a, on second upload we will store it as matrix b
        if (!file.isEmpty()) {
            try {
                if (!completedMatrixAUpload) {
                    // create a 2-dimensional array from the file that was uploaded
                    matrixA = MatrixUtils.createTwoDimentionalArrayFromFileData(file);
                    if (matrixA == null) {
                        logger.error("File doesn't contain any matrix.");
                        redirectAttributes.addFlashAttribute("message", "File  " + file.getOriginalFilename() + " doesn't contain any matrix.");
                        return "redirect:/";
                    }
                    //else check for matrix to be NxN with correct dimensions
                    else {
                        if (!MatrixUtils.isMatrixIsValidAndSquare(matrixA)) {
                            logger.error("Invalid matrix A uploaded!");
                            redirectAttributes.addFlashAttribute("message", "InvalidMatrix !");
                            return "redirect:/";
                        }
                        else {
                            completedMatrixAUpload = true;
                            storageService.storeFile(file);
                            logger.info("Completed Matrix A upload!");
                        }

                    }
                }
                else if (!completedMatrixBUpload) {
                    matrixB = MatrixUtils.createTwoDimentionalArrayFromFileData(file);
                    if (matrixB == null) {
                        logger.error("File doesn't contain any matrix!");
                        redirectAttributes.addFlashAttribute("message", "InvalidMatrix " + file.getOriginalFilename() + "!");
                        return "redirect:/";
                    } else {
                        if (!MatrixUtils.isMatrixIsValidAndSquare(matrixB)) {
                            logger.error("Invalid matrix B uploaded!");
                            redirectAttributes.addFlashAttribute("message", "You have uploaded an invalid matrix.");
                            return "redirect:/";
                        } else {
                            completedMatrixBUpload = true;
                            storageService.storeFile(file);
                            logger.info("Completed Matrix B upload!");
                        }

                    }
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("message", "You have uploaded an invalid matrix.");
                return "redirect:/";
            }
        }

        // success message for successful upload. i.e. all checks have passed
        if (completedMatrixAUpload || completedMatrixBUpload) {
            redirectAttributes.addFlashAttribute("message", file.getOriginalFilename() + " was successfully uploaded.");
            return "redirect:/";
        }

        //default fail message
        redirectAttributes.addFlashAttribute("message", "You have uploaded an invalid matrix.");
        return "redirect:/";
    }

    //listing the uploaded files
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        logger.info("Serving file {} ...", filename);
        Resource file = storageService.loadAsResource(filename);
        logger.info("Returning file {} ...", filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST, params = "standard")
    public String standardMatrixCalculation(HttpServletRequest request, Model uiModel, RedirectAttributes redirectAttributes) {
        int[][] resultMatrix = grpcClientService.multiplyMatrix(matrixA, matrixB);
        redirectAttributes.addAttribute("resultMatrix", resultMatrix);

        return "redirect:/result/{resultMatrix}";
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
    public ResponseEntity<?> StorageFileNotFondExceptionHandler(FileStorageException exc) {
        return ResponseEntity.notFound().build();
    }

}