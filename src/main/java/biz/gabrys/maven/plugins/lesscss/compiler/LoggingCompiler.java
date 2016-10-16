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

import biz.gabrys.lesscss.compiler.CompilerException;
import biz.gabrys.lesscss.compiler.CompilerOptions;
import biz.gabrys.lesscss.extended.compiler.ExtendedCompiler;
import biz.gabrys.lesscss.extended.compiler.source.LessSource;

public class LoggingCompiler implements ExtendedCompiler {

    private final ExtendedCompiler compiler;
    private final Log logger;

    public LoggingCompiler(final ExtendedCompiler compiler, final Log logger) {
        this.compiler = compiler;
        this.logger = logger;
    }

    public String compile(final LessSource source) throws CompilerException {
        logger.info("Compiling source code...");
        return compiler.compile(source);
    }

    public String compile(final LessSource source, final CompilerOptions options) throws CompilerException {
        logger.info("Compiling source code...");
        return compiler.compile(source, options);
    }
}
