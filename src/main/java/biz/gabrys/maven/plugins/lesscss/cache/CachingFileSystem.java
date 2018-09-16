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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import biz.gabrys.lesscss.compiler2.filesystem.FileData;
import biz.gabrys.lesscss.compiler2.filesystem.FileSystem;
import biz.gabrys.maven.plugins.lesscss.config.HashAlgorithm;
import biz.gabrys.maven.plugins.lesscss.config.HashAlgorithm.Hasher;

public class CachingFileSystem implements FileSystem {

    public static final String BASE_FILE_PARAM = "base-file";
    public static final String CACHE_DIRECTORY_PARAM = "cache-directory";
    public static final String HASH_ALGORITHM_PARAM = "hash-algorithm";
    public static final String FILE_SYSTEM_PARAM = "file-system";
    public static final String CACHE_REDIRECTS_PARAM = "cache-redirects";
    public static final String CACHE_CONTENT_PARAM = "cache-content";

    public static final String PROXY_PREFIX = "proxy.";
    private static final int PROXY_PREFIX_LENGTH = PROXY_PREFIX.length();

    protected String baseFile;
    protected FileSystem proxiedFileSystem;
    protected CachingFileSystemStorage cacheStorage;
    protected boolean cacheRedirects;
    protected boolean cacheContent;

    @Override
    public void configure(final Map<String, String> parameters) throws Exception {
        String cacheDirectory = null;
        Hasher hasher = null;
        String className = null;
        final Map<String, String> internalParameters = new LinkedHashMap<String, String>();

        for (final Entry<String, String> entry : parameters.entrySet()) {
            final String name = entry.getKey();
            final String value = entry.getValue();
            if (BASE_FILE_PARAM.equals(name)) {
                baseFile = value;
            } else if (CACHE_DIRECTORY_PARAM.equals(name)) {
                cacheDirectory = value;
            } else if (HASH_ALGORITHM_PARAM.equals(name)) {
                hasher = HashAlgorithm.valueOf(value).getHasher();
            } else if (FILE_SYSTEM_PARAM.equals(name)) {
                className = value;
            } else if (CACHE_REDIRECTS_PARAM.equals(name)) {
                cacheRedirects = "true".equals(value);
            } else if (CACHE_CONTENT_PARAM.equals(name)) {
                cacheContent = "true".equals(value);
            } else {
                internalParameters.put(name.substring(PROXY_PREFIX_LENGTH), value);
            }
        }

        cacheStorage = createCacheStorage(cacheDirectory, hasher);
        proxiedFileSystem = createFileSystem(className);
        proxiedFileSystem.configure(internalParameters);
    }

    protected CachingFileSystemStorage createCacheStorage(final String cacheDirectory, final Hasher hasher) {
        return new CachingFileSystemStorage(cacheDirectory, hasher);
    }

    protected FileSystem createFileSystem(final String className) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        return (FileSystem) Class.forName(className).getConstructor().newInstance();
    }

    @Override
    public boolean isSupported(final String path) {
        return proxiedFileSystem.isSupported(path);
    }

    @Override
    public String normalize(final String path) throws Exception {
        return proxiedFileSystem.normalize(path);
    }

    @Override
    public String expandRedirection(final String path) throws Exception {
        if (cacheRedirects && cacheStorage.hasRedirection(path)) {
            return cacheStorage.getRedirection(path);
        }
        final String result = proxiedFileSystem.expandRedirection(path);
        if (cacheRedirects) {
            cacheStorage.saveRedirection(path, result);
        }
        return result;
    }

    @Override
    public boolean exists(final String path) throws Exception {
        if (cacheContent && cacheStorage.hasExistent(path)) {
            return cacheStorage.isExistent(path);
        }
        final boolean result = proxiedFileSystem.exists(path);
        if (cacheContent && !result) {
            cacheStorage.saveAsNonExistent(path);
        }
        return result;
    }

    @Override
    public FileData fetch(final String path) throws Exception {
        if (cacheContent) {
            validateExistence(path);
        }
        updateDependencies(path);
        if (cacheContent) {
            if (cacheStorage.hasContent(path)) {
                return new FileData(cacheStorage.readContent(path), cacheStorage.readEncoding(path));
            }
            final FileData fileData = proxiedFileSystem.fetch(path);
            cacheStorage.saveContent(path, fileData.getContent());
            cacheStorage.saveEncoding(path, fileData.getEncoding());
            return fileData;
        }
        return proxiedFileSystem.fetch(path);
    }

    protected void validateExistence(final String path) throws IOException {
        if (cacheStorage.hasExistent(path) && !cacheStorage.isExistent(path)) {
            if (baseFile.equals(path)) {
                throw new IOException("Cache data is corrupted. Clear the cache and try one more time.");
            }
            throw new IOException(String.format("File %s does not exist (information read from cache).", path));
        }
    }

    protected void updateDependencies(final String path) throws IOException {
        if (baseFile.equals(path)) {
            return;
        }

        final Collection<String> dependencies = cacheStorage.readDependencies(baseFile);
        if (!dependencies.contains(path)) {
            dependencies.add(path);
            cacheStorage.saveDependencies(baseFile, dependencies);
        }
    }
}
