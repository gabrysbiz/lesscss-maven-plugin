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
package biz.gabrys.maven.plugins.lesscss.config;

import java.util.Map;

import biz.gabrys.lesscss.compiler2.FileSystemOption;

public class ExtendedFileSystemOption extends FileSystemOption {

    private final boolean cacheContent;
    private final boolean cacheRedirects;
    private final DateProviderConfig dateProviderConfig;

    ExtendedFileSystemOption(final String className, final Map<String, String> parameters, final boolean cacheContent,
            final boolean cacheRedirects, final DateProviderConfig dateProviderConfig) {

        super(className, parameters);
        if (dateProviderConfig == null) {
            throw new IllegalArgumentException("Date provider config cannot be null");
        }
        this.cacheContent = cacheContent;
        this.cacheRedirects = cacheRedirects;
        this.dateProviderConfig = dateProviderConfig;
    }

    public boolean isCacheContent() {
        return cacheContent;
    }

    public boolean isCacheRedirects() {
        return cacheRedirects;
    }

    public DateProviderConfig getDateProviderConfig() {
        return dateProviderConfig;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (cacheContent ? 1231 : 1237);
        result = prime * result + (cacheRedirects ? 1231 : 1237);
        return prime * result + dateProviderConfig.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final ExtendedFileSystemOption other = (ExtendedFileSystemOption) obj;
        return cacheContent == other.cacheContent && cacheRedirects == other.cacheRedirects
                && dateProviderConfig.equals(other.dateProviderConfig);
    }
}
