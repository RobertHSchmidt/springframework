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

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;

public class Upload {

	private String bucketName;

	private File file;

	private String toFile;

	private boolean publicRead;

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setToFile(String toFile) {
		this.toFile = toFile;
	}

	public void setPublicRead(boolean publicRead) {
		this.publicRead = publicRead;
	}

	public void init() {
		if (bucketName == null) {
			throw new BuildException("bucketName must be set");
		}
		if (file == null) {
			throw new BuildException("file must be set");
		}
		if (toFile == null) {
			throw new BuildException("toFile must be set");
		}
	}

	public void upload(S3Service service) throws S3ServiceException, IOException {
		S3Bucket bucket = getBucket();
		S3Object object = getObject();

		logStart();
		long startTime = System.currentTimeMillis();
		service.putObject(bucket, object);
		long endTime = System.currentTimeMillis();
		logEnd(startTime, endTime);
	}

	private S3Bucket getBucket() {
		return new S3Bucket(bucketName);
	}

	private S3Object getObject() {
		S3Object object = new S3Object(toFile);
		if (publicRead) {
			object.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
		}
		object.setDataInputFile(file);
		object.setContentLength(file.length());
		return object;
	}

	private void logStart() throws IOException {
		System.out
				.println("Uploading " + file.getCanonicalPath() + " (" + file.length() + "B) to bucket " + bucketName);
	}

	private void logEnd(long startTime, long endTime) {
		float transferTime = endTime - startTime / 1000f;
		System.out.println("Transfer Time: " + transferTime + "s - Transfer Rate: " + file.length() / transferTime
				+ "B/s");
	}

}
