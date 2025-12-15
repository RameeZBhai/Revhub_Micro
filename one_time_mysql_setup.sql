-- One-time MySQL setup for RevHub project
-- Run this ONLY ONCE to set up MySQL root password
-- After this, services will auto-connect without manual setup

-- Set root password to match application configuration
ALTER USER 'root'@'localhost' IDENTIFIED BY '10532';

-- Grant all privileges to root user
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;

-- Create databases (optional - services will auto-create them)
CREATE DATABASE IF NOT EXISTS revhub_auth;
CREATE DATABASE IF NOT EXISTS revhub_users;
CREATE DATABASE IF NOT EXISTS revhub_follows;

-- Apply changes
FLUSH PRIVILEGES;

-- Show confirmation
SELECT 'MySQL setup completed successfully! Services can now auto-connect.' AS Status;