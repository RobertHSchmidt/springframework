/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.osgi.test.provisioning.internal;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.DefaultDocumentLoader;
import org.springframework.beans.factory.xml.DocumentLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.osgi.test.provisioning.ArtifactLocator;
import org.springframework.util.xml.DomUtils;
import org.springframework.util.xml.XmlValidationModeDetector;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Locator for artifacts found in the local maven repository. Does <strong>not</strong>
 * use Maven libraries, it rather uses the maven patterns and conventions to
 * identify the artifacts.
 * 
 * @author Hal Hidlebrand
 * @author Costin Leau
 * 
 */
public class LocalFileSystemMavenRepository implements ArtifactLocator {

	private static final Log log = LogFactory.getLog(LocalFileSystemMavenRepository.class);

	/** local repo system property */
	private static final String SYS_PROPERTY = "localRepository";
	/** user home system property */
	private static final String USER_HOME_PROPERTY = "user.home";
	/** m2 local user settings */
	private static final String M2_DIR = ".m2";
	/** maven settings xml */
	private static final String M2_SETTINGS = M2_DIR.concat("settings.xml");
	/** default local repository */
	private static final String DEFAULT_DIR = M2_DIR.concat("/repository");
	/** discovered local m2 repository home */
	private final String repositoryHome;


	/**
	 * Default constructor. Constructs a new
	 * <code>LocalFileSystemMavenRepository</code> instance. This constructor
	 * will also determine the repository path by checking the existence of
	 * <code>localRepository</code> system property and falling back to the
	 * <code>settings.xml</code> file and then the traditional
	 * <code>user.home/.m2/repository</code>.
	 */
	public LocalFileSystemMavenRepository() {

		boolean trace = log.isDebugEnabled();

		// check system property
		String localRepository = System.getProperty(SYS_PROPERTY);
		if (trace)
			log.trace("m2 sys property [" + SYS_PROPERTY + "] has value=" + localRepository);

		if (localRepository == null) {
			// if it's not present then check settings.xml local repository property
			localRepository = getMavenSettingsLocalRepository(new FileSystemResource(new File(
				System.getProperty(USER_HOME_PROPERTY), M2_SETTINGS)));
			if (trace)
				log.trace("falling back to M2 settings.xml file; found value=" + localRepository);
			if (localRepository == null) {
				// fall back to the default location
				localRepository = new File(System.getProperty(USER_HOME_PROPERTY), DEFAULT_DIR).getAbsolutePath();
				if (trace)
					log.trace("no custom setting found; using defualt M2 local repository=" + localRepository);

			}
		}

		repositoryHome = localRepository;
		log.info("local Maven2 repository used: [" + repositoryHome + "]");
	}

	/**
	 * Returns the <code>localRepository</code> settings as indicated by the
	 * <code>settings.xml</code> file.
	 * 
	 * @return local repository as indicated by a Maven settings.xml file
	 */
	String getMavenSettingsLocalRepository(Resource m2Settings) {
		// no file found, return null to continue the discovery process
		if (!m2Settings.exists())
			return null;

		try {
			DocumentLoader docLoader = new DefaultDocumentLoader();
			Document document = docLoader.loadDocument(new InputSource(m2Settings.getInputStream()), null, null,
				XmlValidationModeDetector.VALIDATION_NONE, false);

			return (DomUtils.getChildElementValueByTagName(document.getDocumentElement(), "localRepository"));
		}
		catch (Exception ex) {
			throw (RuntimeException) new RuntimeException(new ParserConfigurationException("error parsing resource="
					+ m2Settings)).initCause(ex);
		}
	}

	/**
	 * Find a local maven artifact. First tries to find the resource as a
	 * packaged artifact produced by a local maven build, and if that fails will
	 * search the local maven repository.
	 * 
	 * @param groupId - the groupId of the organization supplying the bundle
	 * @param artifactId - the artifact id of the bundle
	 * @param version - the version of the bundle
	 * @return the String representing the URL location of this bundle
	 */
	public Resource locateArtifact(String groupId, String artifactId, String version) {
		return locateArtifact(groupId, artifactId, version, DEFAULT_ARTIFACT_TYPE);
	}

	/**
	 * Find a local maven artifact. First tries to find the resource as a
	 * packaged artifact produced by a local maven build, and if that fails will
	 * search the local maven repository.
	 * 
	 * @param groupId - the groupId of the organization supplying the bundle
	 * @param artifactId - the artifact id of the bundle
	 * @param version - the version of the bundle
	 * @param type - the extension type of the artifact
	 * @return
	 */
	public Resource locateArtifact(String groupId, String artifactId, String version, String type) {
		try {
			return localMavenBuildArtifact(artifactId, version, type);
		}
		catch (IllegalStateException illStateEx) {
			if (log.isDebugEnabled())
				log.debug("local maven build artifact detection failed, falling back to local maven bundle");
			return localMavenBundle(groupId, artifactId, version, type);
		}
	}

	/**
	 * Return the resource of the indicated bundle in the local Maven repository
	 * 
	 * @param groupId - the groupId of the organization supplying the bundle
	 * @param artifact - the artifact id of the bundle
	 * @param version - the version of the bundle
	 * @return
	 */
	protected Resource localMavenBundle(String groupId, String artifact, String version, String type) {
		StringBuffer location = new StringBuffer(groupId.replace('.', '/'));
		location.append('/');
		location.append(artifact);
		location.append('/');
		location.append(version);
		location.append('/');
		location.append(artifact);
		location.append('-');
		location.append(version);
		location.append(".");
		location.append(type);

		return new FileSystemResource(new File(repositoryHome, location.toString()));
	}

	/**
	 * Find a local maven artifact in the current build tree. This searches for
	 * resources produced by the package phase of a maven build.
	 * 
	 * @param artifactId
	 * @param version
	 * @param type
	 * @return
	 */
	protected Resource localMavenBuildArtifact(String artifactId, String version, String type) {
		try {
			File found = new MavenPackagedArtifactFinder(artifactId, version, type).findPackagedArtifact(new File("."));
			Resource res = new FileSystemResource(found);
			if (log.isDebugEnabled()) {
				log.debug("found local maven artifact:" + res.getDescription() + " for " + artifactId + "|" + version);
			}
			return res;
		}
		catch (IOException ioEx) {
			throw (RuntimeException) new IllegalStateException("Artifact " + artifactId + "-" + version + "." + type
					+ " could not be found").initCause(ioEx);
		}
	}

}
