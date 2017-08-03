---
title: Legacy Code
sections:
- Compiling legacy code
- Using legacy code within wurst
- Jurst dialect
---

## Compiling with legacy code

### Jass

If you use GUI or vanilla Jass in your map using the trigger editor, the integration should be seamless.
Saving your map using the WorldEditor will output your GUI or Jass code into the map itself. 
The compiler then extracts that script when performing the **runmap** command and merges it with the generated Wurst code.

### vJass and others

vJass and other precompilers are usually run with an editor extension called JNGP or WEX.
If you use such a tool that automatically injects the modified script into the map, the integration stays the same as with regular Jass.
Wurst will extract the existing code and just add it's own to it.

However this prohibits saving in the normal WorldEditor.

## Using legacy code within wurst

In rare cases it might be desirable to use legacy code from within wurst code without errors while also receiving benefits like autocomplete
and jumping to declarations.
To do this you must extract the war3map.j file from your legacy map and place it into the **wurst/** folder inside your project.

![](/WurstDocs/assets/images/legacy/war3map.j.png){: .img-responsive .img-rounded}

This file will however not be constantly parsed, so you need to clean your workspace for a recheck.
If you want to make changes to the war3map.j and have them applied to the map, you have to add the RunArg **-noExtractMapScript**
to your wurst_run.args file, which will prevent the script extraction from the map and use the provided war3map.j inside the wurst folder instead.

![](/WurstDocs/assets/images/legacy/uselegacycode.png){: .img-responsive .img-rounded}

## Jurst dialect

Jurst is a dialect of Wurst, which has the same features as Wurst, but with a Syntax similar to vJass. You can use Jurst to adapt vJass code, but there are still a few manual steps involved, because of the difference in supported features.
Jurst also allows for parsing of regular Jass code, making it a less strict mix of Jass and Wurst.
Take a look at these following examples, which all compile:

```wurst
library LooksLikeVjass initializer ini
    private function ini takes nothing returns nothing
        local trigger t = CreateTrigger()
        t = null
    endfunction
endlibrary
```
and

```wurst
package NearlyTheSameAsWurst
    function act()
        print(GetTriggerUnit().getName() + " died.")
    end

    init
        CreateTrigger()..registerAnyUnitEvent(EVENT_PLAYER_UNIT_DEATH)
                       ..addAction(function act)
    end

// Note: "end" intentionally not present here.
```

As you can see, Jurst is able to access wurst code including packages from the wurst standard library, such as print. However, you should not try to access Jurst (or jass/vjass) code from Wurst packages.

