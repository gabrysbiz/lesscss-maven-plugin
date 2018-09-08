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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import biz.gabrys.maven.plugins.lesscss.config.HashAlgorithm.Hasher;

public class CacheStorage {

    private static final String REDIRECTION_MARKER_FILE_NAME = "redirection.marker";
    private static final String REDIRECTION_FILE_NAME = "redirection.txt";
    private static final String NON_EXISTENCE_MARKER_FILE_NAME = "non-existence.marker";
    private static final String DEPENDENCIES_FILE_NAME = "dependencies.txt";
    private static final String CONTENT_FILE_NAME = "content.bin";
    private static final String ENCODING_FILE_NAME = "encoding.txt";

    private static final String ENCODING = "UTF-8";

    private final String cacheDirectory;
    private final Hasher hasher;
    private final Map<String, String> idsCache;

    public CacheStorage(final String cacheDirectory, final Hasher hasher) {
        this.cacheDirectory = cacheDirectory;
        this.hasher = hasher;
        idsCache = new HashMap<String, String>();
    }

    public boolean hasRedirection(final String path) {
        return createAbstractFile(path, REDIRECTION_MARKER_FILE_NAME).exists();
    }

    public String getRedirection(final String path) throws IOException {
        final File file = createAbstractFile(path, REDIRECTION_FILE_NAME);
        if (file.exists()) {
            return FileUtils.readFileToString(file, ENCODING);
        }
        return path;
    }

    public void saveRedirection(final String path, final String redirect) throws IOException {
        createEmptyFile(createAbstractFile(path, REDIRECTION_MARKER_FILE_NAME));
        if (!path.equals(redirect)) {
            FileUtils.writeStringToFile(createAbstractFile(path, REDIRECTION_FILE_NAME), redirect, ENCODING);
        }
    }

    public boolean hasExistent(final String path) {
        return createAbstractFile(path, CONTENT_FILE_NAME).exists() || createAbstractFile(path, NON_EXISTENCE_MARKER_FILE_NAME).exists();
    }

    public boolean isExistent(final String path) {
        return !createAbstractFile(path, NON_EXISTENCE_MARKER_FILE_NAME).exists();
    }

    public void markAsNonExistent(final String path) throws IOException {
        createEmptyFile(createAbstractFile(path, NON_EXISTENCE_MARKER_FILE_NAME));
    }

    public Collection<String> readDependencies(final String path) throws IOException {
        final File file = createAbstractFile(path, DEPENDENCIES_FILE_NAME);
        if (file.exists()) {
            return new HashSet<String>(FileUtils.readLines(file, ENCODING));
        }
        return new HashSet<String>();
    }

    public void saveDependencies(final String path, final Collection<String> dependencies) throws IOException {
        FileUtils.writeLines(createAbstractFile(path, DEPENDENCIES_FILE_NAME), ENCODING, dependencies);
    }

    public byte[] readContent(final String path) throws IOException {
        final File file = createAbstractFile(path, CONTENT_FILE_NAME);
        if (file.exists()) {
            return FileUtils.readFileToByteArray(file);
        }
        return null;
    }

    public void saveContent(final String path, final byte[] data) throws IOException {
        FileUtils.writeByteArrayToFile(createAbstractFile(path, CONTENT_FILE_NAME), data);
    }

    public String readEncoding(final String path) throws IOException {
        final File file = createAbstractFile(path, ENCODING_FILE_NAME);
        if (file.exists()) {
            return FileUtils.readFileToString(file, ENCODING);
        }
        return null;
    }

    public void saveEncoding(final String path, final String encoding) throws IOException {
        final File file = createAbstractFile(path, ENCODING_FILE_NAME);
        if (encoding == null) {
            deleteIfExist(file);
        } else {
            FileUtils.write(file, encoding, ENCODING);
        }
    }

    private File createAbstractFile(final String path, final String filename) {
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

    private static void deleteIfExist(final File file) throws IOException {
        if (!file.exists()) {
            return;
        }

        if (file.isFile()) {
            if (!file.delete()) {
                throw new IOException("Cannot delete file: " + file.getAbsolutePath());
            }
            return;
        }

        if (file.isDirectory()) {
            FileUtils.deleteDirectory(file);
            return;
        }

        throw new IOException("Cannot delete file (supports only files and directories): " + file.getAbsolutePath());
    }

    private static void createEmptyFile(final File file) throws IOException {
        if (!file.createNewFile()) {
            throw new IOException("Cannot create file: " + file.getAbsolutePath());
        }
    }
}
