---
layout: simple
title: Wurst-Script
---
_by peq & Frotty_ 


WurstScript is programming language which compiles to Jass (the language to create Warcraft III Maps).

It is an imperative, object-oriented, statically-typed, beginner-friendly programming language with significant whitespace and a readable syntax.



[Read the manual](./manual.html) to learn more about WurstScript.

    
## Download
	
You can find more detailed installation notes here: [Installation Notes](./installation.html).

### Eclipse Plugin

Update site: `http://peeeq.de/hudson/job/Wurst/lastSuccessfulBuild/artifact/downloads/eclipseUpdateSite/`

In Eclipse go to "Help - Install New Software" and add the update site mentioned above. You should then be able to install the Wurst plugin from that pagei.


### Wurst Pack

The Wurst Pack contains a version of the editor with the Wurst compiler built in.
		
* [Wurst Updater](http://peeeq.de/hudson/job/Wurst/lastSuccessfulBuild/artifact/downloads/WurstUpdater.jar)
			This Java program will automatically download Wurstpack for you and you can always run it again to update to the latest version.

If you prefer manual downloads over the automatic updater, you can use the links below:


* [Complete Pack](http://peeeq.de/hudson/job/Wurst/lastSuccessfulBuild/artifact/downloads/wurstpack_complete.zip) 
			This is the complete pack. You usually should download this, but you can use the other downloads if you only want to update
			parts of the Wurst Pack.
* [Compiler](http://peeeq.de/hudson/job/Wurst/lastSuccessfulBuild/artifact/downloads/wurstpack_compiler.zip): 
			This download only contains the wurstscript folder of the Pack. Usually it is sufficient to update this folder, since 
			other parts of the Pack are rarely changed.
* [Standard Library](http://peeeq.de/hudson/job/Wurst/lastSuccessfulBuild/artifact/downloads/wurstpack_lib.zip): 
			This is standard library.
* [Wurstscript.jar](http://peeeq.de/hudson/job/Wurst/lastSuccessfulBuild/artifact/downloads/wurstscript.jar): 
			This is only the main jar file for the compiler. For small bugfixes it is often sufficient to replace this file, but
			to be sure you should usually choose the bigger downloads.

All downloads use the latest successful build from our build-server. If you have problems with the latest version you might want to check out older versions
by clicking on the "Previous Build" arrow on the [download page](http://peeeq.de/hudson/job/Wurst/lastSuccessfulBuild/artifact/downloads/).

## Intro video

<div>
<iframe width="640" height="360" src="//www.youtube.com/embed/JYhUkRsQe-o" frameborder="0" allowfullscreen="true"></iframe>
</div>


## Source Code

You can download the sources for this project in either
[zip](https://github.com/peq/WurstScript/zipball/master) or
[tar](https://github.com/peq/WurstScript/tarball/master) formats.

You can also clone the project with [Git](http://git-scm.com) by running:
	
	$ git clone git://github.com/peq/WurstScript
