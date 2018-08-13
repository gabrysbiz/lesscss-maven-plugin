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
package biz.gabrys.maven.plugins.lesscss;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import biz.gabrys.maven.plugins.lesscss.cache.MetadataStorage;

public class CacheCleaner {

    private final CacheOptions cacheOptions;
    private final MetadataStorage metadata;

    public CacheCleaner(final File metadataDirectory, final CacheOptions cacheOptions) {
        this.cacheOptions = cacheOptions;
        metadata = new MetadataStorage(metadataDirectory, cacheOptions.hashAlgorithm);
    }

    public void cleanupCache(final Collection<File> files) throws IOException {
        if (cacheOptions.cacheAlive == 0) {
            metadata.deleteStorage();
        }
        updateHashAlgorithm();
        if (cacheOptions.cacheAlive >= 0) {
            deleteOutdated(files);
        }
    }

    private void updateHashAlgorithm() throws IOException {
        final String hashAlgorithm = metadata.readHashAlgorithm();
        if (hashAlgorithm != null) {
            if (metadata.getHashAlgorithm().equals(hashAlgorithm)) {
                return;
            }
            metadata.deleteStorage();
        }
        metadata.saveHashAlgorithm();
    }

    private void deleteOutdated(final Collection<File> files) throws IOException {
        final Set<String> expiredPaths = new HashSet<String>();
        final Set<String> validPaths = new HashSet<String>();
        for (final File file : files) {
            isExpired(file.getAbsolutePath(), expiredPaths, validPaths);
        }

        for (final String expiredPath : expiredPaths) {
            metadata.delete(metadata.createId(expiredPath));
        }
    }

    private boolean isExpired(final String path, final Set<String> expiredPaths, final Set<String> unexpiredPaths) throws IOException {
        boolean expired = false;
        for (final String dependency : metadata.readDependencies(path)) {
            if (unexpiredPaths.contains(dependency)) {
                continue;
            }
            if (expiredPaths.contains(dependency)) {
                expired = true;
            } else {
                expired = isExpired(dependency, expiredPaths, unexpiredPaths) || expired;
            }
        }

        if (expired) {
            expiredPaths.add(path);
            return true;
        }

        expired = isExpiredByFileSystem(path);
        if (expired) {
            expiredPaths.add(path);
        } else {
            unexpiredPaths.add(path);
        }
        return expired;
    }

    private boolean isExpiredByFileSystem(final String path) {
        // TODO
        return path.equals("false");
    }
}
