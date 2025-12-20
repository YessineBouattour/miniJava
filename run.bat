@echo off
echo ======================================
echo   Compilation et Demarrage
echo ======================================
echo.

cd /d "%~dp0"

echo [1/3] Telechargement des dependances...
if not exist "lib" mkdir lib

echo   - MySQL Connector...
if not exist "lib\mysql-connector-j-8.2.0.jar" (
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.2.0/mysql-connector-j-8.2.0.jar' -OutFile 'lib\mysql-connector-j-8.2.0.jar'"
)

echo   - Gson...
if not exist "lib\gson-2.10.1.jar" (
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar' -OutFile 'lib\gson-2.10.1.jar'"
)

echo   - HikariCP...
if not exist "lib\HikariCP-5.1.0.jar" (
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar' -OutFile 'lib\HikariCP-5.1.0.jar'"
)

echo   - SLF4J API...
if not exist "lib\slf4j-api-2.0.9.jar" (
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar' -OutFile 'lib\slf4j-api-2.0.9.jar'"
)

echo   - SLF4J Simple...
if not exist "lib\slf4j-simple-2.0.9.jar" (
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar' -OutFile 'lib\slf4j-simple-2.0.9.jar'"
)

echo.
echo [2/3] Compilation du code Java...
if exist "bin" rmdir /s /q bin
mkdir bin

dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d bin -cp "lib\*" @sources.txt
del sources.txt

if %errorlevel% neq 0 (
    echo.
    echo ERREUR: La compilation a echoue
    pause
    exit /b 1
)

echo.
echo [3/3] Demarrage du serveur...
echo.
java -cp "bin;lib\*" com.projectmanagement.StandaloneServer

pause
