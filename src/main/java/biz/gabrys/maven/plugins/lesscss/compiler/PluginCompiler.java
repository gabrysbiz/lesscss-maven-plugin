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

import org.apache.maven.plugin.MojoFailureException;

import biz.gabrys.lesscss.compiler.CompilerException;
import biz.gabrys.lesscss.compiler.CompilerOptions;
import biz.gabrys.lesscss.extended.compiler.ExtendedCompiler;
import biz.gabrys.lesscss.extended.compiler.ExtendedCompilerException;
import biz.gabrys.lesscss.extended.compiler.cache.CompilationDateCache;
import biz.gabrys.lesscss.extended.compiler.source.LessSource;

public class PluginCompiler {

    private final ExtendedCompiler compiler;
    private final CompilationDateCache cache;
    private Date compilationDate;

    public PluginCompiler(final ExtendedCompiler compiler, final CompilationDateCache cache) {
        this.cache = cache;
        this.compiler = compiler;
    }

    public String compile(final LessSource source, final CompilerOptions options) throws MojoFailureException {
        compilationDate = new Date();
        String compiledCode;
        try {
            compiledCode = compiler.compile(source, options);
        } catch (final ExtendedCompilerException e) {
            throw new MojoFailureException(e.getMessage(), e);
        } catch (final CompilerException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }

        if (cache != null && cache.hasCompilationDate(source)) {
            compilationDate = cache.getCompilationDate(source);
        }
        return compiledCode;
    }

    public Date getCompilationDate() {
        return (Date) compilationDate.clone();
    }
}
