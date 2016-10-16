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

import java.io.File;

import biz.gabrys.lesscss.extended.compiler.cache.SourceCodeCache;
import biz.gabrys.lesscss.extended.compiler.source.LessSource;

public class PathInCommentSourceCodeCache implements SourceCodeCache {

    public static final String START_CLASS_PREFIX = "start-";
    public static final String END_CLASS_PREFIX = "end-";

    private final SourceCodeCache cache;
    private final String className;

    public PathInCommentSourceCodeCache(final SourceCodeCache cache, final String className) {
        this.cache = cache;
        this.className = className;
    }

    public void saveSourceCode(final LessSource source, final String sourceCode) {
        final StringBuilder newSourceCode = new StringBuilder();
        newSourceCode.append(createClass(source, true));
        newSourceCode.append(sourceCode);
        newSourceCode.append(createClass(source, false));
        cache.saveSourceCode(source, newSourceCode.toString());
    }

    private CharSequence createClass(final LessSource source, final boolean start) {
        final StringBuilder css = new StringBuilder(100);
        if (!start) {
            css.append('\n');
        }
        css.append('.');
        css.append(className);
        css.append('-');
        css.append(System.currentTimeMillis());
        css.append("{name:\"");
        css.append(start ? START_CLASS_PREFIX : END_CLASS_PREFIX);
        css.append(source.getPath());
        css.append("\";}");
        if (start) {
            css.append('\n');
        }
        return css;
    }

    public boolean hasSourceCode(final LessSource source) {
        return cache.hasSourceCode(source);
    }

    public File getSourceFile(final LessSource source) {
        return cache.getSourceFile(source);
    }

    public String getSourceRelativePath(final LessSource source) {
        return cache.getSourceRelativePath(source);
    }
}
