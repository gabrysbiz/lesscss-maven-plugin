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
package biz.gabrys.maven.plugins.lesscss.compiler;

import java.util.regex.Pattern;

import biz.gabrys.lesscss.extended.compiler.control.processor.PostCompilationProcessor;
import biz.gabrys.lesscss.extended.compiler.source.LessSource;

public class PathInCommentPostProcessor implements PostCompilationProcessor {

    private final String classPattern;

    public PathInCommentPostProcessor(final String className) {
        final StringBuilder pattern = new StringBuilder();
        pattern.append("\\.");
        pattern.append(Pattern.quote(className));
        pattern.append("-(\\d)+(\\s)*\\{(\\s)*name:(\\s)*\"");
        classPattern = pattern.toString();
    }

    public String prepare(final LessSource source, final String compiledCode) {
        final String[] parts = compiledCode.split(classPattern);
        if (parts.length == 1) {
            return parts[0];
        }

        final StringBuilder processedCode = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; ++i) {
            String part = parts[i];
            final StringBuilder comment = new StringBuilder();

            if (part.startsWith(PathInCommentSourceCodeCache.START_CLASS_PREFIX)) {
                comment.append("/* Start ");
                part = part.substring(PathInCommentSourceCodeCache.START_CLASS_PREFIX.length());
            } else {
                comment.append("/* End ");
                part = part.substring(PathInCommentSourceCodeCache.END_CLASS_PREFIX.length());
            }

            int index = part.indexOf('"');
            comment.append(part.substring(0, index));
            comment.append(" */");
            processedCode.append(comment);

            index = part.indexOf('}');
            processedCode.append(part.substring(index + 1));
        }
        return processedCode.toString();
    }
}
