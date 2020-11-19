# WurstScript

Wurstscript is a delicious programming language which can compile to Jass code that is used to power WarCraft III.

[![Build Status](http://peeeq.de/hudson/job/Wurst/badge/icon)](http://peeeq.de/hudson/job/Wurst/)
[![Travis](https://travis-ci.org/wurstscript/WurstScript.svg?branch=master)](https://travis-ci.org/wurstscript/WurstScript)
[![GitHub issues](https://img.shields.io/github/issues/wurstscript/WurstScript.svg)]()
[![GitHub pull requests](https://img.shields.io/github/issues-pr/wurstscript/WurstScript.svg)]()
[![Coverage Status](https://coveralls.io/repos/github/wurstscript/WurstScript/badge.svg?branch=master)](https://coveralls.io/github/wurstscript/WurstScript?branch=master)


## User Documentation

Using WurstScript for your map is easy! Check out the [Setup Guide](https://wurstscript.github.io/start.html) on how to get started.
For a formal description of all features, visit the [Manual](https://wurstscript.github.io/manual.html).


##  Reporting Bugs

Please report any bugs your encounter with our [Issue Tracker](https://github.com/wurstscript/WurstScript/issues).
Include as much information as possible, ideally with logs.
Logfiles are located in your home folder under `~/.wurst/logs`.
Find the last modified file and pastebin it's contents.

## Contributing

See https://github.com/wurstscript/WurstScript/blob/master/CONTRIBUTING.md

## System Overview

This repository contains the following sub-projects:

- de.peeeq.wurstscript
	- The core wurstscript compiler and directly related tools
- Wurstpack
	- (deprecated) Wurst integration for the Warcraft III World Editor
- WurstWeb
	- Attempt to provide Wurst capabilities in browsers

IDE support is provided via a VSCode plugin: https://github.com/wurstscript/wurst4vscode

The source for the wurstscript website can be found here: https://github.com/wurstscript/wurstscript.github.io

## Build Process

### Using Gradle

Simply run the appropriate gradle task using the provided gradle wrapper.

```bash
./gradlew compileJava
```

For deploying .jars and .zips see tasks in **deploy.gradle**

```bash
./gradlew create_zip_wurstpack_compiler
```

To update your compiler installation use

```bash
./gradlew make_for_userdir
```

### Import into IDE

You can import the compiler project into any IDE that provides a gradle plugin.
For IntelliJ IDEA, you can simply execute `./gradlew openIdea`.
To run the Test Suite, execute `AllTests.xml` with TestNG.

### Publishing a new release

[Jenkins](http://peeeq.de/hudson/job/Wurst/) auto-releases versions as `major.minor.patch.hotfix-jenkins-Wurst-buildNumber` - e.g. `1.8.1.0-jenkins-Wurst-1248`.

The version string can be updated in [build.gradle](https://github.com/wurstscript/WurstScript/blob/master/de.peeeq.wurstscript/build.gradle#L28).
