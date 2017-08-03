---
title: Introduction Part One
sections:
- Welcome
- Project Structure
- Hello Wurst
---

{: .question}
### *&nbsp;*{: .fa .fa-question-circle} Wurst not installed yet? Follow the __[Setup Guide](../start.html)__

{: .answer}
### *&nbsp;*{: .fa .fa-exclamation-circle} This guide expects the reader to have **basic** knowledge of functions and variables.
------

## Welcome

Hello and welcome to WurstScript. This guide is an informal introduction to the Wurst Workflow.
We will cover how to structure code, use the standard library, create data objects through code and run the final product in warcraft 3.
This guide will **not** explain the core principles of programming, i.e. how functions and variables work and how we use them.

## Project Structure

If you setup your project correctly using the setup app, there will be many folders and files generated in the project folder you chose. Otheriwse setup your project now. Opened in VSCode it should look something like this:

![](/WurstScript/assets/images/beginner/ProjectExplorer.png){: .img-rounded .img-responsive}

Let's go through all the files:
- **/_build** Contains dynamically generated content and shouldn't be touched by the user.
- **/.vscode** Contains a **settings.json** file that links vscode to the wurst tools. You can add additional project-wide configuration here.
- **/imports** All files inside this folder will be imported into your map upon saving.
- **/wurst** All files in this folder that end with **.wurst, .jurst, .j** will be parsed as code.
- **.gitignore** Useful template if you want to make your wurst project a git repo as well
- **MyMap.w3x** An example tft map containing a Bloodmage
- **wurst_run.args** Defines a set of flags to use when running a map from VSCode
- **wurst.build** Contains project information
- **wurst.dependencies** Generated file that links libraries. Shouldn't be touched.
- **wurst/Hello.wurst** Demo package

## Hello Wurst

Open the **Hello.wurst** file inside the **wurst** folder to start the wurst plugin.
Run the project using the `>runmap` command.

![](/WurstScript/assets/images/beginner/RunMap.png){: .img-responsive .img-rounded}

The text **Hello World** will be displayed.

![](/WurstScript/assets/images/beginner/HelloWorld.png){: .img-responsive .img-rounded}

Let's take a look at the code:

{: .line-numbers}
```wurst
package Hello

// Wurst demo package
init
	print("Hello World")
```
The first line 
```wurst 
package Wurst

```
is the package declaration. Each file contains exactly one package that is named like the file without the ending. Apart from comments this must be the first line
in any .wurst file.

```wurst 
// Wurst demo package
init
	print("Hello World")
```
The third line is a comment, and the fourth line starts an **init**-block by just containing the **init** keyword without indentation.
Everything indented after this keyword is inside it's block of contents, such as the **print** statement in line 5, which displays the given text on the screen for all players.
All blocks automatically end at the end of the file, such as the **init** and **package** blocks in this example.



