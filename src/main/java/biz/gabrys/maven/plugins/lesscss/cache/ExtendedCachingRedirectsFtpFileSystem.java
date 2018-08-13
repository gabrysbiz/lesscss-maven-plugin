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
import java.util.HashSet;
import java.util.Set;

public class ExtendedCachingRedirectsFtpFileSystem extends ExtendedFtpFileSystem {

    private final Set<String> existingUrls = new HashSet<String>();

    @Override
    public boolean exists(final String path) throws IOException {
        if (existingUrls.contains(path)) {
            return true;
        }
        final boolean fileExists = super.exists(path);
        if (fileExists) {
            existingUrls.add(path);
        }
        return fileExists;
    }
}
