/*
 * Copyright 2004-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
