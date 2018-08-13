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
package biz.gabrys.maven.plugins.lesscss;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import biz.gabrys.lesscss.compiler2.LessOptions;
import biz.gabrys.lesscss.compiler2.LessOptionsBuilder;
import biz.gabrys.lesscss.compiler2.LessVariableOptionsBuilder;
import biz.gabrys.lesscss.compiler2.LineNumbersValue;

public class Options {

    protected boolean silent;
    protected boolean strictImports;
    protected boolean compress;
    protected boolean ieCompatibility = true;
    protected boolean javaScript = true;
    protected String[] includePaths = new String[0];
    protected String lineNumbers = LineNumbersValue.OFF.getValue();
    protected String rootPath;
    protected boolean relativeUrls;
    protected boolean strictMath;
    protected boolean strictUnits;

    protected String sourceMapRootPath;
    protected String sourceMapBasePath;
    protected boolean sourceMapLessInline;
    protected String sourceMapUrl;

    protected Banner banner = new Banner();
    protected Map<String, String> globalVariables = new LinkedHashMap<String, String>();
    protected Map<String, String> modifyVariables = new LinkedHashMap<String, String>();

    public LessOptions toLessOptions(final String encoding) throws IOException {

        return new LessOptionsBuilder() //
                .silent(silent) //
                .strictImports(strictImports) //
                .compress(compress) //
                .ieCompatibility(ieCompatibility) //
                .javaScript(javaScript) //
                .includePaths(includePaths) //
                .lineNumbers(LineNumbersValue.toLineNumbersValue(lineNumbers)) //
                .rootPath(rootPath) //
                .relativeUrls(relativeUrls) //
                .strictMath(strictMath) //
                .strictUnits(strictUnits) //
                .sourceMapRootPath(sourceMapRootPath) //
                .sourceMapBasePath(sourceMapBasePath) //
                .sourceMapLessInline(sourceMapLessInline) //
                .sourceMapUrl(sourceMapUrl) //
                .encoding(encoding) //
                .banner(bannerToString(encoding)) //
                .globalVariables(new LessVariableOptionsBuilder().append(globalVariables).build()) //
                .modifyVariables(new LessVariableOptionsBuilder().append(modifyVariables).build()) //
                .build();
    }

    private String bannerToString(final String encoding) throws IOException {
        if (banner.text != null) {
            return banner.text;
        }
        if (banner.file != null) {
            return FileUtils.readFileToString(banner.file, encoding);
        }
        return null;
    }

    public static class Banner {
        protected File file;
        protected String text;
    }
}
