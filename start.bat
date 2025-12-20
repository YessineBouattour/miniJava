@echo off
cls
echo ======================================
echo   Demarrage Direct - Sans Maven
echo ======================================
echo.
echo Mot de passe MySQL:
set /p DB_PASSWORD=

echo.
echo Configuration...
(
echo db.url=jdbc:mysql://localhost:3306/project_management?useSSL=false^&serverTimezone=UTC
echo db.username=root
echo db.password=%DB_PASSWORD%
echo db.driver=com.mysql.cj.jdbc.Driver
echo db.pool.maximumPoolSize=20
echo db.pool.minimumIdle=5
echo db.pool.connectionTimeout=30000
) > src\main\resources\db.properties

if not exist "lib" mkdir lib
if not exist "bin" mkdir bin

echo Telechargement...
if not exist "lib\mysql-connector-j-8.2.0.jar" powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.2.0/mysql-connector-j-8.2.0.jar' -OutFile 'lib\mysql-connector-j-8.2.0.jar'"
if not exist "lib\gson-2.10.1.jar" powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar' -OutFile 'lib\gson-2.10.1.jar'"
if not exist "lib\HikariCP-5.1.0.jar" powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar' -OutFile 'lib\HikariCP-5.1.0.jar'"
if not exist "lib\slf4j-api-2.0.9.jar" powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar' -OutFile 'lib\slf4j-api-2.0.9.jar'"
if not exist "lib\slf4j-simple-2.0.9.jar" powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar' -OutFile 'lib\slf4j-simple-2.0.9.jar'"

echo.
echo Compilation...
dir /s /b src\main\java\com\projectmanagement\model\*.java src\main\java\com\projectmanagement\dao\*.java src\main\java\com\projectmanagement\service\*.java src\main\java\com\projectmanagement\util\*.java > sources.txt
javac -encoding UTF-8 -d bin -cp "lib\*" @sources.txt 2>nul
if %errorlevel% neq 0 (
    echo Erreur compilation
    del sources.txt
    pause
    exit /b 1
)

dir /s /b src\main\java\com\projectmanagement\SimpleServer.java >> sources.txt
javac -encoding UTF-8 -d bin -cp "lib\*;bin" @sources.txt
del sources.txt

xcopy /E /I /Y src\main\resources bin\resources >nul 2>&1

echo.
echo Demarrage...
echo.
java -cp "bin;lib\*" com.projectmanagement.SimpleServer

pause
