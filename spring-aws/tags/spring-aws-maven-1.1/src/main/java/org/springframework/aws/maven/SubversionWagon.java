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

import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of the Maven Wagon interface that allows you to access a
 * subversion repository. URLs that reference a subversion service should be in
 * the form of <code>svn:repository.location</code>. As an example
 * <code>svn:https://springframework.svn.sourceforge.net/svnroot/springframework</code>
 * would access files via SVN, WEBDAV, and HTTPS on the
 * <code>springframework.svn.sourceforge.net</code> host.
 * <p/>
 * This implementation uses the <code>username</code> and
 * <code>password</code> portions of the server authentication metadata for
 * credentials.
 *
 * @author Ben Hale
 * @since 1.1
 */
public class SubversionWagon extends AbstractWagon {

    private SVNRepository repository;

    private DirectoryNode modifiedFiles = new DirectoryNode();

    public SubversionWagon() {
        super(false);
    }

    protected void connectToRepository(Repository source, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) throws Exception {
        SVNURL url = SVNURL.parseURIEncoded(stripSvnUrl(source.getUrl()));
        setupRepositoryConnection(url.getProtocol());
        repository = SVNRepositoryFactory.create(url);
        repository.setAuthenticationManager(createAuthenticationManager(authenticationInfo));
    }

    protected boolean doesRemoteResourceExist(String resourceName) throws Exception {
        return SVNNodeKind.NONE != repository.checkPath(resourceName, -1);
    }

    protected void disconnectFromRepository() throws SVNException, FileNotFoundException {
        if (modifiedFiles.hasChildren()) {
            ISVNEditor editor = null;
            try {
                editor = repository.getCommitEditor("[spring-aws-maven] deploy maven release artifacts", null);
                commitNode(null, modifiedFiles, editor);
            } catch (SVNException e) {
                e.printStackTrace();
                throw e;
            } finally {
                if (editor != null) {
                    editor.closeEdit();
                }
            }
        }
    }

    protected void getResource(String resourceName, File destination, TransferProgress progress) throws Exception {
        if (!doesRemoteResourceExist(resourceName)) {
            throw new ResourceDoesNotExistException(resourceName + " does not exist");
        }
        repository.getFile(resourceName, -1, null, new TransferProgressFileOutputStream(destination, progress));
    }

