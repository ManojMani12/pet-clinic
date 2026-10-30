@echo off
setlocal enabledelayedexpansion

set ROOT_DIR=%~dp0..
set ARTIFACTS_DIR=%ROOT_DIR%\build\artifacts

if not exist "%ARTIFACTS_DIR%" mkdir "%ARTIFACTS_DIR%"

cd /d "%ROOT_DIR%"

echo [build] Running Maven build (tests enabled)...
call mvnw.cmd clean package
if errorlevel 1 (
  echo [build] ERROR: Maven build failed
  exit /b 1
)

set JAR_PATH=
for %%f in ("%ROOT_DIR%\target\*.jar") do (
  set JAR_PATH=%%f
  goto :found
)

:found
if "%JAR_PATH%"=="" (
  echo [build] ERROR: Could not find built jar under target\*.jar
  exit /b 1
)

copy /y "%JAR_PATH%" "%ARTIFACTS_DIR%\%~nxf" >nul
if errorlevel 1 (
  echo [build] ERROR: Failed copying artifact
  exit /b 1
)

echo [build] Artifact: %ARTIFACTS_DIR%\%~nxf

REM SHA-256 (requires certutil)
for /f "tokens=1" %%h in ('certutil -hashfile "%ARTIFACTS_DIR%\%~nxf" SHA256 ^| findstr /r /c:"^[0-9A-Fa-f]"') do (
  set SHA=%%h
  goto :sha_done
)

:sha_done
if "%SHA%"=="" (
  echo [build] ERROR: Failed to compute SHA-256 (certutil missing?)
  exit /b 1
)

echo [build] SHA-256: %SHA%
exit /b 0
