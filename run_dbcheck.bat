@echo off
cd /d %~dp0
java -cp "target\classes;target\dependency\*" smoma.tools.DbChecker
pause
