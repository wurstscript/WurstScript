# WurstScript Setup App

Allows automated installation of a wurstscript environment and project setup.

## How it works

### Wurst Installation

The wurst compiler gets downloaded into the users home directory into a wurst folder `~/.wurst`

### Project Generation

The setup apps downloads this repo https://github.com/Frotty/WurstBareboneTemplate as a wurst project template and then inserts the necessary local parths as well as generating the wurst.dependencies file.
Dependencies are stored in `_build/dependencies/`
