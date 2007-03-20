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
		float transferTime = (endTime - startTime) / 1000;
		System.out.println("Transfer Time: " + transferTime + "s - Transfer Rate: " + file.length() / transferTime
				+ "B/s");
	}

}
