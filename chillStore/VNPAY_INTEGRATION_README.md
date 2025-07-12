# VNPay Integration - ChillStore

## Tổng quan
Hệ thống tích hợp thanh toán VNPay hoàn chỉnh cho ứng dụng ChillStore, cho phép khách hàng thanh toán đơn hàng thông qua cổng thanh toán VNPay.

## Các thành phần chính

### 1. Configuration
- **VNPayConfig**: Cấu hình VNPay (TMN Code, Hash Secret, URLs, etc.)
- **application.properties**: Cấu hình VNPay parameters

### 2. DTOs
- **VNPayRequestDTO**: Dữ liệu request thanh toán
- **VNPayResponseDTO**: Dữ liệu response từ VNPay

### 3. Services
- **VNPayService**: Interface định nghĩa các phương thức VNPay
- **VNPayServiceImpl**: Implementation xử lý logic VNPay

### 4. Controllers
- **PaymentController**: Xử lý thanh toán VNPay và callbacks

### 5. Templates
- `payment/vnpay.html`: Trang thanh toán VNPay
- `payment/payment-success.html`: Trang thanh toán thành công
- `payment/payment-failed.html`: Trang thanh toán thất bại

## Cấu hình VNPay

### 1. Cấu hình trong application.properties
```properties
# VNPay Configuration
vnpay.tmnCode=YN5UKJEW
vnpay.hashSecret=A9O9D0KTKNVC8U9BB81KA3SOAYYF8GY1
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.returnUrl=http://localhost:8080/payment/vnpay/callback
vnpay.ipnUrl=http://localhost:8080/payment/vnpay/ipn
vnpay.command=pay
vnpay.currCode=VND
vnpay.locale=vn
vnpay.version=2.1.0
```

### 2. Cấu hình cho Production
```properties
# Production VNPay URLs
vnpay.url=https://pay.vnpay.vn/vpcpay.html
vnpay.returnUrl=https://yourdomain.com/payment/vnpay/callback
vnpay.ipnUrl=https://yourdomain.com/payment/vnpay/ipn
```

## Quy trình thanh toán

### 1. Khởi tạo thanh toán
1. Khách hàng chọn VNPay trong checkout
2. Hệ thống tạo VNPayRequestDTO với thông tin đơn hàng
3. Gọi VNPayService.createPaymentUrl() để tạo URL thanh toán
4. Redirect khách hàng đến VNPay gateway

### 2. Xử lý thanh toán
1. Khách hàng thanh toán trên VNPay gateway
2. VNPay xử lý thanh toán và gửi callback
3. Hệ thống nhận callback và validate
4. Cập nhật trạng thái đơn hàng

### 3. Kết quả thanh toán
- **Thành công**: Redirect đến trang success
- **Thất bại**: Redirect đến trang failed

## API Endpoints

### Payment URLs
- `GET /payment/vnpay/{orderId}`: Trang thanh toán VNPay
- `GET /payment/vnpay/callback`: Callback từ VNPay
- `POST /payment/vnpay/ipn`: IPN từ VNPay
- `GET /payment/success/{orderId}`: Trang thanh toán thành công
- `GET /payment/failed/{orderId}`: Trang thanh toán thất bại

### Callback Parameters
VNPay sẽ gửi các tham số sau trong callback:
- `vnp_ResponseCode`: Mã phản hồi (00 = thành công)
- `vnp_TxnRef`: Mã đơn hàng
- `vnp_Amount`: Số tiền (x100)
- `vnp_SecureHash`: Hash bảo mật
- `vnp_BankTranNo`: Mã giao dịch ngân hàng
- `vnp_TransactionNo`: Mã giao dịch VNPay
- `vnp_ResponseMessage`: Thông điệp phản hồi

## Bảo mật

### 1. Hash Validation
- Tất cả callback từ VNPay đều được validate hash
- Sử dụng HMAC-SHA512 để tạo và kiểm tra hash
- Hash secret được cấu hình trong application.properties

### 2. IPN (Instant Payment Notification)
- VNPay gửi IPN để xác nhận thanh toán
- IPN được xử lý riêng biệt với callback
- Trả về "OK" cho thành công, "FAIL" cho thất bại

### 3. Error Handling
- Xử lý lỗi khi tạo URL thanh toán
- Xử lý lỗi khi validate callback
- Rollback transaction khi có lỗi

## Testing

### 1. Sandbox Testing
```properties
# Sandbox URLs
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.returnUrl=http://localhost:8080/payment/vnpay/callback
```

### 2. Test Cards
VNPay cung cấp các thẻ test:
- **Thẻ thành công**: 9704000000000018
- **Thẻ thất bại**: 9704000000000026
- **Thẻ chưa đăng ký**: 9704000000000034

### 3. Test Scenarios
1. **Thanh toán thành công**: Sử dụng thẻ thành công
2. **Thanh toán thất bại**: Sử dụng thẻ thất bại
3. **Hủy thanh toán**: Nhấn nút hủy trên VNPay
4. **Timeout**: Để hết thời gian thanh toán

## Deployment

### 1. Production Setup
1. Đăng ký tài khoản merchant với VNPay
2. Lấy TMN Code và Hash Secret từ VNPay
3. Cấu hình return URL và IPN URL
4. Test với sandbox trước khi chuyển production

### 2. SSL Certificate
- Bắt buộc có SSL certificate cho production
- VNPay chỉ chấp nhận HTTPS cho callback URLs

### 3. Firewall Configuration
- Mở port cho VNPay IPN
- Whitelist VNPay IP addresses nếu cần

## Monitoring & Logging

### 1. Payment Logs
- Log tất cả payment requests
- Log callback responses
- Log payment status changes

### 2. Error Monitoring
- Monitor failed payments
- Alert khi có lỗi callback
- Track payment success rate

### 3. Analytics
- Payment volume tracking
- Success rate analysis
- Revenue reporting

## Troubleshooting

### 1. Common Issues
- **Hash validation failed**: Kiểm tra hash secret
- **Callback not received**: Kiểm tra return URL
- **IPN not working**: Kiểm tra IPN URL và firewall
- **Amount mismatch**: Kiểm tra format amount (x100)

### 2. Debug Mode
```properties
# Enable debug logging
logging.level.com.esms.service.VNPayService=DEBUG
logging.level.com.esms.controller.PaymentController=DEBUG
```

### 3. Support
- VNPay Support: support@vnpay.vn
- Documentation: https://sandbox.vnpayment.vn/apis/docs/huong-dan-tich-hop

## Security Best Practices

### 1. Configuration Security
- Không commit hash secret vào git
- Sử dụng environment variables
- Rotate hash secret định kỳ

### 2. Input Validation
- Validate tất cả input từ VNPay
- Sanitize data trước khi xử lý
- Implement rate limiting

### 3. Error Handling
- Không expose sensitive data trong error messages
- Log errors với context đầy đủ
- Implement proper exception handling

## Performance Optimization

### 1. Caching
- Cache VNPay configuration
- Cache payment URLs
- Implement connection pooling

### 2. Async Processing
- Process IPN asynchronously
- Use message queues cho payment processing
- Implement retry mechanism

### 3. Database Optimization
- Index payment tables
- Archive old payment records
- Optimize payment queries

## Future Enhancements

### 1. Additional Payment Methods
- Tích hợp các cổng thanh toán khác
- Support international payments
- Implement wallet payments

### 2. Advanced Features
- Recurring payments
- Payment plans
- Refund processing
- Payment analytics

### 3. Mobile Integration
- Mobile payment SDK
- QR code payments
- In-app payments 