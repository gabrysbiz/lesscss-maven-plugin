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

import java.io.File;
import java.util.Date;

import biz.gabrys.lesscss.compiler2.filesystem.LocalFileSystem;

public class ExtendedLocalFileSystem extends LocalFileSystem implements LastModifiedDateProvider {

    @Override
    public Date getLastModified(final String path) {
        final long date = new File(path).lastModified();
        if (date > 0) {
            return new Date(date);
        }
        return null;
    }
}
