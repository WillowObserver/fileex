@echo off
title 预览文件转换
echo 正在加密，请稍后....
echo path:%~dp0

set base=%~dp0

set class=%base%\cls
set libs=%base%\libs

set class_path=%class%;%libs%\jacob.jar;%libs%\mongo-java-driver-3.2.1.jar;%libs%\slf4j-api-1.7.25.jar;%libs%\logback-classic-1.2.3.jar;%libs%\logback-core-1.2.3.jar;%libs%\JPedal.jar;%libs%\sun-jai_codec.jar;%libs%\nutz-1.r.65.jar;%libs%\jsch-0.1.51.jar;%libs%\commons-net-2.2.jar;

java -classpath %class_path% com.sam.task.ToZipTask
@pause