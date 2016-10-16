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

import org.apache.maven.plugin.logging.Log;

import biz.gabrys.lesscss.extended.compiler.cache.CompiledCodeCache;
import biz.gabrys.lesscss.extended.compiler.source.LessSource;

public class LoggingCompiledCodeCache implements CompiledCodeCache {

    private final CompiledCodeCache cache;
    private final Log logger;

    public LoggingCompiledCodeCache(final CompiledCodeCache cache, final Log logger) {
        this.cache = cache;
        this.logger = logger;
    }

    public void saveCompiledCode(final LessSource source, final String compiledCode) {
        cache.saveCompiledCode(source, compiledCode);
    }

    public boolean hasCompiledCode(final LessSource source) {
        return cache.hasCompiledCode(source);
    }

    public String getCompiledCode(final LessSource source) {
        logger.info("Skips compilation and returns cached CSS compiled code (data not modified)");
        return cache.getCompiledCode(source);
    }
}
