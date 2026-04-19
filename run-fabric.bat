@echo off
REM Launches the Fabric dev client with Java 17 (official requirement for MC 1.20.1).
REM Edit JAVA_17_HOME below if your JDK path differs.

set "JAVA_17_HOME=C:/Program Files/Eclipse Adoptium/jdk-17.0.18.8-hotspot"

if not exist "%JAVA_17_HOME%\bin\java.exe" (
    echo [ERROR] JDK 17 not found at %JAVA_17_HOME%
    echo Install Eclipse Temurin 17 or edit run-fabric.bat.
    exit /b 1
)

set "JAVA_HOME=%JAVA_17_HOME%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

call gradlew.bat :fabric:runClient %*
