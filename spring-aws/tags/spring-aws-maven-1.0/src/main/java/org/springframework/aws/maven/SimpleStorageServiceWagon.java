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
package org.springframework.aws.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.SessionListener;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

public class SimpleStorageServiceWagon implements Wagon {

	private S3Service service;

	private S3Bucket bucket;

	private boolean interactive;

	private Repository repository;

	private SessionListenerSupport sessionListeners = new SessionListenerSupport(this);

	private TransferListenerSupport transferListeners = new TransferListenerSupport(this);

	public void addSessionListener(SessionListener listener) {
		sessionListeners.addListener(listener);
	}

	public void addTransferListener(TransferListener listener) {
		transferListeners.addListener(listener);
	}

	public void connect(Repository source) throws ConnectionException, AuthenticationException {
		repository = source;
		sessionListeners.fireSessionOpening();
		try {
			service = new RestS3Service(null);
		} catch (S3ServiceException e) {
			sessionListeners.fireSessionConnectionRefused();
			throw new AuthenticationException("Cannot authenticate with anonymous credentials", e);
		}
		sessionListeners.fireSessionOpened();
		bucket = new S3Bucket(source.getHost());
	}

	public void connect(Repository source, ProxyInfo proxyInfo) throws ConnectionException, AuthenticationException {
		connect(source);
	}

	public void connect(Repository source, AuthenticationInfo authenticationInfo) throws ConnectionException,
			AuthenticationException {
		repository = source;
		sessionListeners.fireSessionOpening();
		try {
			service = new RestS3Service(getCredentials(authenticationInfo));
		} catch (S3ServiceException e) {
			sessionListeners.fireSessionConnectionRefused();
			throw new AuthenticationException("Cannot authenticate with current credentials", e);
		}
		sessionListeners.fireSessionOpened();
		sessionListeners.fireSessionLoggedIn();
		bucket = new S3Bucket(source.getHost());
	}

	public void connect(Repository source, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo)
			throws ConnectionException, AuthenticationException {
		connect(source, authenticationInfo);
	}

	public void disconnect() throws ConnectionException {
		sessionListeners.fireSessionDisconnecting();
		service = null;
		sessionListeners.fireSessionLoggedOff();
		sessionListeners.fireSessionDisconnected();
	}

	public void get(String resourceName, File destination) throws TransferFailedException,
			ResourceDoesNotExistException, AuthorizationException {
		Resource resource = new Resource(resourceName);
		transferListeners.fireTransferInitiated(resource, TransferEvent.REQUEST_GET);
		S3Object object;
		try {
			object = service.getObject(bucket, resourceName);
		} catch (S3ServiceException e) {
			throw new ResourceDoesNotExistException("Resource " + resourceName + " does not exist in the repository", e);
		}
		transferListeners.fireTransferStarted(resource, TransferEvent.REQUEST_GET);

		try {
			InputStream in = object.getDataInputStream();
			FileOutputStream fos = new FileOutputStream(destination);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) != -1) {
				fos.write(buf, 0, len);
			}
		} catch (S3ServiceException e) {
			transferListeners.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
			throw new TransferFailedException("Transfer of resource " + resourceName + "failed", e);
		} catch (IOException e) {
			transferListeners.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
			throw new TransferFailedException("Transfer of resource " + resourceName + "failed", e);
		}

		transferListeners.fireTransferCompleted(resource, TransferEvent.REQUEST_GET);
	}

	@SuppressWarnings("unchecked")
	public List getFileList(String destinationDirectory) throws TransferFailedException, ResourceDoesNotExistException,
			AuthorizationException {
		S3Object[] objects;
		try {
			objects = service.listObjects(bucket, destinationDirectory, "");
		} catch (S3ServiceException e) {
			sessionListeners.fireSessionError(e);
			throw new ResourceDoesNotExistException("Could not list objects with prefix" + destinationDirectory, e);
		}

		List<String> fileNames = new ArrayList<String>(objects.length);
		for (S3Object object : objects) {
			fileNames.add(object.getKey());
		}
		return fileNames;
	}

	public boolean getIfNewer(String resourceName, File destination, long timestamp) throws TransferFailedException,
			ResourceDoesNotExistException, AuthorizationException {
		try {
			S3Object object = service.getObjectDetails(bucket, resourceName);
			if (object.getLastModifiedDate().compareTo(new Date(timestamp)) < 0) {
				get(resourceName, destination);
				return true;
			} else {
				return false;
			}
		} catch (S3ServiceException e) {
			Resource resource = new Resource(resourceName);
			transferListeners.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
			throw new TransferFailedException("Could not retrieve details for resource " + resourceName, e);
		}
	}

	public Repository getRepository() {
		return repository;
	}

	public boolean hasSessionListener(SessionListener listener) {
		return sessionListeners.hasListener(listener);
	}

	public boolean hasTransferListener(TransferListener listener) {
		return transferListeners.hasListener(listener);
	}

	public boolean isInteractive() {
		return interactive;
	}

	public void openConnection() throws ConnectionException, AuthenticationException {
		// NO-OP for S3
	}

	public void put(File source, String destination) throws TransferFailedException, ResourceDoesNotExistException,
			AuthorizationException {
		Resource resource = new Resource(destination);
		transferListeners.fireTransferInitiated(resource, TransferEvent.REQUEST_PUT);

		S3Object object = new S3Object(destination);
		object.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
		object.setDataInputFile(source);
		object.setContentLength(source.length());

		transferListeners.fireTransferStarted(resource, TransferEvent.REQUEST_PUT);
		try {
			service.putObject(bucket, object);
		} catch (S3ServiceException e) {
			transferListeners.fireTransferError(resource, TransferEvent.REQUEST_PUT, e);
			throw new TransferFailedException("Transfer of resource " + destination + "failed", e);
		}
		transferListeners.fireTransferCompleted(resource, TransferEvent.REQUEST_GET);
	}

	public void putDirectory(File sourceDirectory, String destinationDirectory) throws TransferFailedException,
			ResourceDoesNotExistException, AuthorizationException {
		for (File f : sourceDirectory.listFiles()) {
			put(f, destinationDirectory + "/" + f.getName());
		}
	}

	public void removeSessionListener(SessionListener listener) {
		sessionListeners.removeListener(listener);
	}

	public void removeTransferListener(TransferListener listener) {
		transferListeners.removeListener(listener);
	}

	public boolean resourceExists(String resourceName) throws TransferFailedException, AuthorizationException {
		try {
			service.getObjectDetails(bucket, resourceName);
		} catch (S3ServiceException e) {
			return false;
		}
		return true;
	}

	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}

	public boolean supportsDirectoryCopy() {
		return false;
	}

	private AWSCredentials getCredentials(AuthenticationInfo authenticationInfo) {
		String accessKey = authenticationInfo.getUserName();
		String secretKey = authenticationInfo.getPassphrase();
		AWSCredentials credentials = new AWSCredentials(accessKey, secretKey);
		return credentials;
	}
}
