@echo off
echo ========================================
echo RevHub MySQL One-Time Setup
echo ========================================
echo.
echo This script will set MySQL root password to '10532'
echo Run this ONLY ONCE, then services will auto-connect
echo.
pause

echo Setting up MySQL...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < one_time_mysql_setup.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS: MySQL setup completed!
    echo ========================================
    echo.
    echo Your services will now auto-connect to MySQL
    echo You can start your services normally
    echo.
) else (
    echo.
    echo ========================================
    echo ERROR: Setup failed
    echo ========================================
    echo.
    echo Please check if:
    echo 1. MySQL service is running
    echo 2. You entered the correct current password
    echo.
)

pause