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

import java.util.Date;

import org.apache.maven.plugin.logging.Log;

import biz.gabrys.lesscss.extended.compiler.cache.CompilationDateCache;
import biz.gabrys.lesscss.extended.compiler.source.LessSource;

public class LoggingCompilationDateCache implements CompilationDateCache {

    private final CompilationDateCache cache;
    private final Log logger;

    public LoggingCompilationDateCache(final CompilationDateCache cache, final Log logger) {
        this.cache = cache;
        this.logger = logger;
    }

    public void saveCompilationDate(final LessSource source, final Date compilationDate) {
        cache.saveCompilationDate(source, compilationDate);
    }

    public boolean hasCompilationDate(final LessSource source) {
        final boolean dateAvailable = cache.hasCompilationDate(source);
        if (!dateAvailable) {
            logger.debug("The source file has not been compiled before, file need compilation");
        }
        return dateAvailable;
    }

    public Date getCompilationDate(final LessSource source) {
        return cache.getCompilationDate(source);
    }
}
