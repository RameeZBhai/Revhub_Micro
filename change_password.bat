@echo off
echo Changing MySQL root password to 'root'
mysqladmin -u root -p password root
echo Password changed successfully!