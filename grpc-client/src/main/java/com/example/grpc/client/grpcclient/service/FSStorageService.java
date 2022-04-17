package com.example.grpc.client.grpcclient.service;

import com.example.grpc.client.grpcclient.config.FileStorageProperties;
import com.example.grpc.client.grpcclient.error.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class FSStorageService implements StorageService {

    private final Logger LOGGER = LoggerFactory.getLogger(FSStorageService.class);

    private final Path rootLocation;

    @Autowired
    public FSStorageService(FileStorageProperties properties) {
        this.rootLocation = Paths.get(properties.getStorageLocation());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new FileStorageException("Failed to initialise file storage", e);
        }
    }

    @Override
    public void storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new FileStorageException("Failed to store empty file.");
            }
            Path destinationFile = this.rootLocation.resolve(
                    Paths.get(Objects.requireNonNull(file.getOriginalFilename())))
                    .normalize().toAbsolutePath();
            //make sure file is not stored outside current directory
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new FileStorageException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new FileStorageException("Failed to store file.", e);
        }
    }

    @Override
    public Path loadPath(String filename) {
        return rootLocation.resolve(rootLocation);
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new FileStorageException("Could not read stored files", e);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        LOGGER.error("Attempting to load file as resource from storage service...");
        try {
            Path file = loadPath(filename);
            Resource resource = new UrlResource(file.toUri());
            System.out.println(resource.exists());
            System.out.println(resource.isReadable());
            System.out.println(resource.toString());
            if (resource.exists() || resource.isReadable()) {
                LOGGER.info("Resource retrieved!");
                return resource;
            }
            else {
                LOGGER.error("Failed to access file {}. Make sure the file exists and it is readable.", filename);
                throw new FileStorageException(String.format("Failed to access file %s. Make sure the file exists and it is readable.", filename));
            }
        }
        catch (MalformedURLException e) {
            LOGGER.error("Error reading file, malformed URL exception thrown", e);
            throw new FileStorageException(String.format("Failed to read file %s", filename), e);
        }

    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }
}