    @SuppressWarnings({"unchecked", "LoopStatementThatDoesntLoop"})
    protected boolean isRemoteResourceNewer(String resourceName, long timestamp) throws Exception {
        if (!doesRemoteResourceExist(resourceName)) {
            throw new ResourceDoesNotExistException(resourceName + "does not exist");
        }

        Collection<SVNLogEntry> logEntries = repository.log(new String[]{resourceName}, null, -1, -1, false,
                true);
        for (SVNLogEntry logEntry : logEntries) {
            return logEntry.getDate().compareTo(new Date(timestamp)) < 0;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    protected List<String> listDirectory(String directory) throws Exception {
        if (!doesRemoteResourceExist(directory)) {
            throw new ResourceDoesNotExistException(directory + " does not exist");
        }

        Collection<SVNDirEntry> dirEntries = repository.getDir(directory, -1, null, (Collection) null);
        List<String> fileNames = new ArrayList<String>(dirEntries.size());
        for (SVNDirEntry dirEntry : dirEntries) {
            if (SVNNodeKind.FILE == dirEntry.getKind()) {
                fileNames.add(dirEntry.getName());
            }
        }
        return fileNames;
    }

    protected void putResource(File source, String destination, TransferProgress progress) throws Exception {
        if (doesRemoteResourceExist(destination)) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            getOriginalResource(destination, bytes);
            addModifiedFile(destination, new FileDescriptor(source, destination, bytes.toByteArray()));
        } else {
            addModifiedFile(destination, new FileDescriptor(source, destination));
        }

        InputStream in = null;
        try {
            in = new TransferProgressFileInputStream(source, progress);
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Nothing possible at this point
                }
            }
        }
    }

    private void addModifiedFile(String filename, FileDescriptor fileDescriptor) {
        File path = new File(filename);
        DirectoryNode directoryNode = getDirectoryNode(path.getParentFile());
        directoryNode.addFile(fileDescriptor);
    }

    private void commitNode(File path, DirectoryNode directory, ISVNEditor editor)
            throws SVNException, FileNotFoundException {
        if (path == null) {
            editor.openRoot(-1);
        } else {
            createOrOpenDirectory(path, editor);
        }

        try {
            for (Map.Entry<String, DirectoryNode> subDirectory : directory.getDirectories().entrySet()) {
                commitNode(new File(path, subDirectory.getKey()), subDirectory.getValue(), editor);
            }
            for (FileDescriptor file : directory.getFiles()) {
                createOrModifyFile(file, editor);
            }
        } finally {
            editor.closeDir();
        }
    }

    private ISVNAuthenticationManager createAuthenticationManager(AuthenticationInfo authenticationInfo)
            throws AuthenticationException {
        if (authenticationInfo == null) {
            return null;
        }

        String username = authenticationInfo.getUserName();
        String password = authenticationInfo.getPassword();

        if (username == null || password == null) {
            throw new AuthenticationException("SVN requires a username and password to be set");
        }
        return SVNWCUtil.createDefaultAuthenticationManager(username, password);
    }

    private void createFile(FileDescriptor file, ISVNEditor editor) throws SVNException, FileNotFoundException {
        String destination = file.getDestination();
        String checksum = null;
        editor.addFile(destination, null, -1);
        try {
            editor.applyTextDelta(destination, null);
            try {
                SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
                checksum = deltaGenerator.sendDelta(destination, new FileInputStream(file.getSource()), editor, true);
            } finally {
                editor.textDeltaEnd(destination);
            }
        } finally {
            editor.closeFile(destination, checksum);
        }
    }

    private void createOrModifyFile(FileDescriptor file, ISVNEditor editor) throws SVNException, FileNotFoundException {
        if (file.getExists()) {
            modifyFile(file, editor);
        } else {
            createFile(file, editor);
        }
    }

    private void createOrOpenDirectory(File path, ISVNEditor editor) throws SVNException {
        try {
            editor.openDir(path.getPath(), -1);
        } catch (SVNException e) {
            editor.addDir(path.getName(), null, -1);
        }
    }

    private DirectoryNode getDirectoryNode(File path) {
        if (path == null) {
            return modifiedFiles;
        } else {
            DirectoryNode parentNode = getDirectoryNode(path.getParentFile());
            String name = path.getName();
            if (!parentNode.containsDirectory(name)) {
                parentNode.putDirectory(name, new DirectoryNode());
            }
            return parentNode.getDirectory(name);
        }
    }

    private void getOriginalResource(String destination, ByteArrayOutputStream out) throws SVNException {
        repository.getFile(destination, -1, null, out);
    }

    private void modifyFile(FileDescriptor file, ISVNEditor editor) throws SVNException, FileNotFoundException {
        String destination = file.getDestination();
        String checksum = null;
        editor.openFile(destination, -1);
        try {
            editor.applyTextDelta(destination, null);
            try {
                SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
                checksum = deltaGenerator.sendDelta(destination, new ByteArrayInputStream(file.getOriginal()), 0,
                        new FileInputStream(file.getSource()), editor, true);
            } finally {
                editor.textDeltaEnd(destination);
            }
        } finally {
            editor.closeFile(destination, checksum);
        }
    }

    private void setupRepositoryConnection(String protocol) {
        if ("http".equals(protocol) || "https".equals(protocol)) {
            DAVRepositoryFactory.setup();
        } else if ("svn".equals(protocol) || protocol.startsWith("svn+")) {
            SVNRepositoryFactoryImpl.setup();
        } else if ("file".equals(protocol)) {
            FSRepositoryFactory.setup();
        }
    }

    private String stripSvnUrl(String url) {
        return url.substring(4);
    }

    private class DirectoryNode {

        private Map<String, DirectoryNode> directories = new HashMap<String, DirectoryNode>();

        private Set<FileDescriptor> files = new HashSet<FileDescriptor>();

        public boolean addFile(FileDescriptor file) {
            return files.add(file);
        }

        public boolean containsDirectory(String path) {
            return directories.containsKey(path);
        }

        public Set<FileDescriptor> getFiles() {
            return files;
        }

        public DirectoryNode getDirectory(String path) {
            return directories.get(path);
        }

        public Map<String, DirectoryNode> getDirectories() {
            return directories;
        }

        public boolean hasChildren() {
            return directories.size() != 0 || files.size() != 0;
        }

        public DirectoryNode putDirectory(String path, DirectoryNode node) {
            return directories.put(path, node);
        }
    }

    private class FileDescriptor {

        private File source;

        private String destination;

        private boolean exists;

        private byte[] original;

        public FileDescriptor(File source, String destination) {
            this.source = source;
            this.destination = destination;
        }

        public FileDescriptor(File source, String destination, byte[] original) {
            this.source = source;
            this.destination = destination;
            this.exists = true;
            this.original = original;
        }

        public File getSource() {
            return source;
        }

        public String getDestination() {
            return destination;
        }

        public boolean getExists() {
            return exists;
        }

        public byte[] getOriginal() {
            return original;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof FileDescriptor && destination.equals(((FileDescriptor) o).destination);
        }

        @Override
        public int hashCode() {
            return destination.hashCode();
        }

        @Override
        public String toString() {
            return destination;
        }
    }

}
