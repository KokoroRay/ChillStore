package com.esms.service;

import com.esms.model.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    List<Warehouse> findByProductId(Integer productId);
    List<Warehouse> findByType(String type);
    List<Warehouse> findByAdminId(Integer adminId);
    //update

    /**
     * Nhập sản phẩm vào kho
     * @param productId ID của sản phẩm
     * @param quantity Số lượng nhập
     * @param notes Ghi chú
     * @return Warehouse transaction đã tạo
     */
    Warehouse importProduct(Integer productId, Integer quantity, String notes);

    /**
     * Xuất sản phẩm khỏi kho
     * @param productId ID của sản phẩm
     * @param quantity Số lượng xuất
     * @param notes Ghi chú
     * @return Warehouse transaction đã tạo
     */
    Warehouse exportProduct(Integer productId, Integer quantity, String notes);

    // Lấy tất cả giao dịch kho với phân trang
    Page<Warehouse> getAllWarehouseTransactions(Pageable pageable);

    // Tìm kiếm theo tên sản phẩm với phân trang
    Page<Warehouse> searchWarehouseByProductName(String keyword, Pageable pageable);
}