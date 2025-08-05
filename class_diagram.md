# Class Diagram - Feedback Filtering System

```mermaid
classDiagram
    %% External Actor
    class Customer {
        <<Actor>>
    }
    
    %% Frontend Layer
    class FrontendUI {
        <<Frontend>>
        +displayFilteredFeedbacks()
        +applyRatingFilter()
    }
    
    %% Controller Layer
    class CustomerProductController {
        -FeedbackService feedbackService
        +getFeedbacksByProduct(productId, page, size, rating) Map~String, Object~
        -createPageable(page, size) Pageable
        -validateRating(rating) boolean
        -buildResultMap(feedbackPage) Map~String, Object~
    }
    
    %% Service Layer
    class FeedbackService {
        <<interface>>
        +getFeedbacksByProductIdPaged(productId, pageable) Page~FeedbackDTO~
        +getFeedbacksByProductIdAndRatingPaged(productId, rating, pageable) Page~FeedbackDTO~
        +addFeedback(customerId, productId, rating, comment) Feedback
        +updateFeedback(feedbackId, rating, comment, customerId) Feedback
        +deleteFeedback(feedbackId, customerId) void
        +getAllFeedbacks() List~FeedbackDTO~
        +getFeedbacksByProductId(productId) List~Feedback~
        +getFeedbackByCustomerAndProduct(customerId, productId) Feedback
    }
    
    class FeedbackServiceImpl {
        -FeedbackRepository feedbackRepository
        -ProductRepository productRepository
        -CustomerRepository customerRepository
        +getFeedbacksByProductIdPaged(productId, pageable) Page~FeedbackDTO~
        +getFeedbacksByProductIdAndRatingPaged(productId, rating, pageable) Page~FeedbackDTO~
        +addFeedback(customerId, productId, rating, comment) Feedback
        +updateFeedback(feedbackId, rating, comment, customerId) Feedback
        +deleteFeedback(feedbackId, customerId) void
        -convertToDTO(feedback) FeedbackDTO
        -fakeData() List~FeedbackDTO~
    }
    
    %% Repository Layer
    class FeedbackRepository {
        <<interface>>
        +findByProductProductId(productId, pageable) Page~Feedback~
        +findByProductProductIdAndRating(productId, rating, pageable) Page~Feedback~
        +findByCustomerCustomerIdAndProductProductId(customerId, productId) Optional~Feedback~
        +findByProductProductId(productId) List~Feedback~
        +findByCustomerCustomerId(customerId) List~Feedback~
        +save(feedback) Feedback
        +deleteById(feedbackId) void
    }
    
    %% Entity Classes
    class Feedback {
        -int id
        -Customer customer
        -Product product
        -Byte rating
        -String comment
        -String status
        -LocalDateTime createdAt
        +getId() int
        +setId(id) void
        +getCustomer() Customer
        +setCustomer(customer) void
        +getProduct() Product
        +setProduct(product) void
        +getRating() Byte
        +setRating(rating) void
        +getComment() String
        +setComment(comment) void
        +getStatus() String
        +setStatus(status) void
        +getCreatedAt() LocalDateTime
        +setCreatedAt(createdAt) void
    }
    
    class Customer {
        -Integer customerId
        -String name
        -String email
        -String phone
        -String address
        -String gender
        -LocalDate birthDate
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -boolean isLocked
        -String avatarUrl
        +getCustomerId() Integer
        +setCustomerId(customerId) void
        +getName() String
        +setName(name) void
        +getEmail() String
        +setEmail(email) void
        +getPhone() String
        +setPhone(phone) void
        +getAddress() String
        +setAddress(address) void
        +getGender() String
        +setGender(gender) void
        +getBirthDate() LocalDate
        +setBirthDate(birthDate) void
        +getCreatedAt() LocalDateTime
        +setCreatedAt(createdAt) void
        +getUpdatedAt() LocalDateTime
        +setUpdatedAt(updatedAt) void
        +isLocked() boolean
        +setLocked(locked) void
        +getAvatarUrl() String
        +setAvatarUrl(avatarUrl) void
    }
    
    class Product {
        -Integer productId
        -String name
        -String description
        -BigDecimal price
        -Integer stockQty
        -boolean status
        -String imageUrl
        -Category category
        -Brand brand
        +getProductId() Integer
        +setProductId(productId) void
        +getName() String
        +setName(name) void
        +getDescription() String
        +setDescription(description) void
        +getPrice() BigDecimal
        +setPrice(price) void
        +getStockQty() Integer
        +setStockQty(stockQty) void
        +isStatus() boolean
        +setStatus(status) void
        +getImageUrl() String
        +setImageUrl(imageUrl) void
        +getCategory() Category
        +setCategory(category) void
        +getBrand() Brand
        +setBrand(brand) void
    }
    
    %% DTO Classes
    class FeedbackDTO {
        -int id
        -Customer customer
        -String product
        -Byte rating
        -String comment
        -String status
        -LocalDateTime createdAt
        +getId() int
        +setId(id) void
        +getCustomer() Customer
        +setCustomer(customer) void
        +getProduct() String
        +setProduct(product) void
        +getRating() Byte
        +setRating(rating) void
        +getComment() String
        +setComment(comment) void
        +getStatus() String
        +setStatus(status) void
        +getCreatedAt() LocalDateTime
        +setCreatedAt(createdAt) void
    }
    
    %% Spring Framework Classes
    class Pageable {
        <<Spring Framework>>
        +getPageNumber() int
        +getPageSize() int
        +getSort() Sort
    }
    
    class Page {
        <<Spring Framework>>
        +getContent() List~T~
        +getTotalElements() long
        +getTotalPages() int
        +getNumber() int
        +map(converter) Page~R~
    }
    
    class Sort {
        <<Spring Framework>>
        +by(field) Sort
        +descending() Sort
    }
    
    %% Database
    class Database {
        <<Database>>
    }
    
    %% Relationships
    Customer ||--o{ Feedback : "writes"
    Product ||--o{ Feedback : "receives"
    CustomerProductController --> FeedbackService : "uses"
    FeedbackService <|.. FeedbackServiceImpl : "implements"
    FeedbackServiceImpl --> FeedbackRepository : "uses"
    FeedbackServiceImpl --> ProductRepository : "uses"
    FeedbackServiceImpl --> CustomerRepository : "uses"
    FeedbackRepository --> Feedback : "manages"
    FeedbackRepository --> Database : "persists to"
    FeedbackServiceImpl --> FeedbackDTO : "converts to"
    Feedback --> Customer : "belongs to"
    Feedback --> Product : "belongs to"
    FrontendUI --> CustomerProductController : "calls API"
    CustomerProductController --> Pageable : "creates"
    CustomerProductController --> Page : "returns"
    FeedbackServiceImpl --> Page : "returns"
    FeedbackRepository --> Page : "returns"
```

