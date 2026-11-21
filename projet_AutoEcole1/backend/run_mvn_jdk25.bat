@echo off
set "JAVA_HOME=C:\Users\Chaimaa\.jdks\openjdk-25.0.2"
set "PATH=%JAVA_HOME%\bin;%PATH%"
"C:\Users\Chaimaa\OneDrive\Desktop\projet_AutoEcole\tools\apache-maven-3.9.6\bin\mvn.cmd" -DskipTests=true clean test-compile
