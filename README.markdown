Wurstscript is a scripting language which can compile to Jass code which is used in WarCraft III.

[![Build Status](http://peeeq.de/hudson/job/Wurst/badge/icon)](http://peeeq.de/hudson/job/Wurst/)
[![Travis](https://img.shields.io/travis/wurstscript/WurstScript.svg)]()
[![GitHub issues](https://img.shields.io/github/issues/wurstscript/WurstScript.svg)]()
[![GitHub pull requests](https://img.shields.io/github/issues-pr/wurstscript/WurstScript.svg)]()


User Documentation
==================

If you want to know how to use the Wurst language, check out the [Manual](https://wurstscript.github.io/WurstScript/manual.html).


Reporting Bugs
==============

Bugs should be reported using our [Issue tracker on github](https://github.com/wurstscript/WurstScript/issues).

Please include all steps needed to reproduce the bug.

Contributing
============

If you want to contribute, the best way is to contact us prior to starting your contribution.

Alternatively create a new issue or choose an existing one and indicate that you are working on it. 
When you are done you can create us a pull request, which we will gladly accept once it meets our expectations.


System Overview
===============

This project contains the following sub-projects:

- de.peeeq.wurstscript
	- The core wurstscript compiler and related tools
- Wurstpack
	- Wurst integrated into the Warcraft III World Editor (similar to and based on JassNewGenPack)
- WurstUpdater
	- Automatic updater for the wurstpack

IDE support is provided via a VSCode plugin: https://github.com/peq/wurst4vscode

The documentation is on a separate branch named gh-pages.
	

Build Process
================

## Building the compiler

### Using gradle

Simply run the appropriate gradle task using the provided gradle wrapper.

```gradle
./gradlew compileJava
```

For deploying .jars and .zips see tasks in **deploy.gradle**

```gradle
./gradlew create_zip_wurstpack_compiler
```

### Updating the version number

- Change the version in `de.peeeq.wurstscript/version.properties`.
- Run gradle task :versionInfoFile






	
