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
import java.util.Map;
import java.util.Map.Entry;

import biz.gabrys.lesscss.compiler2.BuilderCreationException;
import biz.gabrys.lesscss.compiler2.FileSystemOption;
import biz.gabrys.lesscss.compiler2.FileSystemOptionBuilder;
import biz.gabrys.maven.plugins.lesscss.config.ExtendedFileSystemOption;
import biz.gabrys.maven.plugins.lesscss.config.HashAlgorithm;

public class CachingFileSystemOptionBuilder {

    private final FileSystemOptionBuilder builder = new FileSystemOptionBuilder();

    public CachingFileSystemOptionBuilder baseFile(final File file) {
        builder.appendParameter(CachingFileSystem.BASE_FILE_PARAM, file.getAbsolutePath());
        return this;
    }

    public CachingFileSystemOptionBuilder cache(final File directory, final HashAlgorithm algorithm) {
        builder.appendParameter(CachingFileSystem.CACHE_DIRECTORY_PARAM, directory.getAbsolutePath());
        builder.appendParameter(CachingFileSystem.HASH_ALGORITHM_PARAM, algorithm.name());
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
        final FileSystemOption option = builder.withClass(CachingFileSystem.class).build();
        final Map<String, String> parameters = option.getParameters();
        if (!parameters.containsKey(CachingFileSystem.BASE_FILE_PARAM)) {
            throw new BuilderCreationException("You forgot to execute \"baseFile\" method");
        }
        if (!parameters.containsKey(CachingFileSystem.CACHE_DIRECTORY_PARAM)) {
            throw new BuilderCreationException("You forgot to execute \"cache\" method");
        }
        if (!parameters.containsKey(CachingFileSystem.CACHE_CONTENT_PARAM)) {
            throw new BuilderCreationException("You forgot to execute \"fileSystemOption\" method");
        }
        return option;
    }
}
