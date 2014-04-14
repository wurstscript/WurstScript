---
layout: simple
title: WurstScript Installation & Setup
---
This tells you how to use the WurstEditor and Eclipse plugin correctly in order to develop your map.
Since the JNGP only works on Windows, Wurstpack usage is limited to windows, too.


##Requirements:

- [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
	Many systems already have Java installed. In that case you should make sure that you have the latest version installed.
- [Wurst Pack](http://peq.github.io/WurstScript/#Downloads)
	Some AnitVirus programs falsely detect harmless injection-dlls. Ignore the warning and/or turn off your AV-Program for the process and add an exception. The Updater might not respond while downloading the required packages.
- [Eclipse](http://www.eclipse.org/downloads/packages/eclipse-standard-432/keplersr2)  
	Download links for your platform are in the right sidebar. Eclipse is needed for working with the Wurst-Plugin.

#Installing the Wurst-Plugin and setting up Wurst

##Installing the Wurst-Plugin

1. Start Eclipse from the directory you extracted it to
2. Open the installation window with _Help_ -> _Install new Software_
3. Enter _http://peeeq.de/hudson/job/Wurst/lastSuccessfulBuild/artifact/downloads/eclipseUpdateSite/_ into the "Work with:" textfield and press Enter. 
4. A WurstScript Eclipse Plugin Entry should appear in the list. Select it and press next.
5. Accept the license agreement, press next and wait for the install to finish.
6. Restart Eclipse to complete the installation

##Setting up a Wurst project

1. Go to _File -> New -> Project_
2. Select *Wurst Project* as wizard and press next
3. Type in the name of your project and press finish. Now you should see your project with a file named "wurst.dependencies". The path of it is wrong and we need to correct it.
4. Go to the folder where you extracted Wurstpack, from there go to: Wurstpack -> wurstscript -> lib
5. Now copy the current file path. If you use windows 7 and don't know how to copy the file path take a look at [here](http://technet.microsoft.com/en-us/magazine/ff678296.aspx)
6. Paste the path into the wurst.dependencies file in eclipse. A correct path should look similar to this: C:\User\YourUserName\Wurstpack\wurstscript\lib
7. Save the file and restart Eclipse

###Create your first Wurst package

1. To create a new Package go: File -> New -> WurstPackage
2. Type in a name (remember naming conventions). Now you should be able to see the Console at the bottom of Eclipse. And easy way to check if everything is correct is to type `1+1` into the Console.

###Importing existing projects

If you want to import existing projects into Eclipse, for example the WurstScript Standard Library, do as follows:

1. Go to File -> Import
2. Select as import source: General -> Existing Projects into Workspace
3. Browse to the folder the contains the project, e.g. the Wurstpack directory and press OK
4. Check the entry of the project you want to import and press Finish.

#Using WurstWE together with the plugin

To achieve perfect workflow, all your coding should be done inside the Wurst-Eclipseplugin and the WurstWE should remain for other editor stuff and testing. In order to use your .wurst files from eclipse when saving the map, you have to create a folder named "wurst" next to your .w3x map. When saving, WurstScript will automatically use all .wurst files in the /wurst folder next to the map.

###Example
There a few ways to get this done, this is in my eyes the best way:

1. Rightclick on your folder, and select New -> Folder
2. Type in a folder name, preferable "wurst"
3. Press on advanced, and select "Link to alternate location"
4. Click on browse, and go to the place where your maps are stored, or you will store your wurst maps
5. Create a new folder there, with the name "wurst" ! (The name HAS to be wurst, else it won't work)
6. Select this folder and press OK. 
	Everything that is stored in your wurst folder, will get imported into your map.
	The maps must be stored at the same place like the wurst folder, else it won't work.

###Test

1. Open the WurstWE.exe which is stored in the wurstpack
2. Create a new map, at the same place where the wurst is stored
3. Open Eclipse and write your code
4. Drag anything that should be imported into the wurst folder
5. Save all the packages in eclipse
6. Save your map via the save symbol (just like in Jass New Gen)
7. Test your map

#Issues:
I always get into the main menu although I have saved the card properly:

- Make sure your map is w3x and not w3m

I wrote something in vJass, but the compiler don't accept it:

- There is no vJass support, but you can still use normal Jass code outside of packages

I still have an Issue, and I don't know how to solve it:

- Post your issue here: https://github.com/peq/WurstScript/issues
- Join our irc: http://webchat.quakenet.org/#
			Channel : #inwc.de-maps

