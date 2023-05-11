package com.example.helloworld.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;

import java.util.zip.CRC32C;
import java.util.zip.Checksum;

@RestController
public class SecretManagerController {
	private static final String PROJECT_ID = System.getenv("PROJECT_ID");

	@GetMapping("/secret")
	String pubMessage(String id, String version) throws IOException {
		System.out.println("start find secret : " + id);
		return accessSecretVersion(id, version);
	}

	public String accessSecretVersion(String secretId, String versionId) throws IOException {
		// Initialize client that will be used to send requests. This client only needs
		// to be created
		// once, and can be reused for multiple requests. After completing all of your
		// requests, call
		// the "close" method on the client to safely clean up any remaining background
		// resources.
		try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
			SecretVersionName secretVersionName = SecretVersionName.of(PROJECT_ID, secretId, versionId);

			// Access the secret version.
			AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

			// Verify checksum. The used library is available in Java 9+.
			// If using Java 8, you may use the following:
			// https://github.com/google/guava/blob/e62d6a0456420d295089a9c319b7593a3eae4a83/guava/src/com/google/common/hash/Hashing.java#L395
			byte[] data = response.getPayload().getData().toByteArray();
			Checksum checksum = new CRC32C();
			checksum.update(data, 0, data.length);
			if (response.getPayload().getDataCrc32C() != checksum.getValue()) {
				System.out.printf("Data corruption detected.");
				return "Data corruption detected.";
			}

			// Print the secret payload.
			//
			// WARNING: Do not print the secret in a production environment - this
			// snippet is showing how to access the secret material.
			String payload = response.getPayload().getData().toStringUtf8();
			System.out.printf("Plaintext: %s\n", payload);
			return payload;
		}
	}

}
