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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biz.gabrys.lesscss.compiler2.FileSystemOption;
import biz.gabrys.lesscss.compiler2.FileSystemOptionBuilder;
import biz.gabrys.lesscss.compiler2.FileSystemOptionsBuilder;
import biz.gabrys.lesscss.compiler2.filesystem.FileSystem;

public class FileSystemsOptions {

    protected boolean classpathEnabled = true;
    protected String classpathDependenciesTypes = "jar,war";
    protected boolean httpEnabled = true;
    protected boolean ftpEnabled = true;
    protected boolean localEnabled = true;
    protected CustomFileSystem[] customs = new CustomFileSystem[0];

    public boolean isClasspathEnabled() {
        return classpathEnabled;
    }

    public boolean isHttpEnabled() {
        return httpEnabled;
    }

    public boolean isFtpEnabled() {
        return ftpEnabled;
    }

    public boolean isLocalEnabled() {
        return localEnabled;
    }

    public CustomFileSystem[] getCustoms() {
        return customs;
    }

    public List<FileSystemOption> toFileSystemOptions() {
        final FileSystemOptionsBuilder builder = new FileSystemOptionsBuilder();

        for (final CustomFileSystem customfileSystem : customs) {
            builder.appendCustom(new FileSystemOptionBuilder()//
                    .withClass(customfileSystem.getClassName())//
                    .appendParameters(customfileSystem.getParameters())//
                    .build());
        }

        if (classpathEnabled) {
            builder.appendClassPath();
        }
        if (httpEnabled) {
            builder.appendHttp();
        }
        if (ftpEnabled) {
            builder.appendFtp();
        }
        if (localEnabled) {
            builder.appendLocal();
        }

        return builder.build();
    }

    public static class CustomFileSystem {

        protected String className;
        protected Boolean cacheRedirects;
        protected Boolean cacheContent;
        protected Parameter[] parameters = new Parameter[0];
        protected DateProvider dateProvider = new DateProvider();

        public CustomFileSystem() {
            // do nothing
        }

        public CustomFileSystem(final Class<? extends FileSystem> clazz, final boolean cacheContent) {
            className = clazz.getName();
            this.cacheContent = cacheContent;
        }

        public String getClassName() {
            return className;
        }

        public Boolean getCacheContent() {
            return cacheContent;
        }

        public boolean isCacheContent() {
            return cacheContent == null || cacheContent.booleanValue();
        }

        public Boolean getCacheRedirects() {
            return cacheRedirects;
        }

        public boolean isCacheRedirects() {
            return cacheRedirects == null || cacheRedirects.booleanValue();
        }

        public Map<String, String> getParameters() {
            final Map<String, String> parametersMap = new LinkedHashMap<String, String>();
            for (final Parameter parameter : parameters) {
                parametersMap.put(parameter.name, parameter.value);
            }
            return parametersMap;
        }

        public DateProvider getDateProvider() {
            return dateProvider.isSet() ? dateProvider : null;
        }

        public static class Parameter {

            protected String name;
            protected String value;
        }

        public static class DateProvider {

            protected String className;
            protected String methodName;
            protected Boolean staticMethod;

            public boolean isSet() {
                return className != null || methodName != null || staticMethod != null;
            }

            public String getClassName() {
                return className;
            }

            public String getMethodName() {
                return methodName;
            }

            public Boolean getStaticMethod() {
                return staticMethod;
            }

            public boolean isStaticMethod() {
                return staticMethod == null || staticMethod.booleanValue();
            }
        }
    }
}
