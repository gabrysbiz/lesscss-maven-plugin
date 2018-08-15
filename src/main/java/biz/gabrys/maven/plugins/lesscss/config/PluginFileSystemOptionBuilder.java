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

import java.util.Collections;
import java.util.Map;

public class PluginFileSystemOptionBuilder {

    private String className;
    private Map<String, String> parameters;
    private boolean cacheContent;
    private DateProviderConfig dateProviderConfig;

    public PluginFileSystemOptionBuilder className(final String className) {
        this.className = className;
        return this;
    }

    public PluginFileSystemOptionBuilder parameters(final Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public PluginFileSystemOptionBuilder cacheContent(final boolean cacheContent) {
        this.cacheContent = cacheContent;
        return this;
    }

    public PluginFileSystemOptionBuilder dateProvider(final String className, final String methodName, final boolean staticMethod) {
        dateProviderConfig = new DateProviderConfig(className, methodName, staticMethod);
        return this;
    }

    public PluginFileSystemOption build() {
        final Map<String, String> params = parameters != null ? parameters : Collections.<String, String>emptyMap();
        return new PluginFileSystemOption(className, params, cacheContent, dateProviderConfig);
    }
}
