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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExtendedCachingRedirectsHttpFileSystem extends ExtendedHttpFileSystem {

    private final Map<String, String> redirectedUrls = new HashMap<String, String>();
    private final Set<String> existingUrls = new HashSet<String>();

    @Override
    public String expandRedirection(final String path) throws IOException, URISyntaxException {
        final String redirectedUrl = redirectedUrls.get(path);
        if (redirectedUrl != null) {
            return redirectedUrl;
        }

        final Set<String> redirectedPaths = new HashSet<String>();
        final String finalPath = expandRedirection(path, redirectedPaths);
        for (final String redirectedPath : redirectedPaths) {
            redirectedUrls.put(redirectedPath, finalPath);
        }
        redirectedUrls.put(finalPath, finalPath);
        return finalPath;
    }

    private String expandRedirection(final String path, final Set<String> redirectedPaths) throws IOException, URISyntaxException {
        HttpURLConnection connection = null;
        String redirectedPath;
        try {
            connection = makeConnection(new URL(path), false);
            final int responseCode = connection.getResponseCode();
            validateResponseCode(responseCode, OK_NOTFOUND_REDIRECT_CODES);
            if (OK_NOTFOUND_CODES.contains(responseCode)) {
                if (HttpURLConnection.HTTP_OK == responseCode) {
                    existingUrls.add(path);
                }
                return path;
            }
            redirectedPaths.add(path);
            final String location = connection.getHeaderField("Location");
            redirectedPath = connection.getURL().toURI().resolve(location).toString();
        } finally {
            disconnect(connection);
        }
        return expandRedirection(redirectedPath, redirectedPaths);
    }

    @Override
    public boolean exists(final String path) throws IOException {
        return existingUrls.contains(path);
    }
}
