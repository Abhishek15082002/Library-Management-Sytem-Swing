@echo off
echo Compiling Library Management System...
echo =======================================
set JDBC_JAR_PATH=src\lib\mysql-connector-j-9.3.0.jar
set MAIN_CLASS=library.LoginPage

if not exist "%JDBC_JAR_PATH%" (
    echo ERROR: JDBC Driver JAR not found at %JDBC_JAR_PATH%
    echo Please check the JDBC_JAR_PATH variable in this script.
    pause
    exit /b 1
)

if not exist "bin" (
    mkdir "bin"
)
javac -d "bin" -cp "%JDBC_JAR_PATH%" src\library\*.java src\library\frontend\*.java src\library\backend\*.java

if %errorlevel% neq 0 (
    echo =======================================
    echo ERROR: Compilation failed. See messages above.
    pause
    exit /b %errorlevel%
)

echo =======================================
echo Compilation successful.
echo Running Library Management System...
echo =======================================

java -cp "bin;%JDBC_JAR_PATH%" %MAIN_CLASS%

if %errorlevel% neq 0 (
    echo =======================================
    echo INFO: Application exited with code %errorlevel%.
) else (
    echo =======================================
    echo Application finished.
)

pause