## Class Descriptions

### Controller Layer
- **CustomerProductController**: Handles HTTP requests for product-related operations, including feedback filtering with rating and pagination.

### Service Layer
- **FeedbackService**: Interface defining feedback business operations
- **FeedbackServiceImpl**: Implementation of feedback service with business logic for filtering, CRUD operations, and DTO conversion

### Repository Layer
- **FeedbackRepository**: Data access interface extending JpaRepository for feedback persistence operations

### Entity Classes
- **Feedback**: JPA entity representing feedback data with relationships to Customer and Product
- **Customer**: JPA entity representing customer information
- **Product**: JPA entity representing product information

### DTO Classes
- **FeedbackDTO**: Data Transfer Object for feedback data, used for API responses

### Framework Classes
- **Pageable**: Spring Data pagination interface
- **Page**: Spring Data pagination result wrapper
- **Sort**: Spring Data sorting interface

## Key Features Represented

1. **Rating Filtering**: System can filter feedbacks by rating (1-5 stars)
2. **Pagination**: Supports paginated results with configurable page size
3. **Sorting**: Results are sorted by creation date (newest first)
4. **DTO Pattern**: Uses DTOs for API responses to decouple internal entities from external API
5. **Repository Pattern**: Clean separation between business logic and data access
6. **Service Layer**: Business logic encapsulation and transaction management 