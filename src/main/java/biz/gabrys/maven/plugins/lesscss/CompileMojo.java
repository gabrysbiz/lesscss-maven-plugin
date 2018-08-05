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
import java.util.Collection;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import biz.gabrys.lesscss.compiler2.LessCompiler;
import biz.gabrys.lesscss.compiler2.LessOptions;
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
import biz.gabrys.maven.plugin.util.timer.TimeSpan;
import biz.gabrys.maven.plugin.util.timer.Timer;

/**
 * Compiles <a href="http://lesscss.org/">Less</a> files to <a href="http://www.w3.org/Style/CSS/">CSS</a> stylesheets
 * using the <a href="http://lesscss-compiler.projects.gabrys.biz/">LessCSS Compiler</a>.
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
     * Defines whether the plugin should add comments with sources paths at the beginning and end of each source.<br>
     * <b>Notice</b>: always false when <a href="#compilerType">compiler type</a> is equal to <b>local</b> or
     * <a href="#compress">compress</a> is equal to true.<br>
     * <b>Notice</b>: you must clear the <a href="#workingDirectory">working directory</a> if you change this parameter.
     * @since 1.0
     */
    // @Parameter(property = "lesscss.addCommentsWithPaths", defaultValue = "false")
    // protected boolean addCommentsWithPaths;

    /**
     * Restricted class name prefix used to create comments with sources paths.<br>
     * <b>Notice</b>: you must clear the <a href="#workingDirectory">working directory</a> if you change this parameter.
     * @since 1.0
     */
    // @Parameter(property = "lesscss.addCommentsWithPathsClassPrefix", defaultValue =
    // "gabrys-biz-comment-with-path-marker-class")
    // protected String addCommentsWithPathsClassPrefix;

    /**
     * Defines <a href="http://lesscss.org/usage/index.html#less-options">Less compiler options</a> responsible for
     * controlling the compilation process.<br>
     * Base options:
     * <ul>
     * <li><b>compress</b> - whether a CSS code should be compressed (default: <code>false</code>)</li>
     * <li><b>ieCompatibility</b> - whether a CSS code should be compatible with Internet Explorer browser (default:
     * <code>true</code>)</li>
     * <li><b>includePaths</b> - available include paths (default: <code>[]</code>)</li>
     * <li><b>javaScript</b> - whether a compiler should allow usage of JavaScript language (default:
     * <code>true</code>)</li>
     * <li><b>lineNumbers</b> - whether a compiler should generate inline source-mapping:
     * <ul>
     * <li><i>empty</i> - line numbers won't be put in CSS code (default)</li>
     * <li><b>comments</b> - line numbers will be put in CSS comments blocks</li>
     * <li><b>mediaquery</b> - line numbers will be put in &#64;media queries</li>
     * <li><b>all</b> - line numbers will be put in CSS comments blocks and &#64;media queries</li>
     * </ul>
     * </li>
     * <li><b>relativeUrls</b> - whether a compiler should rewrite relative URLs (default: <code>false</code>)</li>
     * <li><b>rootPath</b> - a path which will be added to every generated import and URL in CSS code (default:
     * <code>null</code>)</li>
     * <li><b>silent</b> - whether a compiler shouldn't log compilation warnings (default: <code>false</code>)</li>
     * <li><b>strictImports</b> - whether a compiler should disallow an @import operation inside of either @media blocks
     * or other selector blocks (default: <code>false</code>)</li>
     * <li><b>strictMath</b> - whether a compiler should try and process all maths in Less code (default:
     * <code>false</code>)</li>
     * <li><b>strictUnits</b> - whether a compiler should guess units in Less code when it does maths (default:
     * <code>false</code>)</li>
     * </ul>
     * Source Map options:
     * <ul>
     * <li><b>sourceMapBasePath</b> - a path that will be removed from each of the Less file paths inside the Source Map
     * and also from the path to the map file specified in your output CSS (default: <code>null</code>)</li>
     * <li><b>sourceMapLessInline</b> - whether a compiler should include all of the Less files in to the Source Map
     * (default: <code>false</code>)</li>
     * <li><b>sourceMapRootPath</b> - a path that will be prepended to each of the Less file paths inside the Source Map
     * and also to the path to the map file specified in your output CSS (default: <code>null</code>)</li>
     * <li><b>sourceMapUrl</b> - a path which will overwrite the URL in the CSS that points at the Source Map file
     * (default: <code>null</code>)</li>
     * </ul>
     * Additional options:
     * <ul>
     * <li><b>banner</b> - a banner which will be inserted to source files before the compilation (default:
     * <code>null</code>):
     * <ul>
     * <li><b>file</b> - loads banner form the specified file (ignored when <b>text</b> is set)</li>
     * <li><b>text</b> - uses the specified text</li>
     * </ul>
     * </li>
     * <li><b>globalVariables</b> - variables that can be referenced by the files (default: <code>{}</code>)</li>
     * <li><b>modifyVariables</b> - variables that can overwrite variables defined in the files (default:
     * <code>{}</code>)</li>
     * </ul>
     * Non-standard options:
     * <ul>
     * <li><b>httpEnabled</b> - whether the plugin allows using <code>http://</code> protocol (default
     * <code>true</code>)</li>
     * <li><b>ftpEnabled</b> - whether the plugin allows using <code>ftp://</code> protocol (default
     * <code>true</code>)</li>
     * <li><b>classpathEnabled</b> - whether the plugin allows using <code>classpath://</code> protocol (default
     * <code>true</code>)</li>
     * <li><b>classpathLoadedDependenciesTypes</b> - a comma separated types list of dependencies whose will be added to
     * the plugin class path (default: <code>jar,war</code>)</li>
     * </ul>
     * @since 2.0.0
     */
    @Parameter
    protected Options options = new Options();

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
        // logger.append("force", force, new SimpleSanitizer(!watch || !force, Boolean.FALSE));
        // logger.append("alwaysOverwrite", alwaysOverwrite, new SimpleSanitizer(!(!watch && force && !alwaysOverwrite),
        // Boolean.TRUE));
        logger.append("sourceDirectory", sourceDirectory);
        logger.append("outputDirectory", outputDirectory);
        logger.append("filesetPatternFormat", filesetPatternFormat);
        logger.append("includes", includes, new LazySimpleSanitizer(includes.length != 0, new ValueContainer() {

            @Override
            public Object getValue() {
                return getDefaultIncludes();
            }
        }));
        logger.append("excludes", excludes);
        logger.append("options.silent", options.silent);
        logger.append("options.strictImports", options.strictImports);
        logger.append("options.compress", options.compress);
        logger.append("options.ieCompatibility", options.ieCompatibility);
        logger.append("options.javaScript", options.javaScript);
        logger.append("options.includePaths", options.includePaths);
        logger.append("options.lineNumbers", options.lineNumbers);
        logger.append("options.rootPath", options.rootPath);
        logger.append("options.relativeUrls", options.relativeUrls);
        logger.append("options.strictMath", options.strictMath);
        logger.append("options.strictUnits", options.strictUnits);
        logger.append("options.sourceMapRootPath", options.sourceMapRootPath);
        logger.append("options.sourceMapBasePath", options.sourceMapBasePath);
        logger.append("options.sourceMapLessInline", options.sourceMapLessInline);
        logger.append("options.sourceMapUrl", options.sourceMapUrl);
        logger.append("options.banner.file", options.banner.file);
        logger.append("options.banner.text", options.banner.text);
        logger.append("options.globalVariables", options.globalVariables);
        logger.append("options.modifyVariables", options.modifyVariables);
        logger.append("options.httpEnabled", options.httpEnabled);
        logger.append("options.ftpEnabled", options.ftpEnabled);
        logger.append("options.classpathEnabled", options.classpathEnabled);
        logger.append("options.classpathLoadedDependenciesTypes", options.classpathLoadedDependenciesTypes);
        logger.append("encoding", encoding);
        logger.append("outputFileFormat", outputFileFormat);
        logger.append("watch", watch);
        logger.append("watchInterval", watchInterval, new ValueToStringConverter() {

            private static final long MILLISECONDS_IN_SECOND = 1000L;

            @Override
            public String convert(final Object value) {
                final Integer number = (Integer) value;
                final StringBuilder text = new StringBuilder();
                text.append(number);
                if (number > 0) {
                    text.append(" (");
                    text.append(new TimeSpan(number * MILLISECONDS_IN_SECOND));
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

    private void calculateParameters() {
        if (getLog().isDebugEnabled()) {
            verbose = true;
        }
        if (includes.length == 0) {
            includes = getDefaultIncludes();
        }
        if (watchInterval < 1) {
            watchInterval = 1;
        }
    }

    @Override
    public void execute() throws MojoFailureException {
        logParameters();
        if (skip) {
            getLog().info("Skips job execution");
            return;
        }
        calculateParameters();

        if (options.classpathEnabled) {
            addDependenciesToClasspath();
        }

        if (watch) {
            runWatchMode();
        } else {
            runCompilation();
        }
    }

    private void addDependenciesToClasspath() {
        if (verbose) {
            getLog().info("Adding project dependencies to classpath...");
        }
        final ContextClassLoaderExtender extender = new ContextClassLoaderExtender(project, getLog());
        final String[] types = options.classpathLoadedDependenciesTypes.split(",");
        for (int i = 0; i < types.length; ++i) {
            types[i] = types[i].trim();
        }
        extender.addDependencies(types);
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

    private void compileFiles(final Collection<File> files) throws MojoFailureException {
        final String sourceFilesText = "source" + (files.size() != 1 ? "s" : "");
        getLog().info(String.format("Compiling %s %s to %s", files.size(), sourceFilesText, outputDirectory.getAbsolutePath()));

        final LessCompiler compiler = new LessCompiler();
        final Timer timer = SystemTimer.getStartedTimer();
        final LessOptions lessOptions = createOptions();
        for (final File file : files) {
            compileFile(compiler, file, lessOptions);
        }

        getLog().info(String.format("Finished %s compilation in %s", sourceFilesText, timer.stop()));
    }

    private LessOptions createOptions() throws MojoFailureException {
        try {
            return options.toLessOptions(encoding);
        } catch (final Exception e) {
            throw new MojoFailureException("Cannot prepare compiler configuration", e);
        }
    }

    private void compileFile(final LessCompiler compiler, final File source, final LessOptions options) throws MojoFailureException {
        Timer timer = null;
        if (verbose) {
            getLog().info("Processing Less source: " + source.getAbsolutePath());
            timer = SystemTimer.getStartedTimer();
        }
        final File destination = new DestinationFileCreator(sourceDirectory, outputDirectory, outputFileFormat).create(source);
        compiler.compile(source, destination, options);
        if (timer != null) {
            getLog().info("Finished in " + timer.stop());
        }
    }

    // private void saveCompiledCode(final File source, final String compiled, final Date compilationDate) throws
    // MojoFailureException {
    // final File destination = new DestinationFileCreator(sourceDirectory, outputDirectory,
    // outputFileFormat).create(source);
    //
    // final boolean skipsFileSaving = !force && !alwaysOverwrite && destination.exists()
    // && compilationDate.before(new Date(destination.lastModified()));
    // if (skipsFileSaving) {
    // if (verbose) {
    // getLog().info("Skips saving CSS compiled code to file, because cached version is older than destination file: "
    // + destination.getAbsolutePath());
    // }
    // return;
    // }
    //
    // if (verbose) {
    // getLog().info("Saving CSS code to " + destination.getAbsolutePath());
    // }
    // try {
    // FileUtils.write(destination, compiled, encoding);
    // } catch (final IOException e) {
    // throw new MojoFailureException(String.format("Cannot save CSS compiled code to file: %s",
    // destination.getAbsolutePath()), e);
    // }
    // }
}
