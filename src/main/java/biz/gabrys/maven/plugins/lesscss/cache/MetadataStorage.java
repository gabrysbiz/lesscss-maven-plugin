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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

public class MetadataStorage {

    private static final String CONTENT_FILE_NAME = "content.bin";
    private static final String ENCODING_FILE_NAME = "encoding.txt";
    private static final String DEPENDENCIES_FILE_NAME = "dependencies.txt";
    private static final String HASH_ALGHORITM_NAME = "hash-algorithm.txt";

    private static final String ENCODING = "UTF-8";

    private final String storageDirectory;
    private final String hashAlghoritm;

    public MetadataStorage(final File storageDirectory, final String hashAlghoritm) {
        this.storageDirectory = storageDirectory.getAbsolutePath();
        this.hashAlghoritm = hashAlghoritm;
    }

    public MetadataStorage(final String storageDirectory, final String hashAlghoritm) {
        this.storageDirectory = storageDirectory;
        this.hashAlghoritm = hashAlghoritm;
    }

    public String getHashAlgorithm() {
        return hashAlghoritm;
    }

    public String readHashAlgorithm() throws IOException {
        final File file = createFile(HASH_ALGHORITM_NAME);
        if (file.exists()) {
            return FileUtils.readFileToString(file, ENCODING);
        }
        return null;
    }

    public void saveHashAlgorithm() throws IOException {
        FileUtils.writeStringToFile(createFile(HASH_ALGHORITM_NAME), hashAlghoritm, ENCODING);
    }

    public Collection<String> readDependencies(final String id) throws IOException {
        final File file = createFile(id, DEPENDENCIES_FILE_NAME);
        if (file.exists()) {
            return new HashSet<String>(FileUtils.readLines(file, ENCODING));
        }
        return new HashSet<String>();
    }

    public void saveDependencies(final String id, final Collection<String> dependencies) throws IOException {
        FileUtils.writeLines(createFile(id, DEPENDENCIES_FILE_NAME), ENCODING, dependencies);
    }

    public byte[] readContent(final String id) throws IOException {
        final File file = createFile(id, CONTENT_FILE_NAME);
        if (file.exists()) {
            return FileUtils.readFileToByteArray(file);
        }
        return null;
    }

    public void saveContent(final String id, final byte[] data) throws IOException {
        FileUtils.writeByteArrayToFile(createFile(id, CONTENT_FILE_NAME), data);
    }

    public String readEncoding(final String id) throws IOException {
        final File file = createFile(id, ENCODING_FILE_NAME);
        if (file.exists()) {
            return FileUtils.readFileToString(file, ENCODING);
        }
        return null;
    }

    public void saveEncoding(final String id, final String text) throws IOException {
        final File file = createFile(id, ENCODING_FILE_NAME);
        if (text == null) {
            deleteIfNotExist(file);
        } else {
            FileUtils.write(file, text, ENCODING);
        }
    }

    public void deleteStorage() throws IOException {
        FileUtils.deleteDirectory(createFile());
    }

    public void delete(final String id) throws IOException {
        deleteIfNotExist(createFile(id));
    }

    private File createFile(final String... parts) {
        final StringBuilder path = new StringBuilder();
        path.append(storageDirectory);
        for (final String part : parts) {
            path.append(File.separator);
            path.append(part);
        }
        return new File(path.toString());
    }

    private void deleteIfNotExist(final File file) throws IOException {
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

        throw new IOException(String.format("Cannot delete file (supports only files and directories): %s", file.getAbsolutePath()));
    }

    public List<String> getAllIds() {
        final File dir = new File(storageDirectory);
        if (!dir.exists()) {
            return Collections.emptyList();
        }

        final File[] directories = dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(final File pathname) {
                return pathname.isDirectory();
            }
        });

        final List<String> ids = new ArrayList<String>(directories.length);
        for (final File directory : directories) {
            ids.add(directory.getName());
        }
        return ids;
    }

    public String createId(final String path) {
        if ("md5".equals(hashAlghoritm)) {
            return DigestUtils.md5Hex(path);
        } else if ("sha1".equals(hashAlghoritm)) {
            return DigestUtils.sha1Hex(path);
        } else if ("sha256".equals(hashAlghoritm)) {
            return DigestUtils.sha256Hex(path);
        } else if ("sha384".equals(hashAlghoritm)) {
            return DigestUtils.sha384Hex(path);
        } else if ("sha512".equals(hashAlghoritm)) {
            return DigestUtils.sha512Hex(path);
        }
        throw new IllegalArgumentException(String.format("Unsupported hash algorithm: %s", hashAlghoritm));
    }
}
