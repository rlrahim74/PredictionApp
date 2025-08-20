@echo off
set DIR=%~dp0
set JAVA_HOME=%JAVA_HOME%
if "%JAVA_HOME%"=="" set JAVA_HOME=C:\Program Files\Java\jdk-17
"%JAVA_HOME%\bin\java.exe" -Xmx64m -cp "%DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
