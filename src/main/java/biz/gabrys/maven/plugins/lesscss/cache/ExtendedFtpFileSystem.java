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
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import biz.gabrys.lesscss.compiler2.filesystem.FtpFileSystem;

public class ExtendedFtpFileSystem extends FtpFileSystem implements LastModifiedDateProvider {

    @Override
    public Date getLastModified(final String path) throws IOException {
        final URL url = new URL(path);
        FTPClient connection = null;
        final FTPFile[] files;
        try {
            connection = makeConnection(url);
            files = connection.listFiles(url.getPath());
        } finally {
            disconnect(connection);
        }
        if (files.length == 0) {
            return null;
        }
        final Calendar date = files[0].getTimestamp();
        if (date != null) {
            return date.getTime();
        }
        return null;
    }
}
