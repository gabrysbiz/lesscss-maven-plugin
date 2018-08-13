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
package biz.gabrys.maven.plugins.lesscss.cache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

import biz.gabrys.lesscss.compiler2.filesystem.FileSystem;

public class LastModifiedDateProviderBuilder {

    private String fileSystemClassName;
    private Map<String, String> fileSystemParameters;
    private String dataProviderClassName;
    private String dataProviderMethodName;
    private boolean dataProviderStaticMethod;

    public LastModifiedDateProviderBuilder fileSystem(final String className, final Map<String, String> parameters) {
        fileSystemClassName = className;
        fileSystemParameters = parameters;
        return this;
    }

    public LastModifiedDateProviderBuilder dataProvider(final String className, final String methodName, final boolean staticMethod) {
        dataProviderClassName = className;
        dataProviderMethodName = methodName;
        dataProviderStaticMethod = staticMethod;
        return this;
    }

    public LastModifiedDateProvider build() throws Exception {
        final FileSystem fileSystem = (FileSystem) Class.forName(fileSystemClassName).getConstructor().newInstance();
        fileSystem.configure(fileSystemParameters);
        if (fileSystem instanceof LastModifiedDateProvider) {
            return (LastModifiedDateProvider) fileSystem;
        }

        final Class<?> clazz = Class.forName(dataProviderClassName);
        final Object instance = dataProviderStaticMethod ? null : clazz.getConstructor().newInstance();
        final Method method = clazz.getMethod(dataProviderMethodName, String.class, FileSystem.class);

        return new LastModifiedDateProvider() {

            @Override
            public boolean isSupported(final String path) {
                return fileSystem.isSupported(path);
            }

            @Override
            public Date getLastModified(final String path) throws IllegalAccessException, InvocationTargetException {
                return (Date) method.invoke(instance, path, fileSystem);
            }
        };
    }
}
