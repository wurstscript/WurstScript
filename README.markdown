Wurstscript is a scripting language which can compile to Jass code which is used in WarCraft III.

[![Build Status](http://peeeq.de/hudson/job/Wurst/badge/icon)](http://peeeq.de/hudson/job/Wurst/)

User Documentation
==================

If you are a user of Wurst you should check out our [Manual](http://peeeq.de/wurst/manual/).


Reporting Bugs
==============

Bugs should be reported using our [Issue tracker on github](https://github.com/peq/WurstScript/issues).

Please include all steps needed to reproduce the bug.

Contributing
============

If you want to contribute the best way is to contact Frotty or peq.

Alternatively you can choose an open issue from the issue tracker and start working on it. 
When you are done you can send us a pull request. If you are working on something which takes
more than 15 minutes to fix you should communicate before you start working on it (e.g. comment on the issue). 

If you want to add a new feature you should open a ticket in our issue tracker first, so we can discuss the feature.

System Overview
===============

This project contains the following sub-projects:

- de.peeeq.wurstscript
	- This wurstscript compiler, heart of the 
- EclipseWurstPlugin, EclipseWurstPluginFeatures and EclipseWurstPluginUpdateSite
	- The eclipse plugin providing an IDE for wurst development
- Wurstpack
	- Wurst integrated into the Warcraft III World Editor (similar to and based on JassNewGenPack)
- WurstUpdater
	- Automatic updater for the wurstpack
- parseq
	- AST generator tool (used to build the compiler)

The documentation is on a separate branch named gh-pages.
	
	
No longer used:

- MpqCL
	- Mpq editing command line tool
- WurstEditor
	- Old editor (before we decided to have an eclipse plugin as main editor)


Build Process
================

## Building the compiler

### Using eclipse

Building this project with eclipse is recommended.

- import the project de.peeeq.wurstscript into your workspace
- the project depends on some generated sources which are generated using the ant task "gen" from the build.xml
	- to execute this task open the ant view in eclipse (window -> show view -> ant)
	- drag and drop build.xml into the ant view
	- doubleclick "gen" from the list of tasks
- (optional) you can now run the test suite in /src-test/de/peeeq/wurstscript/tests/AllTests.java using jUnit
- Execute the "make_for_wurstpack" task from the build-jar.xml file. This will generate a new compiler jar and place it into the 
	Wurstpack folder.

### Using the command line

To build the project from the commandline you need ant installed on your system.

- run "ant -buildfile build-wurst.xml compile" to compile the project
- run "ant -buildfile build-jar.xml make_for_wurstpack"

The effects should be the same as when invoking ant from eclipse.

## Building the eclipse plugin

The eclipse plugin can be started right from your current eclipse installation:

- Rightclick project
- Run as ... eclipse application

### Building the update site

- Open the site.xml file in the EclipseWurstPluginUpdateSite project.
- Press "Build All"

### Updating the version number

- Change the version in `de.peeeq.wurstscript/version.properties`.
- Run the ant file `de.peeeq.wurstscript/build-generate-mvn.xml`, target `updateversions`. 
	This will update the version number in all the necessary files.


Architecture Overview
=====================

TODO




	
