# ChillStore - Hệ thống Bán Thiết Bị Điện Tử

<!-- Chèn logo/banner của ChillStore -->

![ChillStore Logo](/images/logo.png)

## 📋 Giới thiệu

ChillStore là wedsite chuyên kinh doanh các mặt hàng điện lạnh, gia dụng. Dự án được phát triển bằng Spring Boot với kiến trúc MVC, hỗ trợ đầy đủ các chức năng của một website bán hàng hiện đại.

<!-- Chèn ảnh chụp màn hình trang chủ -->

![Trang chủ ChillStore](images/home.png)

## 🚀 Tính năng chính

### Khách hàng (Customer)

- **Đăng ký/Đăng nhập**: Hỗ trợ đăng ký thông thường và OAuth2 (Google)
- **Quản lý tài khoản**: Chỉnh sửa thông tin cá nhân, đổi mật khẩu
- **Mua sắm**: Xem sản phẩm, thêm vào giỏ hàng, thanh toán
- **Đơn hàng**: Theo dõi lịch sử đơn hàng, trạng thái giao hàng
- **Đánh giá**: Đánh giá và phản hồi sản phẩm
- **Voucher**: Sử dụng mã giảm giá
- **Wishlist**: Danh sách yêu thích

<!-- Chèn ảnh giao diện khách hàng -->

![Giao diện khách hàng](images/customer-interface.png)

### Nhân viên (Staff)

- **Quản lý đơn hàng**: Xử lý và cập nhật trạng thái đơn hàng
- **Quản lý sản phẩm**: Xem thông tin sản phẩm
- **Quản lý khách hàng**: Xem thông tin khách hàng
- **Phản hồi**: Trả lời đánh giá của khách hàng
- **Bảo trì**: Quản lý yêu cầu bảo trì

<!-- Chèn ảnh dashboard nhân viên -->

![Dashboard nhân viên](images/staff-dashboard.png)

### Quản trị viên (Admin)

- **Quản lý sản phẩm**: CRUD sản phẩm, danh mục, thương hiệu
- **Quản lý người dùng**: Quản lý khách hàng và nhân viên
- **Quản lý kho**: Nhập/xuất hàng, theo dõi tồn kho
- **Quản lý khuyến mãi**: Tạo và quản lý voucher, giảm giá
- **Báo cáo doanh thu**: Thống kê và phân tích doanh số
- **Quản lý phản hồi**: Xem và xử lý phản hồi khách hàng

<!-- Chèn ảnh dashboard admin -->

![Dashboard admin](images/admin-dashboard.png)

## 🛠️ Công nghệ sử dụng

<!-- Chèn sơ đồ kiến trúc hệ thống -->

![Kiến trúc hệ thống](images/system-architecture.png)

### Backend

- **Framework**: Spring Boot 3.5.0
- **Java**: JDK 17
- **Database**: SQL Server, MySQL (hỗ trợ), H2 (testing)
- **ORM**: Spring Data JPA, Hibernate
- **Security**: Spring Security 6
- **Authentication**: OAuth2 (Google), JWT
- **Migration**: Flyway
- **Caching**: Caffeine Cache
- **Validation**: Bean Validation
- **Mapping**: MapStruct
- **Build Tool**: Maven

### Frontend

- **Template Engine**: Thymeleaf
- **CSS Framework**: Custom CSS
- **JavaScript**: Vanilla JS
- **UI Components**: Custom components

### Tích hợp bên thứ 3

- **Payment**: VNPay
- **Email**: Gmail SMTP
- **OAuth2**: Google OAuth2
- **File Export**: Apache POI (Excel)

<!-- Chèn ảnh tech stack -->

![Tech Stack](images/tech-stack.png)

## 📁 Cấu trúc dự án

```
chillStore/
├── src/main/java/com/esms/
│   ├── config/           # Cấu hình ứng dụng
│   ├── controller/       # REST Controllers
│   ├── exception/        # Exception handlers
│   ├── model/
│   │   ├── dto/         # Data Transfer Objects
│   │   └── entity/      # JPA Entities
│   ├── repository/       # Data repositories
│   ├── service/         # Business logic
│   ├── util/            # Utilities
│   └── validation/      # Custom validators
├── src/main/resources/
│   ├── static/          # CSS, JS, Images
│   ├── templates/       # Thymeleaf templates
│   └── db/migration/    # Flyway migrations
└── src/test/            # Unit tests
```

