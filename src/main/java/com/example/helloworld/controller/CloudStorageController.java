package com.example.helloworld.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@RestController
public class CloudStorageController {
	private static final String PROJECT_ID = System.getenv("PROJECT_ID");
	private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");
	
	@GetMapping("/copy")
	String copyfile(String source, String target) throws IOException  {
		System.out.println("Start download file");
		Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
	    byte[] content = storage.readAllBytes(BUCKET_NAME, source);
	    System.out.println("Start upload file");
	    
	    BlobId blobId = BlobId.of(BUCKET_NAME, target);
	    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

	    storage.createFrom(blobInfo, new ByteArrayInputStream(content));
	    
		return "successed";
	}
}
