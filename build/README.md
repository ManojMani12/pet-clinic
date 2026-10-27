# Local Build & Deploy (Spring PetClinic)

## Prerequisites
- JDK 17 installed and on PATH (`java -version` shows 17.x)
- No external DB required (defaults to H2 in-memory)

## Build (tests enabled)
### Linux/macOS
```sh
./build/build.sh
```

### Windows
```bat
build\build.bat
```

Artifacts are copied to:
- `build/artifacts/`

The build script prints the artifact path and SHA-256 checksum.

## Deploy locally
### Linux/macOS
```sh
./build/deploy.sh        # defaults to port 8080
./build/deploy.sh 8081   # custom port
```

### Windows
```bat
build\deploy.bat
build\deploy.bat 8081
```

Deploy logs:
- `build/run/app.log`

PID file (Linux/macOS):
- `build/run/app.pid`

## Verify
- Preferred health endpoint (if enabled): `http://localhost:<port>/actuator/health`
- Otherwise: `http://localhost:<port>/`

## Stop the app
### Linux/macOS
If started via `deploy.sh`, it writes a PID file:
```sh
kill "$(cat build/run/app.pid)"
rm -f build/run/app.pid
```

### Windows
If you need to stop it, find the Java process and kill it:
```bat
tasklist | findstr java
taskkill /PID <PID> /T /F
```
