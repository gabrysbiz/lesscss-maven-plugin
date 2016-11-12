# About
[![License BSD 3-Clause](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg)](http://lesscss-maven-plugin.projects.gabrys.biz/license.txt)
[![Build Status](https://travis-ci.org/gabrysbiz/lesscss-maven-plugin.svg?branch=release%2F1.2.1)](https://travis-ci.org/gabrysbiz/lesscss-maven-plugin)

This plugin compiles [Less](http://lesscss.org/) files to [CSS](http://www.w3.org/Style/CSS/) stylesheets using [extended version](http://lesscss-extended-compiler.projects.gabrys.biz/) of the [LessCSS Compiler](http://lesscss-compiler.projects.gabrys.biz/).

The plugin was developed as an alternative version of the [org.lesscss:lesscss-maven-plugin](https://github.com/marceloverdijk/lesscss-maven-plugin) (by [Marcel Overdijk](https://github.com/marceloverdijk)) which is compatible with [Less 1.7.5](https://github.com/less/less.js/releases/tag/v1.7.5). It resolves all problems with [imports options](http://lesscss.org/features/#import-options) and adds extra features:
* two [compiler types](http://lesscss-maven-plugin.projects.gabrys.biz/1.2.1/compilers-comparison.html) for lower disk usage
* [inserts sources paths](http://lesscss-maven-plugin.projects.gabrys.biz/1.2.1/compile-mojo.html#addCommentsWithPaths) to [CSS](http://www.w3.org/Style/CSS/) stylesheets (assists work on large files)

Read a [migration](http://lesscss-maven-plugin.projects.gabrys.biz/1.2.1/migration.html) page for information how to switch from the [org.lesscss:lesscss-maven-plugin](https://github.com/marceloverdijk/lesscss-maven-plugin) to this plugin.

# Goals Overview
* [lesscss:compile](http://lesscss-maven-plugin.projects.gabrys.biz/1.2.1/compile-mojo.html) - compiles [Less](http://lesscss.org/) files to [CSS](http://www.w3.org/Style/CSS/) stylesheets

# Requirements
The plugin to run requires:
* Java 5.0 or higher
* Maven 2.0.11 or higher

# Usage
General instructions on how to use the LessCSS Maven Plugin can be found on the [usage](http://lesscss-maven-plugin.projects.gabrys.biz/1.2.1/usage.html) page. Some more specific use cases are described in the examples given below. Last but not least, users occasionally contribute additional examples, tips or errata to the plugin's [wiki](https://github.com/gabrysbiz/lesscss-maven-plugin/wiki) page.

In case you still have questions regarding the plugin's usage, please have a look at the [FAQ](http://lesscss-maven-plugin.projects.gabrys.biz/1.2.1/faq.html).

If you feel like the plugin is missing a feature or has a defect, you can fill a feature request or bug report in the [issue tracker](http://lesscss-maven-plugin.projects.gabrys.biz/1.2.1/issue-tracking.html). When creating a new issue, please provide a comprehensive description of your concern. Especially for fixing bugs it is crucial that the developers can reproduce your problem. For this reason, entire debug logs, POMs or most preferably little demo projects attached to the issue are very much appreciated. Of course, patches are welcome, too. Contributors can check out the project from the [source repository](http://lesscss-maven-plugin.projects.gabrys.biz/1.2.1/source-repository.html) and will find supplementary information in the [guide to helping with Maven](http://maven.apache.org/guides/development/guide-helping.html).

# Examples
To provide you with better understanding of some usages of the LessCSS Maven Plugin, you can take a look into the following examples:
* [Using include/exclude patterns](http://lesscss-maven-plugin.projects.gabrys.biz/1.2.1/examples/patterns.html)
* [Multiple source/output directories](http://lesscss-maven-plugin.projects.gabrys.biz/1.2.1/examples/multiple-directories.html)
* [Sources located only on local hard drive](http://lesscss-maven-plugin.projects.gabrys.biz/1.2.1/examples/local-sources.html)

You can also fetch example projects from [GitHub](https://github.com/gabrysbiz/lesscss-maven-plugin-examples).