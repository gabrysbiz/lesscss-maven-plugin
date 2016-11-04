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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import biz.gabrys.lesscss.compiler.CompilerOptions;
import biz.gabrys.lesscss.compiler.CompilerOptionsBuilder;
import biz.gabrys.lesscss.extended.compiler.CachingCompiledCodeExtendedCompiler;
import biz.gabrys.lesscss.extended.compiler.CachingSourceCodeExtendedCompilerBuilder;
import biz.gabrys.lesscss.extended.compiler.ExtendedCompiler;
import biz.gabrys.lesscss.extended.compiler.NonCachingExtendedCompilerBuilder;
import biz.gabrys.lesscss.extended.compiler.cache.FullCache;
import biz.gabrys.lesscss.extended.compiler.cache.FullCacheAdapterBuilder;
import biz.gabrys.lesscss.extended.compiler.cache.FullCacheBuilder;
import biz.gabrys.lesscss.extended.compiler.control.expiration.CompiledSourceExpirationChecker;
import biz.gabrys.lesscss.extended.compiler.control.processor.PostCompilationProcessor;
import biz.gabrys.lesscss.extended.compiler.source.LocalSource;
import biz.gabrys.lesscss.extended.compiler.source.SourceFactory;
import biz.gabrys.lesscss.extended.compiler.source.SourceFactoryBuilder;
import biz.gabrys.maven.plugin.util.classpath.ContextClassLoaderExtender;
import biz.gabrys.maven.plugin.util.io.DestinationFileCreator;
import biz.gabrys.maven.plugin.util.io.FileScanner;
import biz.gabrys.maven.plugin.util.io.ScannerFactory;
import biz.gabrys.maven.plugin.util.io.ScannerPatternFormat;
import biz.gabrys.maven.plugin.util.parameter.ParametersLogBuilder;
import biz.gabrys.maven.plugin.util.parameter.converter.ValueToStringConverter;
import biz.gabrys.maven.plugin.util.parameter.sanitizer.LazySimpleSanitizer;
import biz.gabrys.maven.plugin.util.parameter.sanitizer.LazySimpleSanitizer.ValueContainer;
import biz.gabrys.maven.plugin.util.parameter.sanitizer.SimpleSanitizer;
import biz.gabrys.maven.plugin.util.timer.SystemTimer;
import biz.gabrys.maven.plugin.util.timer.Time;
import biz.gabrys.maven.plugin.util.timer.Timer;
import biz.gabrys.maven.plugins.lesscss.compiler.LoggingCompilationDateCache;
import biz.gabrys.maven.plugins.lesscss.compiler.LoggingCompiledCodeCache;
import biz.gabrys.maven.plugins.lesscss.compiler.LoggingCompiler;
import biz.gabrys.maven.plugins.lesscss.compiler.PathInCommentPostProcessor;
import biz.gabrys.maven.plugins.lesscss.compiler.PathInCommentSourceCodeCache;
import biz.gabrys.maven.plugins.lesscss.compiler.PluginCompiler;
import biz.gabrys.maven.plugins.lesscss.compiler.PluginSourceExpirationChecker;

