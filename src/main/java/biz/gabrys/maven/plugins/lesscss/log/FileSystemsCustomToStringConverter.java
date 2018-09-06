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
package biz.gabrys.maven.plugins.lesscss.log;

import java.util.Map;
import java.util.Map.Entry;

import biz.gabrys.maven.plugin.util.parameter.converter.ValueToStringConverter;
import biz.gabrys.maven.plugins.lesscss.FileSystemsOptions.CustomFileSystem;
import biz.gabrys.maven.plugins.lesscss.FileSystemsOptions.CustomFileSystem.DateProvider;

public class FileSystemsCustomToStringConverter implements ValueToStringConverter {

    private static final String PADDING_1 = "              ";
    private static final String PADDING_2 = PADDING_1 + "  ";
    private static final String PADDING_3 = PADDING_2 + "  ";

    @Override
    public String convert(final Object value) {
        final CustomFileSystem[] fileSystems = (CustomFileSystem[]) value;
        final StringBuilder text = new StringBuilder();
        text.append('[');
        appendFileSystems(text, fileSystems);
        text.append(']');
        return text.toString();
    }

    protected void appendFileSystems(final StringBuilder text, final CustomFileSystem[] fileSystems) {
        for (int i = 0; i < fileSystems.length; ++i) {
            text.append("{\n");
            appendFileSystem(text, fileSystems[i]);
            text.append('\n');
            text.append(PADDING_1);
            text.append('}');
            if (i != fileSystems.length - 1) {
                text.append(", ");
            }
        }
    }

    protected void appendFileSystem(final StringBuilder text, final CustomFileSystem fileSystem) {
        appendClassName(text, fileSystem.getClassName());
        text.append('\n');
        appendCacheContent(text, fileSystem);
        text.append('\n');
        appendCacheRedirects(text, fileSystem);
        text.append('\n');
        appendParameters(text, fileSystem.getParameters());
        text.append('\n');
        appendDateProvider(text, fileSystem);
    }

    protected void appendClassName(final StringBuilder text, final String className) {
        text.append(PADDING_2);
        text.append("className: ");
        text.append(className);
    }

    protected void appendCacheContent(final StringBuilder text, final CustomFileSystem fileSystem) {
        text.append(PADDING_2);
        text.append("cacheContent: ");
        text.append(fileSystem.getCacheContent());
        appendCalculated(text, fileSystem.getCacheContent(), fileSystem.isCacheContent());
    }

    protected void appendCacheRedirects(final StringBuilder text, final CustomFileSystem fileSystem) {
        text.append(PADDING_2);
        text.append("cacheRedirects: ");
        text.append(fileSystem.getCacheRedirects());
        appendCalculated(text, fileSystem.getCacheRedirects(), fileSystem.isCacheRedirects());
    }

    protected void appendParameters(final StringBuilder text, final Map<String, String> parameters) {
        text.append(PADDING_2);
        text.append("parameters: ");
        if (parameters.isEmpty()) {
            text.append("{}");
        } else {
            text.append("{\n");
            for (final Entry<String, String> entry : parameters.entrySet()) {
                text.append(PADDING_3);
                text.append(entry.getKey());
                text.append(": ");
                text.append(entry.getValue());
                text.append('\n');
            }
            text.append(PADDING_2);
            text.append('}');
        }
    }

    protected void appendDateProvider(final StringBuilder text, final CustomFileSystem fileSystem) {
        text.append(PADDING_2);
        text.append("dateProvider: ");
        final DateProvider dateProvider = fileSystem.getDateProvider();
        if (dateProvider == null) {
            text.append("null");
        } else {
            text.append("{\n");
            text.append(PADDING_3);
            text.append("className: ");
            text.append(dateProvider.getClassName());
            appendCalculated(text, dateProvider.getClassName(), fileSystem.getClassName());
            text.append('\n');
            text.append(PADDING_3);
            text.append("methodName: ");
            text.append(dateProvider.getMethodName());
            text.append('\n');
            text.append(PADDING_3);
            text.append("staticMethod: ");
            text.append(dateProvider.getStaticMethod());
            appendCalculated(text, dateProvider.getStaticMethod(), dateProvider.isStaticMethod());
            text.append('\n');
            text.append(PADDING_2);
            text.append('}');
        }
    }

    private static void appendCalculated(final StringBuilder text, final Object value, final Object calculatedValue) {
        if (value == null) {
            text.append(" (calculated: ");
            text.append(calculatedValue);
            text.append(')');
        }
    }
}
