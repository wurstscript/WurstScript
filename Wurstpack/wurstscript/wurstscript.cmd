@echo off
setlocal

rem Save current code page
for /f "tokens=2 delims=: " %%A in ('chcp') do set "_OLDCP=%%A"

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
