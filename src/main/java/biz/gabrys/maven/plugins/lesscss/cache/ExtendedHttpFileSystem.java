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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import biz.gabrys.lesscss.compiler2.filesystem.HttpFileSystem;

public class ExtendedHttpFileSystem extends HttpFileSystem implements LastModifiedDateProvider {

    @Override
    public Date getLastModified(final String path) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = makeConnection(new URL(path), false);
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                final long lastModified = connection.getLastModified();
                if (lastModified != 0) {
                    return new Date(lastModified);
                }
            }
            return null;
        } finally {
            disconnect(connection);
        }
    }
}
