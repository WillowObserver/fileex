@echo off

echo path:%~dp0
set base=%~dp0

set _task=java.exe
set _svr=%base%\start.bat
set _des=start-bak.bat

:checkstart
for /f "tokens=5" %%n in ('qprocess.exe ^| find "%_task%" ') do (
 if %%n==%_task% (goto checkag) else goto startsvr
)

 

:startsvr
echo %time% 
echo ********����ʼ����********
echo �������������� %time% ,����ϵͳ��־ >> restart_service.txt
echo start %_svr% > %_des%
echo exit >> %_des%
start %_des%
set/p=.<nul
for /L %%i in (1 1 10) do set /p a=.<nul&ping.exe /n 2 127.0.0.1>nul
echo .
echo Wscript.Sleep WScript.Arguments(0) >%tmp%\delay.vbs 
cscript //b //nologo %tmp%\delay.vbs 10000 
del %_des% /Q
echo ********�����������********
goto checkstart


:checkag
echo %time% ������������,10���������.. 
echo Wscript.Sleep WScript.Arguments(0) >%tmp%\delay.vbs 
cscript //b //nologo %tmp%\delay.vbs 10000 
goto checkstart