## ⚙️ Cài đặt và chạy dự án

### Yêu cầu hệ thống

- Java 17+
- Maven 3.6+
- SQL Server 2019+
- IDE: IntelliJ IDEA hoặc Eclipse

### Bước 1: Clone dự án

```bash
git clone <repository-url>
cd chillStore
```

### Bước 2: Cấu hình database

1. Tạo database trong SQL Server:

```sql
CREATE DATABASE group4_swp_chillStore_Final11;
```

2. Cập nhật thông tin database trong `application.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=group4_swp_chillStore_Final11;encrypt=true;trustServerCertificate=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Bước 3: Cấu hình email

Cập nhật thông tin email trong `application.properties`:

```properties
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```

### Bước 4: Cấu hình OAuth2 Google

1. Tạo project trên Google Cloud Console
2. Tạo OAuth2 credentials
3. Cập nhật trong `application.properties`:

```properties
spring.security.oauth2.client.registration.google.client-id=your_client_id
spring.security.oauth2.client.registration.google.client-secret=your_client_secret
```

### Bước 5: Cấu hình VNPay

Cập nhật thông tin VNPay trong `application.properties`:

```properties
vnpay.tmnCode=your_tmn_code
vnpay.hashSecret=your_hash_secret
vnpay.returnUrl=your_return_url
vnpay.ipnUrl=your_ipn_url
```

### Bước 6: Chạy ứng dụng

```bash
mvn clean install
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

<!-- Chèn ảnh ứng dụng đang chạy -->

![Ứng dụng chạy thành công](images/app-running.png)

## 🧪 Testing

Chạy unit tests:

```bash
mvn test
```

Các test cases bao gồm:

- ChangePasswordTest
- CustomerLockTest
- EditProfileTest
- VNPayServiceTest

<!-- Chèn ảnh kết quả test -->

![Kết quả test](images/test-results.png)

## 📊 Database Schema

<!-- Chèn ERD diagram -->

![Database ERD](images/database-erd.png)

Dự án sử dụng các bảng chính:

- `customers` - Thông tin khách hàng
- `products` - Sản phẩm
- `categories` - Danh mục sản phẩm
- `brands` - Thương hiệu
- `orders` - Đơn hàng
- `order_items` - Chi tiết đơn hàng
- `carts` - Giỏ hàng
- `vouchers` - Mã giảm giá
- `discounts` - Khuyến mãi
- `feedbacks` - Đánh giá
- `warehouse` - Quản lý kho
- `maintenance` - Bảo trì
- `staff` - Nhân viên
- `admin` - Quản trị viên

## 🔐 Bảo mật

- **Authentication**: Spring Security với session-based và OAuth2
- **Authorization**: Role-based access control (CUSTOMER, STAFF, ADMIN)
- **Password**: BCrypt hashing
- **CSRF Protection**: Enabled
- **SQL Injection**: Prevented by JPA/Hibernate
- **XSS Protection**: Thymeleaf auto-escaping

## 🚀 Deployment

### Production Build

```bash
mvn clean package -Pprod
```

### Docker (Optional)

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/chillStore-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 📝 API Documentation

Các endpoint chính:

- `GET /` - Trang chủ
- `GET /login` - Đăng nhập
- `GET /register` - Đăng ký
- `GET /products/{id}` - Chi tiết sản phẩm
- `POST /cart/add` - Thêm vào giỏ hàng
- `GET /checkout` - Thanh toán
- `GET /admin/**` - Quản trị
- `GET /staff/**` - Nhân viên

<!-- Chèn ảnh API documentation hoặc Postman collection -->

![API Documentation](images/api-docs.png)

## 🤝 Đóng góp

1. Fork dự án
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

## 📄 License

Dự án này được phát triển cho mục đích học tập trong khóa SWP391.

## 👥 Nhóm phát triển

- **Nhóm**: Group 4
- **Môn học**: SWP391
- **Học kỳ**: Spring 2025
- **Trường**: FPT University

<!-- Chèn ảnh team members hoặc group photo -->

![Team Members](images/team-photo.png)

## 📞 Liên hệ

Nếu có bất kỳ câu hỏi nào, vui lòng liên hệ qua email: kokororay356@gmail.com

---

<!-- Chèn ảnh demo các tính năng chính -->

![Demo Features](images/features-demo.gif)

**ChillStore** - Mang đến trải nghiệm mua sắm thiết bị điện tử tuyệt vời! 🛒✨
