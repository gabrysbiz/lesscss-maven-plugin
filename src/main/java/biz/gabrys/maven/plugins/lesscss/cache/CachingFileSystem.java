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

    public static final String CACHE_DIRECTORY_PARAM = "cache-directory";
    public static final String BASE_FILE_PARAM = "base-file";
    public static final String HASH_ALGORITHM_PARAM = "hash-algorithm";
    public static final String FILE_SYSTEM_CLASS_PARAM = "file-system-class";
    public static final String CACHE_CONTENT_PARAM = "cache-content";

    public static final String PROXY_PREFIX = "proxy.";
    private static final int PROXY_PREFIX_LENGTH = PROXY_PREFIX.length();

    private String baseFile;
    private FileSystem fileSystem;
    private boolean cacheContent;
    private CacheStorage cacheStorage;

    @Override
    public void configure(final Map<String, String> parameters) throws Exception {

        String workingDirectory = null;
        String hashAlgorithm = null;
        String className = null;
        final Map<String, String> internalParameters = new LinkedHashMap<String, String>();

        for (final Entry<String, String> entry : parameters.entrySet()) {
            final String name = entry.getKey();
            final String value = entry.getValue();
            if (CACHE_DIRECTORY_PARAM.equals(name)) {
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

        cacheStorage = new CacheStorage(workingDirectory, hashAlgorithm);
        fileSystem = (FileSystem) Class.forName(className).getConstructor().newInstance();
        fileSystem.configure(internalParameters);
    }

    @Override
    public boolean isSupported(final String path) {
        return fileSystem.isSupported(path);
    }

    @Override
    public String normalize(final String path) throws Exception {
        return fileSystem.normalize(path);
    }

    @Override
    public String expandRedirection(final String path) throws Exception {
        if (cacheStorage.hasRedirection(path)) {
            return cacheStorage.getRedirection(path);
        }
        final String result = fileSystem.expandRedirection(path);
        cacheStorage.saveRedirection(path, result);
        return result;
    }

    @Override
    public boolean exists(final String path) throws Exception {
        if (cacheStorage.hasExistent(path)) {
            return cacheStorage.getExistent(path);
        }
        final boolean result = fileSystem.exists(path);
        cacheStorage.saveExistent(path, result);
        return result;
    }

    @Override
    public FileData fetch(final String path) throws Exception {
        final Collection<String> dependencies = cacheStorage.readDependencies(baseFile);
        if (!dependencies.contains(path)) {
            dependencies.add(path);
            cacheStorage.saveDependencies(baseFile, dependencies);
        }

        if (cacheContent) {
            final byte[] content = cacheStorage.readContent(path);
            if (content != null) {
                final String encoding = cacheStorage.readEncoding(path);
                return new FileData(content, encoding);
            }
            final FileData fileData = fileSystem.fetch(path);
            cacheStorage.saveContent(path, fileData.getContent());
            cacheStorage.saveEncoding(path, fileData.getEncoding());
            return fileData;
        }
        return fileSystem.fetch(path);
    }
}
