package com.example.grpc.client.grpcclient.controller;

import com.example.grpc.client.grpcclient.error.FileStorageException;
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

    Logger logger = LoggerFactory.getLogger(MatrixRestController.class);

    @Autowired
    public MatrixRestController(StorageService storageService) {
        this.storageService = storageService;
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
                            logger.error("Invalid matrix uploaded!");
                            redirectAttributes.addFlashAttribute("message", "InvalidMatrix !");
                            return "redirect:/";
                        }
                        //if matrix satisfies all our criterias save
                        else {
                            matrixAUploaded = true;
                            storageService.storeFile(file);
                            System.out.println("Completed file upload....");
                        }

                    }
                }
                //if we are uploading MATRIX B
                else if (!matrixBUploaded) {
                    //creating array from the file
                    matrixB = makeArray(file);
                    //is null when exception was thrown, then show error
                    if (matrixB == null) {
                        redirectAttributes.addFlashAttribute("message", "InvalidMatrix " + file.getOriginalFilename() + "!");
                        return "redirect:/";
                    } else {
                        if (!isMatrixIsValidAndSquare(matrixB)) {
                            redirectAttributes.addFlashAttribute("message", "InvalidMatrix !");
                            return "redirect:/";
                        } else {
                            //if matrix satisfies all our criteria save
                            matrixBUploaded = true;
                            storageService.storeFile(file);
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

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<?> handleStorageFileNotFound(FileStorageException exc) {
        return ResponseEntity.notFound().build();
    }

    private boolean isMatrixIsValidAndSquare(int [][] matrix) {
        int lines = matrix.length;
        int columns = matrix[0].length;

        return lines < 1 || columns < 1 || lines != columns || !isPowerOfTwo(lines) || !isPowerOfTwo(columns);
    }
}