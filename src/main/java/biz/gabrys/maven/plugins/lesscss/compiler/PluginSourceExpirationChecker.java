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
package biz.gabrys.maven.plugins.lesscss.compiler;

import java.util.Date;

import org.apache.maven.plugin.logging.Log;

import biz.gabrys.lesscss.extended.compiler.cache.FullCache;
import biz.gabrys.lesscss.extended.compiler.cache.SourceModificationDateCache;
import biz.gabrys.lesscss.extended.compiler.control.expiration.CompiledSourceExpirationChecker;
import biz.gabrys.lesscss.extended.compiler.control.expiration.CompiledSourceExpirationCheckerImpl;
import biz.gabrys.lesscss.extended.compiler.control.expiration.SourceExpirationChecker;
import biz.gabrys.lesscss.extended.compiler.control.expiration.SourceModificationDateBasedExpirationChecker;
import biz.gabrys.lesscss.extended.compiler.source.LessSource;
import biz.gabrys.lesscss.extended.compiler.source.SourceFactory;

public class PluginSourceExpirationChecker implements CompiledSourceExpirationChecker {

    private final CompiledSourceExpirationChecker expirationChecker;
    private final Log logger;

    public PluginSourceExpirationChecker(final FullCache cache, final SourceFactory sourceFactory, final Log logger) {
        expirationChecker = new CompiledSourceExpirationCheckerWithLogger(cache, sourceFactory, logger);
        this.logger = logger;
    }

    public boolean isExpired(final LessSource source, final Date compilationDate) {
        if (logger.isDebugEnabled()) {
            logger.debug("Verificating whether the source file or at least one import need compilation...");
        }
        final boolean expired = expirationChecker.isExpired(source, compilationDate);
        if (logger.isDebugEnabled() && !expired) {
            logger.debug("Cache for the source file and all imports are up to date");
        }
        return expired;
    }

    private static final class CompiledSourceExpirationCheckerWithLogger extends CompiledSourceExpirationCheckerImpl {

        private final Log logger;

        private CompiledSourceExpirationCheckerWithLogger(final FullCache cache, final SourceFactory sourceFactory, final Log logger) {
            super(new SourceExpirationCheckerWithLogger(cache, logger), cache, cache, sourceFactory);
            this.logger = logger;
        }

        @Override
        protected boolean isModifiedAfterLastCompilation(final LessSource source, final Date lastCompilationDate) {
            final boolean expired = super.isModifiedAfterLastCompilation(source, lastCompilationDate);
            if (logger.isDebugEnabled() && expired) {
                logger.debug(String.format("Cache for source in older than import %s, file need compilation", source.getPath()));
            }
            return expired;
        }
    }

    private static final class SourceExpirationCheckerWithLogger implements SourceExpirationChecker {

        private final SourceExpirationChecker expirationChecker;
        private final Log logger;

        private SourceExpirationCheckerWithLogger(final SourceModificationDateCache cache, final Log logger) {
            expirationChecker = new SourceModificationDateBasedExpirationChecker(cache);
            this.logger = logger;
        }

        public boolean isExpired(final LessSource source) {
            final boolean expired = expirationChecker.isExpired(source);
            if (logger.isDebugEnabled() && expired) {
                logger.debug(String.format("Cache for source file %s expired, file need compilation", source.getPath()));
            }
            return expired;
        }
    }

}
