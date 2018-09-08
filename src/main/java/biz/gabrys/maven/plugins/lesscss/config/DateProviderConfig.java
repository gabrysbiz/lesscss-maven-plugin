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

import biz.gabrys.lesscss.compiler2.util.StringUtils;

public class DateProviderConfig {

    private final String className;
    private final String methodName;
    private final boolean staticMethod;

    protected DateProviderConfig(final String className, final String methodName, final boolean staticMethod) {
        if (StringUtils.isBlank(className)) {
            throw new IllegalArgumentException("Class name cannot be blank");
        }
        if (StringUtils.isBlank(methodName)) {
            throw new IllegalArgumentException("Method name cannot be blank");
        }
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + className.hashCode();
        result = prime * result + methodName.hashCode();
        return prime * result + (staticMethod ? 1231 : 1237);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final DateProviderConfig other = (DateProviderConfig) obj;
        return staticMethod == other.staticMethod && className.equals(other.className) && methodName.equals(other.methodName);
    }
}
