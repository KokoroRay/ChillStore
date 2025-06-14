package com.esms.service;

import com.esms.model.entity.Warehouse;
import java.util.List;

/**
 * Interface WarehouseService định nghĩa các phương thức liên quan đến nghiệp vụ warehouse.
 * Đây là nơi khai báo các hành động chính như thêm, sửa, xóa, tìm kiếm và truy vấn các giao dịch warehouse.
 */
public interface WarehouseService {
    // Basic CRUD operations
    List<Warehouse> getAllWarehouseTransactions();
    Warehouse getWarehouseTransactionById(Integer id);
    List<Warehouse> getWarehouseTransactionsByProductId(Integer productId);
    Warehouse addWarehouseTransaction(Warehouse warehouse);
    Warehouse updateWarehouseTransaction(Integer id, Warehouse warehouse);
    void deleteWarehouseTransaction(Integer id);
    
    // Search operations
    List<Warehouse> searchWarehouseByProductName(String productName);
    
    // Additional operations
    List<Warehouse> findByTransactionType(String transactionType);
    List<Warehouse> findByDateRange(String startDate, String endDate);
}
