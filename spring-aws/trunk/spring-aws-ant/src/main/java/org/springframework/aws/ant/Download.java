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
package org.springframework.aws.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tools.ant.BuildException;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;

/**
 * A member of the S3 ANT task for dealing with Amazon S3 upload behavior.
 * Requires properties to be set for <code>bucketName</code>,
 * <code>file</code>, and <code>toFile</code>. This operation will use the
 * credentials setup in its parent S3 task tag.
 * 
 * <pre>
 * &lt;download bucketName=&quot;static.springframework.org&quot;
 *         file=&quot;${target.release.dir}/${release-with-dependencies.zip}&quot;
 *         toFile=&quot;SPR/spring-framework-${spring-version}-with-dependencies-${tstamp}-${build.number}.zip&quot;
 *         publicRead=&quot;true&quot;/&gt;
 * </pre>
 * 
 * @author Ben Hale
 */
public class Download extends AbstractTransferService {

	private String bucketName;

	private String file;

	private File toFile;

	/**
	 * Required parameter that corresponds to the S3 bucket to upload to
	 * @param bucketName The name of the bucket
	 */
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	/**
	 * Required parameter that corresponds to the source object key in S3
	 * @param file The source object key in S3
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * Required parameter that corresponds to the file to download
	 * @param toFile The file to download
	 */
	public void setToFile(File toFile) {
		this.toFile = toFile;
	}

	/**
	 * Verify that required parameters have been set
	 */
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

	/**
	 * Uploads an object to S3.
	 * @param service The service with credentials to use for upload
	 * @throws S3ServiceException If there is an error with the S3 service
	 * @throws IOException If the source file cannot be read
	 */
	public void download(S3Service service) throws S3ServiceException, IOException {
		S3Bucket bucket = getBucket();
		S3Object object = service.getObject(bucket, file);

		logStart(object);
		long startTime = System.currentTimeMillis();

		InputStream in = null;
		OutputStream out = null;
		try {
			in = object.getDataInputStream();
			out = new FileOutputStream(toFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					// Nothing possible at this point
				}
			}
			if (out != null) {
				try {
					out.close();
				}
				catch (IOException e) {
					// Nothing possible at this point
				}
			}
		}

		long endTime = System.currentTimeMillis();
		logEnd(startTime, endTime);
	}

	private S3Bucket getBucket() {
		return new S3Bucket(bucketName);
	}

	private void logStart(S3Object object) throws IOException {
		System.out.println("Downloading " + bucketName + "/" + file + " (" + getFormattedSize(object.getContentLength()) + ") to "
				+ toFile.getCanonicalPath());
	}

	private void logEnd(long startTime, long endTime) {
		long transferTime = endTime - startTime;
		System.out.println("Transfer Time: " + getFormattedTime(transferTime) + " - Transfer Rate: "
				+ getFormattedSpeed(toFile.length(), transferTime));
	}
}
