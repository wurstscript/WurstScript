---
layout: simple
title: WurstScript Installation & Setup
---
This tells you how to use the WurstEditor and Eclipse plugin correctly in order to develop your map.
Since the JNGP only works on Windows, Wurstpack usage is limited to windows, too.


## Requirements:

- [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
	Many systems already have Java installed. In that case you should make sure that you have the latest version installed.
- [Wurst Pack](http://peq.github.io/WurstScript/#Downloads)
	Some AnitVirus programs falsely detect harmless injection-dlls. Ignore the warning and/or turn off your AV-Program for the process and add an exception. The Updater might not respond while downloading the required packages.
- [VSCode](https://code.visualstudio.com/)
        VSCode serves as IDE platform for wurst.


# Installing the Wurst-Plugin and setting up Wurst

## Installing the Wurst-Plugin

1. Start VSCode
2. Press F1 and enter 'install extension' in the prompt.
3. Select the "Extensions: Install Extensions" task
4. In the Extensions View, search for "wurst"
5. Install the "Wurst language support" plugin
6. Restart VSCode and open a .wurst file to activate the plugin 

## Setting up a Wurst project

Your project should have the following hierachy:

````
- wurst/
|-------- Hello.wurst
- wurst.dependencies
- YourMap.w3x
````

1. Create the root, the wurst folder and the wurst.dependencies file as necessary
2. Open the root folder with VSCode via *File -> Open Folder*
3. Open in explorer the folder where you extracted Wurstpack, from there go to: Wurstpack -> wurstscript -> lib
4. Now copy the current file path. If you use windows 7 and don't know how to copy the file path take a look at [here](http://technet.microsoft.com/en-us/magazine/ff678296.aspx)
5. Paste the path into the wurst.dependencies file in eclipse. A correct path should look similar to this: C:\User\YourUserName\Wurstpack\wurstscript\lib
6. Save the file.

### Create your first Wurst package

1. To create a new Package go: File -> New File
2. Enter a filename that ends in **.wurst**
3. The file will now be treated as wurst package

### Importing existing projects

If you want to import existing projects into VSCode, just use *File -> Open Folder* and open the root.

# Issues:
I always get into the main menu although I have saved the card properly:

- Make sure your map is w3x and not w3m

I wrote something in vJass, but the compiler don't accept it:

- You can enable JassHelper preprocessing inside WurstPack to generate a valid war3map.j

I still have an Issue, and I don't know how to solve it:

- Post your issue here: https://github.com/peq/WurstScript/issues
- Join our irc: http://webchat.quakenet.org/#
			Channel : #inwc.de-maps

