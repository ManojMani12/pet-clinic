@echo off
setlocal enabledelayedexpansion

set PORT=%1
if "%PORT%"=="" set PORT=8080

set ROOT_DIR=%~dp0..
set RUN_DIR=%ROOT_DIR%\build\run
set ARTIFACTS_DIR=%ROOT_DIR%\build\artifacts
set PID_FILE=%RUN_DIR%\app.pid
set LOG_FILE=%RUN_DIR%\app.log

if not exist "%RUN_DIR%" mkdir "%RUN_DIR%"

REM Stop previous instance if PID file exists
if exist "%PID_FILE%" (
  set /p OLD_PID=<"%PID_FILE%"
  if not "%OLD_PID%"=="" (
    echo [deploy] Stopping previous app PID %OLD_PID%...
    taskkill /PID %OLD_PID% /T /F >nul 2>&1
  )
  del /q "%PID_FILE%" >nul 2>&1
)

REM Pick latest jar in build\artifacts (by modified time)
set JAR_PATH=
for /f "delims=" %%f in ('dir /b /a:-d /o:-d "%ARTIFACTS_DIR%\*.jar" 2^>nul') do (
  set JAR_PATH=%ARTIFACTS_DIR%\%%f
  goto :jar_found
)

:jar_found
if "%JAR_PATH%"=="" (
  echo [deploy] ERROR: No jar found in %ARTIFACTS_DIR%. Run build\build.bat first.
  exit /b 1
)

echo [deploy] Starting %JAR_PATH% on port %PORT%...
type nul > "%LOG_FILE%"

REM Start detached
start "petclinic" /b cmd /c "java -jar ""%JAR_PATH%"" --server.port=%PORT% >> ""%LOG_FILE%"" 2>&1"

echo [deploy] Logs: %LOG_FILE%

set BASE_URL=http://localhost:%PORT%
set HEALTH1=%BASE_URL%/actuator/health
set HEALTH2=%BASE_URL%/

echo [deploy] Waiting for app to become healthy (timeout 90s)...
set OK=0

for /l %%t in (1,1,90) do (
  powershell -NoProfile -Command "try { $r = Invoke-WebRequest -UseBasicParsing -TimeoutSec 2 '%HEALTH1%'; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }"
  if !errorlevel! equ 0 (
    set OK=1
    goto :healthy
  )

  powershell -NoProfile -Command "try { $r = Invoke-WebRequest -UseBasicParsing -TimeoutSec 2 '%HEALTH2%'; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }"
  if !errorlevel! equ 0 (
    set OK=1
    goto :healthy
  )

  timeout /t 1 >nul
)

:healthy
if "%OK%"=="0" (
  echo [deploy] ERROR: App did not become healthy within 90s. Last 50 log lines:
  powershell -NoProfile -Command "Get-Content -Tail 50 '%LOG_FILE%'"
  exit /b 1
)

echo [deploy] Healthy at %BASE_URL%
exit /b 0
