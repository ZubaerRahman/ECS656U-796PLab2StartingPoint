package com.example.grpc.client.grpcclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class FileStorageProperties {

    /**
     * Path of folder where files will be stored
     */
    private String storageLocation = "upload-dir";

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

}