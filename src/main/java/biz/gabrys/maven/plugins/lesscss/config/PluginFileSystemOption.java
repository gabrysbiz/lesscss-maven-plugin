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

public class PluginFileSystemOption extends FileSystemOption {

    private final boolean cacheContent;
    private final DateProviderConfig dateProviderConfig;

    PluginFileSystemOption(final String className, final Map<String, String> parameters, final boolean cacheContent,
            final DateProviderConfig dateProviderConfig) {

        super(className, parameters);
        if (dateProviderConfig == null) {
            throw new IllegalArgumentException("Date provider config cannot be null");
        }
        this.cacheContent = cacheContent;
        this.dateProviderConfig = dateProviderConfig;
    }

    public boolean isCacheContent() {
        return cacheContent;
    }

    public DateProviderConfig getDateProviderConfig() {
        return dateProviderConfig;
    }
}
