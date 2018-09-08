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
package biz.gabrys.maven.plugins.lesscss.config;

import org.apache.commons.codec.digest.DigestUtils;

public enum HashAlgorithm {

    MD5(new Hasher() {
        @Override
        public String hash(final String text) {
            return DigestUtils.md5Hex(text);
        }
    }), //
    SHA1(new Hasher() {
        @Override
        public String hash(final String text) {
            return DigestUtils.sha1Hex(text);
        }
    }), //
    SHA256(new Hasher() {
        @Override
        public String hash(final String text) {
            return DigestUtils.sha256Hex(text);
        }
    }), //
    SHA384(new Hasher() {
        @Override
        public String hash(final String text) {
            return DigestUtils.sha384Hex(text);
        }
    }), //
    SHA512(new Hasher() {
        @Override
        public String hash(final String text) {
            return DigestUtils.sha512Hex(text);
        }
    });

    private Hasher hasher;

    HashAlgorithm(final Hasher hasher) {
        this.hasher = hasher;
    }

    public Hasher getHasher() {
        return hasher;
    }

    public interface Hasher {

        String hash(String text);
    }
}
