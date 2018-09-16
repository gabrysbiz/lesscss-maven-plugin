/*
 * LessCSS Maven Plugin
 * http://lesscss-maven-plugin.projects.gabrys.biz/
 *
 * Copyright (c) 2015 Adam Gabryś
 *
 * This file is licensed under the BSD 3-Clause (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain:
 *  - a copy of the License at project page
 *  - a template of the License at https://opensource.org/licenses/BSD-3-Clause
 */
package biz.gabrys.maven.plugins.lesscss.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import biz.gabrys.maven.plugins.lesscss.config.HashAlgorithm.Hasher;
import biz.gabrys.maven.plugins.lesscss.io.FileManager;

public class CachingFileSystemStorage {

    private static final String REDIRECTION_MARKER_FILE_NAME = "redirection.marker";
    private static final String REDIRECTION_FILE_NAME = "redirection.txt";
    private static final String NON_EXISTENCE_MARKER_FILE_NAME = "non-existence.marker";
    private static final String DEPENDENCIES_FILE_NAME = "dependencies.txt";
    private static final String CONTENT_FILE_NAME = "content.bin";
    private static final String ENCODING_FILE_NAME = "encoding.txt";

    protected final String cacheDirectory;
    protected final Hasher hasher;
    protected final FileManager fileManager;
    protected final Map<String, String> idsCache = new HashMap<String, String>();

    public CachingFileSystemStorage(final String cacheDirectory, final Hasher hasher) {
        this(cacheDirectory, hasher, new FileManager());
    }

    protected CachingFileSystemStorage(final String cacheDirectory, final Hasher hasher, final FileManager fileManager) {
        this.cacheDirectory = cacheDirectory;
        this.hasher = hasher;
        this.fileManager = fileManager;
    }

    public boolean hasRedirection(final String path) {
        return createAbstractFile(path, REDIRECTION_MARKER_FILE_NAME).exists();
    }

    public String getRedirection(final String path) throws IOException {
        final File file = createAbstractFile(path, REDIRECTION_FILE_NAME);
        if (file.exists()) {
            return fileManager.readString(file);
        }
        return path;
    }

    public void saveRedirection(final String path, final String redirect) throws IOException {
        fileManager.createEmptyFile(createAbstractFile(path, REDIRECTION_MARKER_FILE_NAME));
        if (!path.equals(redirect)) {
            fileManager.writeString(createAbstractFile(path, REDIRECTION_FILE_NAME), redirect);
        }
    }

    public boolean hasExistent(final String path) {
        return createAbstractFile(path, NON_EXISTENCE_MARKER_FILE_NAME).exists() || createAbstractFile(path, CONTENT_FILE_NAME).exists();
    }

    public boolean isExistent(final String path) {
        return !createAbstractFile(path, NON_EXISTENCE_MARKER_FILE_NAME).exists();
    }

    public void saveAsNonExistent(final String path) throws IOException {
        fileManager.createEmptyFile(createAbstractFile(path, NON_EXISTENCE_MARKER_FILE_NAME));
    }

    public Collection<String> readDependencies(final String path) throws IOException {
        final File file = createAbstractFile(path, DEPENDENCIES_FILE_NAME);
        if (file.exists()) {
            return new HashSet<String>(fileManager.readLines(file));
        }
        return new HashSet<String>();
    }

    public void saveDependencies(final String path, final Collection<String> dependencies) throws IOException {
        final List<String> sortedDependencies = new ArrayList<String>(dependencies);
        Collections.sort(sortedDependencies);
        fileManager.writeLines(createAbstractFile(path, DEPENDENCIES_FILE_NAME), sortedDependencies);
    }

    public boolean hasContent(final String path) {
        return createAbstractFile(path, CONTENT_FILE_NAME).exists();
    }

    public byte[] readContent(final String path) throws IOException {
        return fileManager.readByteArray(createAbstractFile(path, CONTENT_FILE_NAME));
    }

    public void saveContent(final String path, final byte[] data) throws IOException {
        fileManager.writeByteArray(createAbstractFile(path, CONTENT_FILE_NAME), data);
    }

    public String readEncoding(final String path) throws IOException {
        final File file = createAbstractFile(path, ENCODING_FILE_NAME);
        if (file.exists()) {
            return fileManager.readString(file);
        }
        return null;
    }

    public void saveEncoding(final String path, final String encoding) throws IOException {
        if (encoding != null) {
            fileManager.writeString(createAbstractFile(path, ENCODING_FILE_NAME), encoding);
        }
    }

    protected File createAbstractFile(final String path, final String filename) {
        final StringBuilder file = new StringBuilder();
        file.append(cacheDirectory);
        file.append(File.separator);
        String id = idsCache.get(path);
        if (id == null) {
            id = hasher.hash(path);
            idsCache.put(path, id);
        }
        file.append(id);
        file.append(File.separator);
        file.append(filename);
        return new File(file.toString());
    }
}
