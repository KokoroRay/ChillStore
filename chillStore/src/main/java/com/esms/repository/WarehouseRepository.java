package com.esms.repository;

import com.esms.model.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
    /**
     * Tìm danh sách giao dịch kho có tên sản phẩm chứa từ khóa, dùng phân trang.
     * Dùng trong chức năng tìm kiếm có paging.
     *
     * @param keyword từ khóa cần tìm trong tên sản phẩm
     * @param pageable đối tượng phân trang (số trang, kích thước trang,...)
     * @return danh sách trang các giao dịch phù hợp
     */
    @Query("SELECT w FROM Warehouse w WHERE w.product.name LIKE %:keyword%")
    Page<Warehouse> findByProductNameContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Lấy tất cả giao dịch của một sản phẩm cụ thể (theo productId), sắp xếp theo thời gian giảm dần.
     * Dùng để hiển thị lịch sử nhập/xuất của một sản phẩm.
     *
     * @param productId ID của sản phẩm
     * @return danh sách giao dịch kho liên quan đến sản phẩm
     */
    @Query("SELECT w FROM Warehouse w WHERE w.product.productId = :productId ORDER BY w.transDate DESC")
    List<Warehouse> findByProductId(@Param("productId") Integer productId);


    /**
     * Lấy tất cả giao dịch theo loại ('import' hoặc 'export'), sắp xếp theo thời gian mới nhất.
     * Dùng để lọc riêng các giao dịch nhập hoặc xuất.
     *
     * @param type loại giao dịch ("import" hoặc "export")
     * @return danh sách giao dịch theo loại
     */
    @Query("SELECT w FROM Warehouse w WHERE w.type = :type ORDER BY w.transDate DESC")
    List<Warehouse> findByType(@Param("type") String type);

    /**
     * Lấy tất cả giao dịch do một admin thực hiện, sắp xếp theo thời gian giảm dần.
     * Dùng để theo dõi hoạt động của nhân viên kho hoặc admin.
     *
     * @param adminId ID của admin
     * @return danh sách giao dịch theo admin
     */
    @Query("SELECT w FROM Warehouse w WHERE w.admin.adminId = :adminId ORDER BY w.transDate DESC")
    List<Warehouse> findByAdminId(@Param("adminId") Integer adminId);

    /**
     * Tìm tất cả giao dịch kho có tên sản phẩm chứa từ khóa.
     * Khác với phương thức trên ở chỗ không hỗ trợ phân trang.
     * Thường dùng khi muốn xuất file Excel toàn bộ danh sách tìm được.
     *
     * @param productName từ khóa tìm kiếm tên sản phẩm
     * @return danh sách tất cả giao dịch khớp
     */
    @Query("SELECT w FROM Warehouse w WHERE w.product.name LIKE %:productName% ORDER BY w.transDate DESC")
    List<Warehouse> searchByProductName(@Param("productName") String productName);
}
