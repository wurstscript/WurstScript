# WurstScript

Wurstscript is a delicious programming language which compiles to Jass or Lua code that is used to power WarCraft III maps.

[![Build](https://github.com/wurstscript/WurstScript/actions/workflows/build.yml/badge.svg)](https://github.com/wurstscript/WurstScript/actions/workflows/build.yml)
[![Release](https://github.com/wurstscript/WurstScript/actions/workflows/release.yml/badge.svg)](https://github.com/wurstscript/WurstScript/actions/workflows/release.yml)
[![GitHub issues](https://img.shields.io/github/issues/wurstscript/WurstScript.svg)]()
[![GitHub pull requests](https://img.shields.io/github/issues-pr/wurstscript/WurstScript.svg)]()
[![Coverage Status](https://coveralls.io/repos/github/wurstscript/WurstScript/badge.svg?branch=master)](https://coveralls.io/github/wurstscript/WurstScript?branch=master)


## User Documentation

Using WurstScript to build a map is easy! Check out the [Setup Guide](https://wurstscript.github.io/start.html) on how to get started.
For users, installation is automated and intended to work out of the box via the official setup.
For a formal description of all language features, visit the [Manual](https://wurstscript.github.io/manual.html).

Consider joining the WurstScript community on [Discord](https://discord.gg/mSHZpWcadz).


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
 - HelperScripts
   	- Some external scripts used for generating data e.g. for StdLib

IDE support is provided via a VSCode plugin: https://github.com/wurstscript/wurst4vscode

The source for the wurstscript website can be found here: https://github.com/wurstscript/wurstscript.github.io

## Compiler Build Process

For **contributing/developing the compiler**, Java 25 is required.  
End users normally do not need to install/configure Java manually for the standard Wurst setup flow.

Clone the repository and open the `de.peeeq.wurstscript` folder which contains the compiler project.

### Using Gradle

Use the gradle wrapper to run required tasks, it will download gradle automatically. Replace `[task_name]` with the desired task.

```bash
.\gradlew [task_name]
```

#### Examples 

To update the local compiler installation used by vscode run

```bash
.\gradlew make_for_userdir
```

For deploying .jars and .zips see tasks in **deploy.gradle**

```bash
.\gradlew create_zip_wurstpack_compiler
```


### Import into IDE

You can import the compiler project into any IDE that provides a gradle plugin.
For IntelliJ IDEA, you can simply execute `./gradlew openIdea`.
To run the Test Suite, execute `AllTests.xml` with TestNG.

### Publishing a new release

Releases are published via GitHub workflows and GitHub Releases:
- Workflow: https://github.com/wurstscript/WurstScript/actions/workflows/release.yml
- Releases: https://github.com/wurstscript/WurstScript/releases

The version string can be updated in [build.gradle](https://github.com/wurstscript/WurstScript/blob/master/de.peeeq.wurstscript/build.gradle#L28).
