# Discount Debug Guide

## Các lỗi đã được sửa:

### 1. **Layout Issues (Đã sửa)**
- ✅ **list.html**: Đã sửa layout để không bị sidebar che
- ✅ **form.html**: Đã sửa layout để không bị sidebar che
- ✅ **Responsive design**: Hoạt động tốt trên mobile

### 2. **Lombok Issues (Đã sửa)**
- ✅ **Discount.java**: Đã thay thế Lombok bằng get/set thủ công
- ✅ **DiscountDto.java**: Đã thay thế Lombok bằng get/set thủ công
- ✅ **Comment chi tiết**: Đã thêm comment cho từng field và method

### 3. **Controller Issues (Đã sửa)**
- ✅ **saveDiscount method**: Đã cải thiện validation và error handling
- ✅ **createdBy field**: Đã set giá trị mặc định
- ✅ **Better error messages**: Thông báo lỗi rõ ràng hơn

## Cách test chức năng Add Discount:

### 1. **Test Form đơn giản:**
```
URL: http://localhost:8080/admin/discount/test
```
- Form đơn giản để test chức năng cơ bản
- Không có Select2, chỉ có các field cần thiết

### 2. **Form đầy đủ:**
```
URL: http://localhost:8080/admin/discount/create
```
- Form đầy đủ với Select2 và cascading dropdowns
- Có validation phía client

### 3. **List page:**
```
URL: http://localhost:8080/admin/discount/list
```
- Hiển thị danh sách discount
- Có search và filter

## Các bước debug:

### 1. **Kiểm tra Database:**
```sql
-- Kiểm tra bảng promotions
SELECT * FROM promotions;

-- Kiểm tra bảng discount_products (nếu có)
SELECT * FROM discount_products;
```

### 2. **Kiểm tra Logs:**
- Xem console logs khi submit form
- Kiểm tra error messages

### 3. **Test với dữ liệu đơn giản:**
```
Code: TEST001
Description: Test discount
Percentage: 10.00
Start Date: 2024-01-01
End Date: 2024-12-31
Apply Type: product
Active: true
```

## Các vấn đề có thể gặp:

### 1. **Validation Errors:**
- Code đã tồn tại
- Ngày end < ngày start
- Percentage <= 0
- Thiếu required fields

### 2. **Database Errors:**
- Bảng không tồn tại
- Constraint violations
- Connection issues

### 3. **Service Errors:**
- Null pointer exceptions
- Conversion errors
- Business logic errors

## Cách khắc phục:

### 1. **Nếu form không submit được:**
- Kiểm tra JavaScript console
- Kiểm tra network tab
- Kiểm tra server logs

### 2. **Nếu có lỗi validation:**
- Kiểm tra dữ liệu input
- Kiểm tra validation rules
- Kiểm tra error messages

### 3. **Nếu có lỗi database:**
- Kiểm tra database connection
- Kiểm tra table structure
- Kiểm tra constraints

## Test Cases:

### Test Case 1: Tạo discount cơ bản
```
Input:
- Code: BASIC001
- Description: Basic discount
- Percentage: 15.00
- Start Date: 2024-01-01
- End Date: 2024-12-31
- Apply Type: product
- Active: true

Expected: Discount được tạo thành công
```

### Test Case 2: Tạo discount với code trùng
```
Input:
- Code: BASIC001 (đã tồn tại)
- ... other fields

Expected: Error message "Discount code already exists"
```

### Test Case 3: Tạo discount với ngày không hợp lệ
```
Input:
- Start Date: 2024-12-31
- End Date: 2024-01-01

Expected: Error message "End date must be after start date"
```

## Monitoring:

### 1. **Application Logs:**
```bash
# Xem logs real-time
tail -f logs/application.log
```

### 2. **Database Monitoring:**
```sql
-- Xem số lượng discount
SELECT COUNT(*) FROM promotions;

-- Xem discount mới nhất
SELECT * FROM promotions ORDER BY promo_id DESC LIMIT 5;
```

### 3. **Performance Monitoring:**
- Response time của API
- Database query performance
- Memory usage

## Troubleshooting Checklist:

- [ ] Database connection OK
- [ ] Tables exist and have correct structure
- [ ] Form validation working
- [ ] Controller methods accessible
- [ ] Service methods working
- [ ] Repository methods working
- [ ] Error handling working
- [ ] Success redirect working
- [ ] List page displaying correctly
- [ ] Search and filter working

## Next Steps:

1. Test với form đơn giản trước
2. Nếu OK, test với form đầy đủ
3. Test các validation scenarios
4. Test error handling
5. Test success scenarios
6. Test list page functionality 