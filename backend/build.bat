@echo off
set JAVA_HOME=C:\Users\刘子轩\.jdks\ms-21.0.12
cd /d "%~dp0"
call .\mvnw.cmd clean install -DskipTests -pl common,gateway,auth-service,user-service,document-service,consultation-service,contract-service,payment-service,case-service -am -q
pause
