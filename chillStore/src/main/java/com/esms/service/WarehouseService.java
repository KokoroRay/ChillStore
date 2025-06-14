package com.esms.service;

import com.esms.model.entity.Warehouse;
import java.util.List;

/**
 * Interface WarehouseService định nghĩa các phương thức liên quan đến nghiệp vụ warehouse.
 * Đây là nơi khai báo các hành động chính như thêm, sửa, xóa, tìm kiếm và truy vấn các giao dịch warehouse.
 */
public interface WarehouseService {
    //Các method CRUD cơ bản

    Warehouse save(Warehouse warehouse); //Lưu or cập nhật 1 giao dịch warehouse vào DB
    Warehouse findById(Integer id); //Search 1 giao dịch
    List<Warehouse> findAll(); //Lấy tất cả giao dịch warehouse từ DB
    void deleteById(Integer id); //Xóa một giao dch warehouse dựa trên ID của nó
    List<Warehouse> getAllWarehouseTransactions();
    List<Warehouse> searchWarehouseByProductName(String productName);
    //update

    List<Warehouse> getWarehouseTransactionsByProductId(Integer productId);
    Warehouse addWarehouseTransaction(Warehouse warehouse);
    Warehouse updateWarehouseTransaction(Integer id, Warehouse warehouse);
    void deleteWarehouseTransaction(Integer id);
}
