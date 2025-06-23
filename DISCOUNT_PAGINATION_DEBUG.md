# Discount Pagination Debug Guide

## Vấn đề đã được sửa

### 1. Phân trang mặc định 3 discount mỗi trang
- Đã thay đổi `defaultValue = "3"` trong controller
- Cập nhật page size selector với các tùy chọn: 3, 6, 9, 12
- Hiển thị thông tin "Single page" khi chỉ có 1 trang

### 2. Hiển thị phân trang ngay cả khi chỉ có 1 trang
- Thay đổi điều kiện từ `th:if="${totalPages > 1}"` thành `th:if="${totalDiscounts > 0}"`
- Luôn hiển thị thông tin trang và page size selector
- Chỉ ẩn nút phân trang khi chỉ có 1 trang

### 3. Debug Information
- Thêm alert box hiển thị thông tin debug
- Hiển thị: Total discounts, Current page, Total pages, Page size, Start-End index

## Cách kiểm tra phân trang

### 1. Kiểm tra có đủ dữ liệu không
```sql
-- Kiểm tra số lượng discount trong database
SELECT COUNT(*) FROM promotions;
```

### 2. Tạo thêm dữ liệu test
```sql
-- Thêm một số discount test
INSERT INTO promotions (code, description, discount_pct, start_date, end_date, active, created_by, apply_type) 
VALUES 
('TEST1', 'Test Discount 1', 10.00, '2024-01-01', '2024-12-31', 1, 1, 'product'),
('TEST2', 'Test Discount 2', 15.00, '2024-01-01', '2024-12-31', 1, 1, 'brand'),
('TEST3', 'Test Discount 3', 20.00, '2024-01-01', '2024-12-31', 1, 1, 'category'),
('TEST4', 'Test Discount 4', 25.00, '2024-01-01', '2024-12-31', 1, 1, 'product'),
('TEST5', 'Test Discount 5', 30.00, '2024-01-01', '2024-12-31', 1, 1, 'brand');
```

### 3. Kiểm tra URL parameters
- Trang đầu: `/admin/discount/list?page=0&size=3`
- Trang 2: `/admin/discount/list?page=1&size=3`
- Thay đổi page size: `/admin/discount/list?page=0&size=6`

### 4. Kiểm tra JavaScript
- Mở Developer Tools (F12)
- Kiểm tra Console để xem có lỗi JavaScript không
- Test function `changePageSize()` và `goToPage()`

## Các tính năng phân trang

### 1. Navigation Controls
- **First Page**: Chuyển về trang đầu
- **Previous**: Trang trước
- **Page Numbers**: Hiển thị số trang với ellipsis (...)
- **Next**: Trang tiếp theo
- **Last Page**: Chuyển đến trang cuối

### 2. Page Size Options
- 3 items per page (mặc định)
- 6 items per page
- 9 items per page
- 12 items per page

### 3. Page Information
- Hiển thị: "Showing X to Y of Z discounts"
- Thông tin trang: "Page X of Y" hoặc "Single page"

### 4. Responsive Design
- Pagination controls responsive trên mobile
- Page size selector tự động điều chỉnh

## Troubleshooting

### 1. Không hiển thị phân trang
- Kiểm tra có dữ liệu discount không
- Kiểm tra debug info box
- Kiểm tra console errors

### 2. Phân trang không hoạt động
- Kiểm tra URL parameters
- Kiểm tra JavaScript functions
- Kiểm tra controller logic

### 3. Page size không thay đổi
- Kiểm tra JavaScript function `changePageSize()`
- Kiểm tra URL parameters
- Kiểm tra controller parameter binding

## Xóa Debug Information

Khi phân trang hoạt động ổn định, xóa debug box:

```html
<!-- Xóa đoạn code này -->
<div class="alert alert-info alert-dismissible fade show" role="alert" th:if="${totalDiscounts > 0}">
    <strong>Debug Info:</strong> 
    Total: <span th:text="${totalDiscounts}">0</span> | 
    Page: <span th:text="${currentPage + 1}">1</span>/<span th:text="${totalPages}">1</span> | 
    Size: <span th:text="${pageSize}">3</span> | 
    Showing: <span th:text="${startIndex}">1</span>-<span th:text="${endIndex}">3</span>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
```

## Kết luận

Phân trang đã được cấu hình để hiển thị 3 discount mỗi trang theo yêu cầu. Hệ thống sẽ:

1. Hiển thị thông tin phân trang ngay cả khi chỉ có 1 trang
2. Cho phép thay đổi page size từ 3 đến 12 items
3. Cung cấp navigation controls đầy đủ
4. Hiển thị debug information để troubleshooting
5. Responsive trên tất cả thiết bị 