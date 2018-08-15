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

public class DateProviderConfig {

    private final String className;
    private final String methodName;
    private final boolean staticMethod;

    public DateProviderConfig(final String className, final String methodName, final boolean staticMethod) {
        this.className = className;
        this.methodName = methodName;
        this.staticMethod = staticMethod;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isStaticMethod() {
        return staticMethod;
    }
}
