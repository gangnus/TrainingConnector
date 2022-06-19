echo on
call C:\IdM\midpoint-4.4\bin\stop.bat
call mvn -e clean package
echo error= %ERRORLEVEL%

if not errorlevel 1 (
    copy .\target\connector*.* C:\IdM\midpoint-4.4\var\icf-connectors\
    call C:\IdM\midpoint-4.4\bin\start.bat
)
