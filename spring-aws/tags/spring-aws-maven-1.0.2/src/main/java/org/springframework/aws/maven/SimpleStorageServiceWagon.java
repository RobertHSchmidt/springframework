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

import java.io.*;
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

/**
 * An implementation of the Maven Wagon interface that allows you to access the
 * Amazon S3 service. URLs that reference the S3 service should be in the form
 * of <code>s3://bucket.name</code>. As an example
 * <code>s3://static.springframework.org</code> would put files into the
 * <code>static.springframework.org</code> bucket on the S3 service.
 * 
 * This implementation uses the <code>username</code> and
 * <code>passphrase</code> portions of the server authentication metadata for
 * credentials.
 * 
 * @author Ben Hale
 */
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
		if (authenticationInfo == null) {
			throw new AuthenticationException("S3 requires a username and passphrase to be set");
		}
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

		InputStream in = null;
		OutputStream out = null;
		try {
			in = object.getDataInputStream();
			out = new FileOutputStream(destination);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
		} catch (S3ServiceException e) {
			transferListeners.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
			throw new TransferFailedException("Transfer of resource " + resourceName + "failed", e);
		} catch (IOException e) {
			transferListeners.fireTransferError(resource, TransferEvent.REQUEST_GET, e);
			throw new TransferFailedException("Transfer of resource " + resourceName + "failed", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
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
        InputStream in = null;
        try {
			service.putObject(bucket, object);

            in = new FileInputStream(source);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
				transferListeners.fireTransferProgress(resource, TransferEvent.REQUEST_PUT, buf, len);
			}
        } catch (S3ServiceException e) {
			transferListeners.fireTransferError(resource, TransferEvent.REQUEST_PUT, e);
			throw new TransferFailedException("Transfer of resource " + destination + "failed", e);
		} catch (FileNotFoundException e) {
            transferListeners.fireTransferError(resource, TransferEvent.REQUEST_PUT, e);
			throw new TransferFailedException("Transfer of resource " + destination + "failed", e);
        } catch (IOException e) {
			transferListeners.fireTransferError(resource, TransferEvent.REQUEST_PUT, e);
			throw new TransferFailedException("Transfer of resource " + destination + "failed", e);
		} finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
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
        return new AWSCredentials(accessKey, secretKey);
	}
}
