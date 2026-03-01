@echo off
setlocal EnableExtensions DisableDelayedExpansion

rem Save current code page (extract number after ':')
for /f "tokens=2 delims=:" %%A in ('chcp') do for /f "tokens=1" %%B in ("%%A") do set "_OLDCP=%%B"

rem Switch to UTF-8 for this session
chcp 65001 >NUL

rem Resolve script dir
set "DIR=%~dp0"

set "JAVA=%DIR%wurst-runtime\bin\java.exe"
set "GRILL_JAR=%DIR%grill\grill.jar"

if not exist "%GRILL_JAR%" (
    echo [grill] ERROR: Missing jar. Searched:
    echo   %GRILL_JAR%
    rem fallback to ../grill if you want:
    set "GRILL_JAR=%DIR%..\grill\grill.jar"
    if not exist "%GRILL_JAR%" (
        echo   %GRILL_JAR%
        goto :restore
    )
)

if not exist "%JAVA%" (
    echo [grill] ERROR: Bundled runtime not found or not executable at:
    echo   %JAVA%
    echo Please reinstall wurstscript via the VS Code extension.
    goto :restore
)

"%JAVA%" -Dfile.encoding=UTF-8 -jar "%GRILL_JAR%" %*

:restore
rem Restore previous code page if we captured it
if defined _OLDCP chcp %_OLDCP% >NUL

endlocal
exit /b %ERRORLEVEL%
