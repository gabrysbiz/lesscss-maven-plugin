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

import biz.gabrys.lesscss.compiler2.filesystem.FileSystem;
import biz.gabrys.maven.plugins.lesscss.config.DateProviderConfig;
import biz.gabrys.maven.plugins.lesscss.config.PluginFileSystemOption;

public class LastModifiedDateProviderBuilder {

    public LastModifiedDateProvider create(final PluginFileSystemOption option) throws Exception {
        final FileSystem fileSystem = (FileSystem) Class.forName(option.getClassName()).getConstructor().newInstance();
        fileSystem.configure(option.getParameters());
        if (fileSystem instanceof LastModifiedDateProvider) {
            return (LastModifiedDateProvider) fileSystem;
        }

        final DateProviderConfig dateProviderConfig = option.getDateProviderConfig();
        if (dateProviderConfig.getClassName() == null || option.getClassName().equals(dateProviderConfig.getClassName())) {
            return createFromFileSystem(fileSystem, dateProviderConfig);
        }
        return createFromDateProviderConfig(fileSystem, dateProviderConfig);
    }

    protected LastModifiedDateProvider createFromFileSystem(final FileSystem fileSystem, final DateProviderConfig dateProviderConfig)
            throws NoSuchMethodException {
        final Method method = fileSystem.getClass().getMethod(dateProviderConfig.getMethodName(), String.class);
        final Object instance = dateProviderConfig.isStaticMethod() ? null : fileSystem;
        return new LastModifiedDateProvider() {

            @Override
            public boolean isSupported(final String path) {
                return fileSystem.isSupported(path);
            }

            @Override
            public Date getLastModified(final String path) throws Exception {
                return (Date) method.invoke(instance, path);
            }
        };
    }

    protected LastModifiedDateProvider createFromDateProviderConfig(final FileSystem fileSystem,
            final DateProviderConfig dateProviderConfig) throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        final Class<?> dateProviderClass = Class.forName(dateProviderConfig.getClassName());
        final Object instance = dateProviderConfig.isStaticMethod() ? null : dateProviderClass.getConstructor().newInstance();
        final Method method = dateProviderClass.getMethod(dateProviderConfig.getMethodName(), String.class, FileSystem.class);
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
