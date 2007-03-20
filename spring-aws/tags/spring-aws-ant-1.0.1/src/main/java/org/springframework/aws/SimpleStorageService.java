package org.springframework.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;

public class SimpleStorageService {

	private String accessKey;

	private String secretKey;

	private List<Upload> uploads = new ArrayList<Upload>();

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public void addConfiguredUpload(Upload upload) {
		uploads.add(upload);
	}

	public void execute() {
		try {
			AWSCredentials credentials = new AWSCredentials(accessKey, secretKey);
			S3Service service = new RestS3Service(credentials);

			for (Upload upload : uploads) {
				upload.upload(service);
			}
		} catch (S3ServiceException e) {
			throw new BuildException(e);
		} catch (IOException e) {
			throw new BuildException(e);
		}
	}

}
