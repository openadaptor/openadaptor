@ECHO OFF
REM
REM sets classpath for basic examples, uses absolute paths, but relies on this
REM script being run from the bin dir
REM
FOR /F "TOKENS=*" %%A in ('chdir') DO SET ROOT=%%A\..\..\lib
SET CLASSPATH=%ROOT%;%ROOT%\patch.jar;%ROOT%\openadaptor.jar;%ROOT%\openadaptor-spring.jar;%ROOT%\openadaptor-depends.jar
