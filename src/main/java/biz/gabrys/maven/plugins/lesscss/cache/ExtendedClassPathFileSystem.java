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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import biz.gabrys.lesscss.compiler2.filesystem.ClassPathFileSystem;

public class ExtendedClassPathFileSystem extends ClassPathFileSystem implements LastModifiedDateProvider {

    @Override
    public Date getLastModified(final String path) throws IOException {
        final URL url = getClassLoader().getResource(path);
        if (url == null) {
            return null;
        }
        final long lastModified = makeConnection(url).getLastModified();
        if (lastModified == 0) {
            return null;
        }
        return new Date(lastModified);
    }

    protected URLConnection makeConnection(final URL url) throws IOException {
        return url.openConnection();
    }
}
