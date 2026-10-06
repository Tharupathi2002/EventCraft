@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------

@if "%DEBUG%" == "" @echo off
@classpathsnoop %*

set ERROR_CODE=0

@setlocal

set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
if "%MAVEN_PROJECTBASEDIR%"=="" set MAVEN_PROJECTBASEDIR=%~dp0

set MAVEN_CONFIG=%MAVEN_PROJECTBASEDIR%\.mvn

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

if exist %WRAPPER_JAR% goto run

echo Downloading Maven Wrapper...
java -classpath %WRAPPER_JAR% %WRAPPER_LAUNCHER% %*
if errorlevel 1 goto error

:run
java -classpath %WRAPPER_JAR% %WRAPPER_LAUNCHER% %*
if errorlevel 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
cmd /C exit /B %ERROR_CODE%
