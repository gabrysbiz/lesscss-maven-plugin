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
package biz.gabrys.maven.plugins.lesscss.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FileManager {

    private static final Charset CHARSET = Charset.defaultCharset();

    public String readString(final File file) throws IOException {
        return FileUtils.readFileToString(file, CHARSET);
    }

    public void writeString(final File file, final String text) throws IOException {
        FileUtils.writeStringToFile(file, text, CHARSET);
    }

    public List<String> readLines(final File file) throws IOException {
        return FileUtils.readLines(file, CHARSET);
    }

    public void writeLines(final File file, final Collection<String> lines) throws IOException {
        FileUtils.writeLines(file, CHARSET.name(), lines);
    }

    public byte[] readByteArray(final File file) throws IOException {
        return FileUtils.readFileToByteArray(file);
    }

    public void writeByteArray(final File file, final byte[] data) throws IOException {
        FileUtils.writeByteArrayToFile(file, data);
    }

    public void createEmptyFile(final File file) throws IOException {
        if (!file.createNewFile()) {
            throw new IOException("Cannot create file: " + file.getAbsolutePath());
        }
    }

    public void delete(final File file) throws IOException {
        if (!file.exists()) {
            return;
        }

        if (file.isFile()) {
            deleteFile(file);
        } else if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            throw new IOException(String.format("Cannot delete %s (supports only files and directories)", file.getAbsolutePath()));
        }
    }

    protected void deleteFile(final File file) throws IOException {
        if (!file.delete()) {
            throw new IOException("Cannot delete file: " + file.getAbsolutePath());
        }
    }

    protected void deleteDirectory(final File directory) throws IOException {
        FileUtils.deleteDirectory(directory);
    }
}
