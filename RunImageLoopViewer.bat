@echo off
setlocal
cd /d %~dp0
.\jdk\bin\java.exe -jar ImageLoopViewer.jar
endlocal
pause
