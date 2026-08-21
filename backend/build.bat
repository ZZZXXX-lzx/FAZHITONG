@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17
cd /d "%~dp0"
call .\mvnw.cmd clean install -DskipTests -pl common,gateway,auth-service,user-service,document-service,consultation-service,contract-service,payment-service,case-service -am -q
pause
