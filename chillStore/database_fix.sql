-- Script để tăng giới hạn cột total_amount và discount_amount trong bảng orders
-- Chạy script này nếu muốn tăng giới hạn tổng tiền đơn hàng

USE [group4_swp_chillStore_Final11]
GO

-- Tăng giới hạn total_amount từ decimal(10,2) thành decimal(15,2) 
-- Cho phép tối đa 9,999,999,999,999.99 VND
ALTER TABLE [dbo].[orders] 
ALTER COLUMN [total_amount] decimal(15,2);

-- Tăng giới hạn discount_amount từ decimal(10,2) thành decimal(15,2)
ALTER TABLE [dbo].[orders] 
ALTER COLUMN [discount_amount] decimal(15,2);

-- Kiểm tra thay đổi
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    NUMERIC_PRECISION,
    NUMERIC_SCALE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'orders' 
AND COLUMN_NAME IN ('total_amount', 'discount_amount');

PRINT 'Database schema updated successfully!';
PRINT 'New limits:';
PRINT '- total_amount: up to 9,999,999,999,999.99 VND';
PRINT '- discount_amount: up to 9,999,999,999,999.99 VND';