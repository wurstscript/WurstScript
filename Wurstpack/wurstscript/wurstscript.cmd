@echo off
setlocal EnableExtensions DisableDelayedExpansion

rem Save current code page (extract number after ':')
for /f "tokens=2 delims=:" %%A in ('chcp') do for /f "tokens=1" %%B in ("%%A") do set "_OLDCP=%%B"

rem Switch to UTF-8
chcp 65001 >NUL

set "DIR=%~dp0"
set "JAVA=%DIR%wurst-runtime\bin\java.exe"
set "JAR=%DIR%wurst-compiler\wurstscript.jar"

if not exist "%JAR%" (
    set "JAR=%DIR%..\wurst-compiler\wurstscript.jar"
)

if not exist "%JAR%" (
    echo [wurstscript] ERROR: Missing jar. Searched:
    echo   %DIR%wurst-compiler\wurstscript.jar
    echo   %DIR%..\wurst-compiler\wurstscript.jar
    goto :restore
)

if not exist "%JAVA%" (
    echo [wurstscript] ERROR: Bundled runtime not found or not executable at:
    echo   %JAVA%
    echo Please reinstall wurstscript via the VS Code extension.
    goto :restore
)

"%JAVA%" -Dfile.encoding=UTF-8 -jar "%JAR%" %*

:restore
if defined _OLDCP chcp %_OLDCP% >NUL
endlocal
exit /b %ERRORLEVEL%
