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
import java.util.Map.Entry;

import biz.gabrys.lesscss.compiler2.FileSystemOption;
import biz.gabrys.lesscss.compiler2.FileSystemOptionBuilder;
import biz.gabrys.maven.plugins.lesscss.config.ExtendedFileSystemOption;

public class CachingFileSystemOptionBuilder {

    private final FileSystemOptionBuilder builder = new FileSystemOptionBuilder();

    public CachingFileSystemOptionBuilder cacheDirectory(final File directory) {
        builder.appendParameter(CachingFileSystem.CACHE_DIRECTORY_PARAM, directory.getAbsolutePath());
        return this;
    }

    public CachingFileSystemOptionBuilder baseFile(final File file) {
        builder.appendParameter(CachingFileSystem.BASE_FILE_PARAM, file.getAbsolutePath());
        return this;
    }

    public CachingFileSystemOptionBuilder hashAlgorithm(final String algorithm) {
        builder.appendParameter(CachingFileSystem.HASH_ALGORITHM_PARAM, algorithm);
        return this;
    }

    public CachingFileSystemOptionBuilder fileSystemOption(final ExtendedFileSystemOption option) {
        builder.appendParameter(CachingFileSystem.CACHE_CONTENT_PARAM, Boolean.toString(option.isCacheContent()));
        builder.appendParameter(CachingFileSystem.CACHE_REDIRECTS_PARAM, Boolean.toString(option.isCacheRedirects()));
        builder.appendParameter(CachingFileSystem.FILE_SYSTEM_PARAM, option.getClassName());
        for (final Entry<String, String> entry : option.getParameters().entrySet()) {
            builder.appendParameter(CachingFileSystem.PROXY_PREFIX + entry.getKey(), entry.getValue());
        }
        return this;
    }

    public FileSystemOption build() {
        return builder.withClass(CachingFileSystem.class).build();
    }
}
