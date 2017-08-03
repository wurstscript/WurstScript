---
title: Install Java
sections:
  - Download JRE
  - Verify Java Intallation
---

WurstScript and it's related tools require Java 8 to run. Please install or verify your installation.

### Download JRE

[*&nbsp;*{: .fa .fa-download} Download Java](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html){: .btn .btn-orange} ![](/WurstDocs/assets/images/setup/java_powered.png) 

Download the Java Runtime appropriate for your system and follow the installation procedure.

Once finished, continue to the next step

### Verify Java Intallation

Many tasks rely on a environment variable called **JAVA_HOME** which might not have been set by the installer.
To verify your installation, open a command prompt and enter `java -version`

A valid installation will return something like this:

![](/WurstDocs/assets/images/setup/JavaVerify.png){: .img-responsive}

Your minor version might differ, but that's fine as long as it's 1.8 or higher.

If you get an error or a lower version even though you installed a newer one, your **JAVA_HOME** is either missing or configured wrong.

Refer to [*&nbsp;*{: .fa .fa-external-link-square} These Tutorials](http://www.baeldung.com/java-home-on-windows-7-8-10-mac-os-x-linux) in order to setup your **JAVA_HOME** correctly.

Once you have setup Java correctly, continue by installing VSCode.