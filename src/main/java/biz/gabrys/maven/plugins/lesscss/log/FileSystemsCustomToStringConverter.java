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
import biz.gabrys.maven.plugins.lesscss.FileSystems.CustomFileSystem;
import biz.gabrys.maven.plugins.lesscss.FileSystems.CustomFileSystem.DateProvider;

public class FileSystemsCustomToStringConverter implements ValueToStringConverter {

    private static final String PADDING_1 = "              ";
    private static final String PADDING_2 = PADDING_1 + "  ";
    private static final String PADDING_3 = PADDING_2 + "  ";

    @Override
    public String convert(final Object value) {
        final CustomFileSystem[] options = (CustomFileSystem[]) value;
        final StringBuilder text = new StringBuilder();
        text.append('[');
        for (int i = 0; i < options.length; ++i) {
            text.append("{\n");
            appendOption(text, options[i]);
            text.append('\n');
            text.append(PADDING_1);
            text.append('}');
            if (i != options.length - 1) {
                text.append(", ");
            }
        }
        text.append(']');
        return text.toString();
    }

    private void appendOption(final StringBuilder text, final CustomFileSystem option) {
        appendClassName(text, option.getClassName());
        text.append('\n');
        appendCacheContent(text, option);
        text.append('\n');
        appendParameters(text, option.getParameters());
        text.append('\n');
        appendDateProvider(text, option.getDateProvider());
    }

    private void appendClassName(final StringBuilder text, final String className) {
        text.append(PADDING_2);
        text.append("className: ");
        text.append(className);
    }

    private void appendCacheContent(final StringBuilder text, final CustomFileSystem option) {
        text.append(PADDING_2);
        text.append("cacheContent: ");
        text.append(option.getCacheContent());
        if (option.getCacheContent() == null) {
            text.append(" (calculated: ");
            text.append(option.isCacheContent());
            text.append(')');
        }
    }

    private void appendParameters(final StringBuilder text, final Map<String, String> parameters) {
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

    private void appendDateProvider(final StringBuilder text, final DateProvider dateProvider) {
        text.append(PADDING_2);
        text.append("dateProvider: ");
        if (dateProvider == null) {
            text.append("null");
        } else {
            text.append("{\n");
            text.append(PADDING_3);
            text.append("className: ");
            text.append(dateProvider.getClassName());
            text.append('\n');
            text.append(PADDING_3);
            text.append("methodName: ");
            text.append(dateProvider.getMethodName());
            text.append('\n');
            text.append(PADDING_3);
            text.append("staticMethod: ");
            text.append(dateProvider.getStaticMethod());
            if (dateProvider.getStaticMethod() == null) {
                text.append(" (calculated: ");
                text.append(dateProvider.isStaticMethod());
                text.append(')');
            }
            text.append('\n');
            text.append(PADDING_2);
            text.append('}');
        }
    }
}
