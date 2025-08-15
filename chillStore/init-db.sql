-- Create database if not exists
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'group4_swp_chillStore_Final11')
BEGIN
    PRINT 'Creating database group4_swp_chillStore_Final11...'
    CREATE DATABASE [group4_swp_chillStore_Final11];
    PRINT 'Database group4_swp_chillStore_Final11 created successfully';
END
ELSE
BEGIN
    PRINT 'Database group4_swp_chillStore_Final11 already exists';
END
GO

-- Switch to the database
USE [group4_swp_chillStore_Final11];
GO

-- Verify database is ready
SELECT 'Database initialization completed' AS Status;
GO