/**
 * Compiles <a href="http://lesscss.org/">Less</a> files to <a href="http://www.w3.org/Style/CSS/">CSS</a> stylesheets
 * using <a href="http://lesscss-extended-compiler.projects.gabrys.biz/">extended version</a> of the
 * <a href="http://lesscss-compiler.projects.gabrys.biz/">LessCSS Compiler</a>.
 * @since 1.0
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.RUNTIME_PLUS_SYSTEM,
        threadSafe = true)
public class CompileMojo extends AbstractMojo {

    /**
     * Defines whether to skip the plugin execution.
     * @since 1.0
     */
    @Parameter(property = "lesscss.skip", defaultValue = "false")
    protected boolean skip;

    /**
     * Defines whether the plugin runs in verbose mode.<br>
     * <b>Notice</b>: always true in debug mode.
     * @since 1.0
     */
    @Parameter(property = "lesscss.verbose", defaultValue = "false")
    protected boolean verbose;

    /**
     * Forces the <a href="http://lesscss.org/">Less</a> compiler to always compile the
     * <a href="http://lesscss.org/">Less</a> sources. By default <a href="http://lesscss.org/">Less</a> sources are
     * only compiled when modified (including imports) or the <a href="http://www.w3.org/Style/CSS/">CSS</a> stylesheet
     * does not exist.<br>
     * <b>Notice</b>: always false when <a href="#watch">watch</a> is equal to true.
     * @since 1.0
     */
    @Parameter(property = "lesscss.force", defaultValue = "false")
    protected boolean force;

    /**
     * Defines whether the plugin should always overwrite destination files (also if sources did not changed).<br>
     * <b>Notice</b>: always true when <a href="#force">force</a> is equal to true.
     * @since 1.0
     */
    @Parameter(property = "lesscss.alwaysOverwrite", defaultValue = "false")
    protected boolean alwaysOverwrite;

    /**
     * The directory which contains the <a href="http://lesscss.org/">Less</a> sources.
     * @since 1.0
     */
    @Parameter(property = "lesscss.sourceDirectory", defaultValue = "${project.basedir}/src/main/less")
    protected File sourceDirectory;

    /**
     * The directory for compiled <a href="http://www.w3.org/Style/CSS/">CSS</a> stylesheets.
     * @since 1.0
     */
    @Parameter(property = "lesscss.outputDirectory", defaultValue = "${project.build.directory}")
    protected File outputDirectory;

    /**
     * Defines inclusion and exclusion fileset patterns format. Available options:
     * <ul>
     * <li><b>ant</b> - <a href="http://ant.apache.org/">Ant</a>
     * <a href="http://ant.apache.org/manual/dirtasks.html#patterns">patterns</a></li>
     * <li><b>regex</b> - regular expressions (use '/' as path separator)</li>
     * </ul>
     * @since 1.0
     */
    @Parameter(property = "lesscss.filesetPatternFormat", defaultValue = "ant")
    protected String filesetPatternFormat;

    /**
     * List of files to include. Specified as fileset patterns whose are relative to the
     * <a href="#sourceDirectory">source directory</a>. See <a href="#filesetPatternFormat">available fileset patterns
     * formats</a>.<br>
     * <b>Default value is</b>: <tt>["&#42;&#42;/&#42;.less"]</tt> for <a href="#filesetPatternFormat">ant</a> or
     * <tt>["^.+\.less$"]</tt> for <a href="#filesetPatternFormat">regex</a>.
     * @since 1.0
     */
    @Parameter
    protected String[] includes = new String[0];

    /**
     * List of files to exclude. Specified as fileset patterns whose are relative to the
     * <a href="#sourceDirectory">source directory</a>. See <a href="#filesetPatternFormat">available fileset patterns
     * formats</a>.<br>
     * <b>Default value is</b>: <tt>[]</tt>.
     * @since 1.0
     */
    @Parameter
    protected String[] excludes = new String[0];

    /**
     * Defines compiler type used in compilation process. Available options:
     * <ul>
     * <li><b>full</b> - designed to compile files placed on a local hard drive, in the classpath and in the network
     * </li>
     * <li><b>local</b> - designed to compile files placed on a local hard drive</li>
     * </ul>
     * @since 1.0
     */
    @Parameter(property = "lesscss.compilerType", defaultValue = "full")
    protected String compilerType;

    /**
     * Defines types of dependencies whose will be added to plugin classpath (required for <code>classpath://</code>
     * support).<br>
     * <b>Notice</b>: ignored when <a href="#compilerType">compiler type</a> is not equal to <code>full</code>.<br>
     * <b>Default value is</b>: <tt>["jar", "war", "zip"]</tt>.
     * @since 1.2.0
     */
    @Parameter(property = "lesscss.classpathLoadedDependenciesTypes")
    protected String[] classpathLoadedDependenciesTypes = new String[0];

    /**
     * Defines whether the plugin should add comments with sources paths at the beginning and end of each source.<br>
     * <b>Notice</b>: always false when <a href="#compilerType">compiler type</a> is equal to <b>local</b> or
     * <a href="#compress">compress</a> is equal to true.<br>
     * <b>Notice</b>: you must clear the <a href="#workingDirectory">working directory</a> if you change this parameter.
     * @since 1.0
     */
    @Parameter(property = "lesscss.addCommentsWithPaths", defaultValue = "false")
    protected boolean addCommentsWithPaths;

    /**
     * Restricted class name prefix used to create comments with sources paths.<br>
     * <b>Notice</b>: you must clear the <a href="#workingDirectory">working directory</a> if you change this parameter.
     * @since 1.0
     */
    @Parameter(property = "lesscss.addCommentsWithPathsClassPrefix", defaultValue = "gabrys-biz-comment-with-path-marker-class")
    protected String addCommentsWithPathsClassPrefix;

    /**
     * Whether the compiler should minify the <a href="http://www.w3.org/Style/CSS/">CSS</a> code.<br>
     * <b>Notice</b>: you must clear the <a href="#workingDirectory">working directory</a> if you change this parameter
     * and <a href="#force">force</a> is equal to false.
     * @since 1.0
     */
    @Parameter(property = "lesscss.compress", defaultValue = "false")
    protected boolean compress;

    /**
     * List of options passed to the compiler. See
     * <a href="http://lesscss.org/usage/index.html#command-line-usage-options">Less options</a><br>
     * <b>Notice</b>: you must clear the <a href="#workingDirectory">working directory</a> if you change this parameter.
     * <br>
     * <b>Default value is</b>: <tt>[]</tt>.
     * @since 1.0
     */
    @Parameter
    protected String[] compilerOptions = new String[0];

    /**
     * Sources encoding.<br>
     * <b>Notice</b>: you must clear the <a href="#workingDirectory">working directory</a> if you change this parameter
     * and <a href="#force">force</a> is equal to false.
     * @since 1.0
     */
    @Parameter(property = "lesscss.encoding", defaultValue = "${project.build.sourceEncoding}")
    protected String encoding;

    /**
     * Destination files naming format. {fileName} is equal to source file name without extension.
     * @since 1.0
     */
    @Parameter(property = "lesscss.outputFileFormat", defaultValue = DestinationFileCreator.FILE_NAME_PARAMETER + ".css")
    protected String outputFileFormat;

    /**
     * Defines whether the plugin should watch for changes in source files and compile if it detects any.
     * @since 1.0
     */
    @Parameter(property = "lesscss.watch", defaultValue = "false")
    protected boolean watch;

    /**
     * The interval in seconds between the plugin searching for changes in source files.<br>
     * <b>Notice</b>: all values smaller than <code>1</code> are treated as <code>1</code>.
     * @since 1.0
     */
    @Parameter(property = "lesscss.watchInterval", defaultValue = "5")
    protected int watchInterval;

    /**
     * The plugin working directory.
     * @since 1.0
     */
    @Parameter(property = "lesscss.workingDirectory", defaultValue = "${project.build.directory}/gabrys-biz-lesscss-maven-plugin")
    protected File workingDirectory;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    private void logParameters() {
        if (!getLog().isDebugEnabled()) {
            return;
        }

        final ParametersLogBuilder logger = new ParametersLogBuilder(getLog());
        logger.append("skip", skip);
        logger.append("verbose", verbose, new SimpleSanitizer(verbose, Boolean.TRUE));
        logger.append("force", force, new SimpleSanitizer(!watch || watch && !force, Boolean.FALSE));
        logger.append("alwaysOverwrite", alwaysOverwrite, new SimpleSanitizer(!(!watch && force && !alwaysOverwrite), Boolean.TRUE));
        logger.append("sourceDirectory", sourceDirectory);
        logger.append("outputDirectory", outputDirectory);
        logger.append("filesetPatternFormat", filesetPatternFormat);
        logger.append("includes", includes, new LazySimpleSanitizer(includes.length != 0, new ValueContainer() {

            public Object getValue() {
                return getDefaultIncludes();
            }
        }));
        logger.append("excludes", excludes);
        logger.append("compilerType", compilerType);
        logger.append("classpathLoadedDependenciesTypes", classpathLoadedDependenciesTypes,
                new LazySimpleSanitizer(classpathLoadedDependenciesTypes.length != 0, new ValueContainer() {

                    public Object getValue() {
                        return getDefaultClasspathLoadedDependenciesTypes();
                    }
                }));
        logger.append("addCommentsWithPaths", addCommentsWithPaths, new SimpleSanitizer(!compress, Boolean.FALSE));
        logger.append("addCommentsWithPathsClassPrefix", addCommentsWithPathsClassPrefix);
        logger.append("compress", compress);
        logger.append("compilerOptions", compilerOptions);
        logger.append("encoding", encoding);
        logger.append("outputFileFormat", outputFileFormat);
        logger.append("watch", watch);
        logger.append("watchInterval", watchInterval, new ValueToStringConverter() {

            private static final long MILLISECONDS_IN_SECOND = 1000L;

            public String convert(final Object value) {
                final Integer number = (Integer) value;
                final StringBuilder text = new StringBuilder();
                text.append(number);
                if (number > 0) {
                    text.append(" (");
                    text.append(new Time(number * MILLISECONDS_IN_SECOND));
                    text.append(')');
                }
                return text.toString();
            }
        }, new SimpleSanitizer(watchInterval > 0, Integer.valueOf(1)));
        logger.append("workingDirectory", workingDirectory);
        logger.debug();
    }

    private String[] getDefaultIncludes() {
        if (ScannerPatternFormat.ANT.name().equalsIgnoreCase(filesetPatternFormat)) {
            return new String[] { "**/*.less" };
        } else {
            return new String[] { "^.+\\.less$" };
        }
    }

    private static String[] getDefaultClasspathLoadedDependenciesTypes() {
        return new String[] { "jar", "war", "zip" };
    }

    private void calculateParameters() {
        if (getLog().isDebugEnabled()) {
            verbose = true;
        }
        if (watch) {
            force = false;
        }
        if (force) {
            alwaysOverwrite = true;
        }
        if (compress) {
            addCommentsWithPaths = false;
        }
        if (includes.length == 0) {
            includes = getDefaultIncludes();
        }
        if (classpathLoadedDependenciesTypes.length == 0) {
            classpathLoadedDependenciesTypes = getDefaultClasspathLoadedDependenciesTypes();
        }
        if (watchInterval < 1) {
            watchInterval = 1;
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        logParameters();
        if (skip) {
            getLog().info("Skips job execution");
            return;
        }
        calculateParameters();

        if ("full".equals(compilerType)) {
            addDependenciesToClasspath();
        }

        if (watch) {
            runWatchMode();
        } else {
            runCompilation();
        }
    }

    private void addDependenciesToClasspath() throws MojoExecutionException {
        if (verbose) {
            getLog().info("Adding project dependencies to classpath...");
        }
        final ContextClassLoaderExtender extender = new ContextClassLoaderExtender(project, getLog());
        extender.addDependencies(classpathLoadedDependenciesTypes);
    }

    private void runWatchMode() throws MojoFailureException {
        getLog().info("Starts watch mode on the " + sourceDirectory);
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        final long interval = watchInterval * 1000L;
        while (true) {
            runCompilation();
            try {
                Thread.sleep(interval);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void runCompilation() throws MojoFailureException {
        if (!sourceDirectory.exists()) {
            getLog().warn("Source directory does not exist: " + sourceDirectory.getAbsolutePath());
            return;
        }
        final Collection<File> files = getFiles();
        if (files.isEmpty()) {
            getLog().warn("No sources to compile");
            return;
        }

        if (force) {
            deleteWorkingDirectory();
        }
        compileFiles(files);
    }

    private Collection<File> getFiles() {
        final ScannerPatternFormat patternFormat = ScannerPatternFormat.toPattern(filesetPatternFormat);
        final FileScanner scanner = new ScannerFactory().create(patternFormat, getLog());
        if (verbose) {
            getLog().info("Scanning directory for sources...");
        }
        return scanner.getFiles(sourceDirectory, includes, excludes);
    }

    private void deleteWorkingDirectory() throws MojoFailureException {
        if (!workingDirectory.exists()) {
            return;
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Deleting working directory: " + workingDirectory.getAbsolutePath());
        }
        try {
            FileUtils.deleteDirectory(workingDirectory);
        } catch (final IOException e) {
            throw new MojoFailureException(String.format("Cannot delete working directory: %s", workingDirectory.getAbsolutePath()), e);
        }
    }

    private void compileFiles(final Collection<File> files) throws MojoFailureException {
        final PluginCompiler compiler = createCompiler();
        final CompilerOptions options = createOptions();
        final String sourceFilesText = "source" + (files.size() != 1 ? "s" : "");
        getLog().info(String.format("Compiling %s %s to %s", files.size(), sourceFilesText, outputDirectory.getAbsolutePath()));
        final Timer timer = SystemTimer.getStartedTimer();
        for (final File file : files) {
            compileFile(compiler, options, file);
        }
        getLog().info(String.format("Finished %s compilation in %s", sourceFilesText, timer.stop()));
    }

    private PluginCompiler createCompiler() {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Creating compiler...");
        }
        FullCache cache = null;
        ExtendedCompiler compiler;
        SourceFactory sourceFactory;
        if ("full".equalsIgnoreCase(compilerType)) {
            cache = new FullCacheBuilder().withDirectory(workingDirectory).create();
            sourceFactory = new SourceFactoryBuilder().withClasspath().withStandard().create();
            PostCompilationProcessor postProcessor = null;
            if (addCommentsWithPaths) {
                cache = new FullCacheAdapterBuilder(cache)
                        .withSourceCodeCache(new PathInCommentSourceCodeCache(cache, addCommentsWithPathsClassPrefix)).create();
                postProcessor = new PathInCommentPostProcessor(addCommentsWithPathsClassPrefix);
            }
            compiler = new CachingSourceCodeExtendedCompilerBuilder(cache).withSourceFactory(sourceFactory).withPostProcessor(postProcessor)
                    .create();
        } else if ("local".equalsIgnoreCase(compilerType)) {
            sourceFactory = force ? null : new SourceFactoryBuilder().withLocal().create();
            compiler = new NonCachingExtendedCompilerBuilder().create();
        } else {
            throw new IllegalArgumentException(String.format("Cannot find compiler for type \"%s\"", compilerType));
        }
        if (verbose) {
            compiler = new LoggingCompiler(compiler, getLog());
        }
        if (!force) {
            cache = cache != null ? cache : new FullCacheBuilder().withDirectory(workingDirectory).create();
            if (verbose) {
                final FullCacheAdapterBuilder builder = new FullCacheAdapterBuilder(cache);
                builder.withCompiledCodeCache(new LoggingCompiledCodeCache(cache, getLog()));
                if (getLog().isDebugEnabled()) {
                    builder.withCompilationDateCache(new LoggingCompilationDateCache(cache, getLog()));
                }
                cache = builder.create();
            }
            final CompiledSourceExpirationChecker expirationChecker = new PluginSourceExpirationChecker(cache, sourceFactory, getLog());
            compiler = new CachingCompiledCodeExtendedCompiler(compiler, expirationChecker, cache, cache);
        }
        return new PluginCompiler(compiler, cache);
    }

    private CompilerOptions createOptions() {
        final CompilerOptionsBuilder builder = new CompilerOptionsBuilder();
        builder.setMinified(compress);
        final CompilerOptions options = builder.create();
        final List<Object> arguments = new ArrayList<Object>();
        arguments.addAll(options.getArguments());
        arguments.addAll(Arrays.asList(compilerOptions));
        return new CompilerOptions(arguments);
    }

    private void compileFile(final PluginCompiler compiler, final CompilerOptions options, final File source) throws MojoFailureException {
        Timer timer = null;
        if (verbose) {
            getLog().info("Processing Less source: " + source.getAbsolutePath());
            timer = SystemTimer.getStartedTimer();
        }
        final String compiled = compiler.compile(new LocalSource(source, encoding), options);
        saveCompiledCode(source, compiled, compiler.getCompilationDate());
        if (timer != null) {
            getLog().info("Finished in " + timer.stop());
        }
    }

    private void saveCompiledCode(final File source, final String compiled, final Date compilationDate) throws MojoFailureException {
        final File destination = new DestinationFileCreator(sourceDirectory, outputDirectory, outputFileFormat).create(source);

        final boolean skipsFileSaving = !force && !alwaysOverwrite && destination.exists()
                && compilationDate.before(new Date(destination.lastModified()));
        if (skipsFileSaving) {
            if (verbose) {
                getLog().info("Skips saving CSS compiled code to file, because cached version is older than destination file: "
                        + destination.getAbsolutePath());
            }
            return;
        }

        if (verbose) {
            getLog().info("Saving CSS code to " + destination.getAbsolutePath());
        }
        try {
            FileUtils.write(destination, compiled, encoding);
        } catch (final IOException e) {
            throw new MojoFailureException(String.format("Cannot save CSS compiled code to file: %s", destination.getAbsolutePath()), e);
        }
    }
}
