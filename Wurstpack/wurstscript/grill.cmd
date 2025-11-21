@echo off
setlocal EnableExtensions

rem ---- shared slim runtime ----
set "RUNTIME=%USERPROFILE%\.wurst\wurst-runtime\bin\java.exe"

rem script directory (trailing backslash)
set "DIR=%~dp0"

if not exist "%RUNTIME%" (
  echo [wurstscript] ERROR: Runtime not found:
  echo(%RUNTIME%
  echo Reinstall Wurstscript via the VSCode extension.
  exit /b 1
)

rem ---- fixed jar location(s), no wildcards ----
set "JAR=%DIR%grill\grill.jar"
if not exist "%JAR%" set "JAR=%DIR%..\grill\grill.jar"
if not exist "%JAR%" (
  echo [grill] ERROR: Missing jar:
  echo(%DIR%grill\grill.jar
  echo or
  echo(%DIR%..\grill\grill.jar
  exit /b 1
)

rem Optional JVM flags via env var, e.g. set WURST_JAVA_OPTS=-Xmx1g
"%RUNTIME%" %WURST_JAVA_OPTS% -jar "%JAR%" %*
endlocal
