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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import biz.gabrys.lesscss.compiler2.filesystem.FileData;
import biz.gabrys.lesscss.compiler2.filesystem.FileSystem;

public class CachingFileSystem implements FileSystem {

    public static final String WORKING_DIRECTORY_PARAM = "working-directory";
    public static final String BASE_FILE_PARAM = "base-file";
    public static final String HASH_ALGORITHM_PARAM = "hash-algorithm";
    public static final String FILE_SYSTEM_CLASS_PARAM = "file-system-class";
    public static final String CACHE_CONTENT_PARAM = "cache-content";

    public static final String PROXY_PREFIX = "proxy.";
    private static final int PROXY_PREFIX_LENGTH = PROXY_PREFIX.length();

    private String baseFileId;
    private FileSystem internalFileSystem;
    private boolean cacheContent;

    private MetadataStorage metadata;

    @Override
    public void configure(final Map<String, String> parameters) throws Exception {

        String workingDirectory = null;
        String baseFile = null;
        String hashAlgorithm = null;
        String className = null;
        final Map<String, String> internalParameters = new LinkedHashMap<String, String>();

        for (final Entry<String, String> entry : parameters.entrySet()) {
            final String name = entry.getKey();
            final String value = entry.getValue();
            if (WORKING_DIRECTORY_PARAM.equals(name)) {
                workingDirectory = value;
            } else if (BASE_FILE_PARAM.equals(name)) {
                baseFile = value;
            } else if (HASH_ALGORITHM_PARAM.equals(name)) {
                hashAlgorithm = value;
            } else if (FILE_SYSTEM_CLASS_PARAM.equals(name)) {
                className = value;
            } else if (CACHE_CONTENT_PARAM.equals(name)) {
                cacheContent = "false".equals(value);
            } else {
                internalParameters.put(name.substring(PROXY_PREFIX_LENGTH), value);
            }
        }

        metadata = new MetadataStorage(workingDirectory, hashAlgorithm);
        baseFileId = metadata.createId(baseFile);
        internalFileSystem = (FileSystem) Class.forName(className).getConstructor().newInstance();
        internalFileSystem.configure(internalParameters);
    }

    @Override
    public boolean isSupported(final String path) {
        return internalFileSystem.isSupported(path);
    }

    @Override
    public String normalize(final String path) throws Exception {
        return internalFileSystem.normalize(path);
    }

    @Override
    public String expandRedirection(final String path) throws Exception {
        return internalFileSystem.expandRedirection(path);
    }

    @Override
    public boolean exists(final String path) throws Exception {
        return internalFileSystem.exists(path);
    }

    @Override
    public FileData fetch(final String path) throws Exception {
        final Collection<String> dependencies = metadata.readDependencies(baseFileId);
        if (!dependencies.contains(path)) {
            dependencies.add(path);
            metadata.saveDependencies(baseFileId, dependencies);
        }

        if (cacheContent) {
            final String fileId = metadata.createId(path);
            final byte[] content = metadata.readContent(fileId);
            if (content != null) {
                final String encoding = metadata.readEncoding(fileId);
                return new FileData(content, encoding);
            }
            final FileData fileData = internalFileSystem.fetch(path);
            metadata.saveContent(fileId, fileData.getContent());
            metadata.saveEncoding(fileId, fileData.getEncoding());
            return fileData;
        }
        return internalFileSystem.fetch(path);
    }
}
