@echo off
echo Stopping MySQL service...
net stop mysql

echo Starting MySQL in safe mode...
mysqld --skip-grant-tables --skip-networking

echo In another command prompt, run:
echo mysql -u root
echo Then execute: ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';
echo Then execute: FLUSH PRIVILEGES;
echo Then restart MySQL service normally

pause