-- DDL Script for Revised ChillStore Database Schema with Vouchers
CREATE DATABASE [group4_swp_chillStore_Extended2.1]
GO
USE [group4_swp_chillStore_Extended2.1]
GO

-- Users Tables (Separated by Role)
CREATE TABLE dbo.customers (
    customer_id INT IDENTITY PRIMARY KEY,
    name VARCHAR(100),
    display_name VARCHAR(100),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(255),
    birth_date DATE,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 DEFAULT SYSUTCDATETIME()
);

CREATE TABLE dbo.staff (
    staff_id INT IDENTITY PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(255),
    gender VARCHAR(10),
    national_id VARCHAR(20) UNIQUE,
    start_date DATE,
    contract_type VARCHAR(20) CHECK (contract_type IN ('thử việc', 'chính thức')),
    base_salary DECIMAL(10,2),
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 DEFAULT SYSUTCDATETIME()
);

CREATE TABLE dbo.admins (
    admin_id INT IDENTITY PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(255),
    gender VARCHAR(10),
    national_id VARCHAR(20) UNIQUE,
    start_date DATE,
    privileges VARCHAR(100),
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 DEFAULT SYSUTCDATETIME()
);

-- Vouchers
CREATE TABLE dbo.vouchers (
    voucher_id INT IDENTITY PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    discount_amount DECIMAL(10,2),
    discount_pct DECIMAL(5,2),
    min_order_amount DECIMAL(10,2) DEFAULT 0,
    quantity_available INT DEFAULT 0,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BIT DEFAULT 1,
    created_by INT NOT NULL,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    FOREIGN KEY (created_by) REFERENCES dbo.admins(admin_id)
);

-- Other Tables Adjusted as Requested
CREATE TABLE dbo.categories (
    category_id INT IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id INT NULL,
    FOREIGN KEY (parent_id) REFERENCES dbo.categories(category_id)
);

CREATE TABLE dbo.products (
    product_id INT IDENTITY PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    stock_qty INT DEFAULT 0,
    active BIT DEFAULT 1,
    category_id INT,
    discount_pct DECIMAL(5,2) DEFAULT 0,
    discount_from DATE,
    discount_to DATE,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    FOREIGN KEY (category_id) REFERENCES dbo.categories(category_id)
);

CREATE TABLE dbo.promotions (
    promo_id INT IDENTITY PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    discount_pct DECIMAL(5,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BIT DEFAULT 1,
    created_by INT NOT NULL,
    FOREIGN KEY (created_by) REFERENCES dbo.admins(admin_id)
);

CREATE TABLE dbo.promotion_products (
    promo_id INT NOT NULL,
    product_id INT NOT NULL,
    PRIMARY KEY (promo_id, product_id),
    FOREIGN KEY (promo_id) REFERENCES dbo.promotions(promo_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES dbo.products(product_id)
);

-- User Carts and Items
CREATE TABLE dbo.user_carts (
    cart_id INT IDENTITY PRIMARY KEY,
    customer_id INT UNIQUE NOT NULL,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    FOREIGN KEY (customer_id) REFERENCES dbo.customers(customer_id) ON DELETE CASCADE
);

CREATE TABLE dbo.cart_items (
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT DEFAULT 1,
    PRIMARY KEY (cart_id, product_id),
    FOREIGN KEY (cart_id) REFERENCES dbo.user_carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES dbo.products(product_id)
);

-- Orders and Items
CREATE TABLE dbo.orders (
    order_id INT IDENTITY PRIMARY KEY,
    customer_id INT NOT NULL,
    promo_id INT NULL,
    voucher_id INT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    order_date DATETIME2 DEFAULT SYSUTCDATETIME(),
    total_amount DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'Pending' CHECK (status IN ('Pending', 'Paid', 'Cancelled', 'Shipped', 'Delivered')),
    payment_method VARCHAR(10),
    FOREIGN KEY (customer_id) REFERENCES dbo.customers(customer_id),
    FOREIGN KEY (promo_id) REFERENCES dbo.promotions(promo_id),
    FOREIGN KEY (voucher_id) REFERENCES dbo.vouchers(voucher_id)
);

CREATE TABLE dbo.order_items (
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price_each DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES dbo.products(product_id)
);

-- Order History
CREATE TABLE dbo.order_history (
    history_id INT IDENTITY PRIMARY KEY,
    order_id INT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('Pending', 'Paid', 'Cancelled', 'Shipped', 'Delivered')),
    updated_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    updated_by INT NULL,
    updated_role VARCHAR(10) CHECK (updated_role IN ('staff', 'admin', 'system')),
    notes VARCHAR(500),
    FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id)
);

-- Maintenance and Warranty
CREATE TABLE dbo.maintenance_warranty_requests (
    request_id INT IDENTITY PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    customer_id INT NOT NULL,
    request_type VARCHAR(20) CHECK (request_type IN ('maintenance', 'warranty')),
    request_date DATETIME2 DEFAULT SYSUTCDATETIME(),
    reason VARCHAR(500),
    status VARCHAR(20) DEFAULT 'Pending',
    FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id),
    FOREIGN KEY (product_id) REFERENCES dbo.products(product_id),
    FOREIGN KEY (customer_id) REFERENCES dbo.customers(customer_id)
);

CREATE TABLE dbo.maintenance_warranty_history (
    history_id INT IDENTITY PRIMARY KEY,
    request_id INT NOT NULL,
    update_time DATETIME2 DEFAULT SYSUTCDATETIME(),
    notes VARCHAR(1000),
    status VARCHAR(20),
    handled_by INT,
    handled_role VARCHAR(10) CHECK (handled_role IN ('staff', 'admin')),
    FOREIGN KEY (request_id) REFERENCES dbo.maintenance_warranty_requests(request_id)
);

-- Delivery Tracking
CREATE TABLE dbo.delivery_tracking (
    tracking_id INT IDENTITY PRIMARY KEY,
    order_id INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    updated_by INT NOT NULL,
    updated_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    notes VARCHAR(500),
    FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id),
    FOREIGN KEY (updated_by) REFERENCES dbo.staff(staff_id)
);

-- Feedback and Replies
CREATE TABLE dbo.feedback (
    feedback_id INT IDENTITY PRIMARY KEY,
    customer_id INT NOT NULL,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    rating TINYINT,
    comment VARCHAR(1000),
    status VARCHAR(20) DEFAULT 'Pending',
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    FOREIGN KEY (customer_id) REFERENCES dbo.customers(customer_id),
    FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id),
    FOREIGN KEY (order_id, product_id) REFERENCES dbo.order_items(order_id, product_id)
);

CREATE TABLE dbo.replies (
    reply_id INT IDENTITY PRIMARY KEY,
    feedback_id INT NOT NULL,
    staff_id INT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at DATETIME2 DEFAULT SYSUTCDATETIME(),
    FOREIGN KEY (feedback_id) REFERENCES dbo.feedback(feedback_id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES dbo.staff(staff_id)
);

-- Import Invoices
CREATE TABLE dbo.import_invoices (
    invoice_id INT IDENTITY PRIMARY KEY,
    admin_id INT NOT NULL,
    import_date DATETIME2 DEFAULT SYSUTCDATETIME(),
    notes VARCHAR(255),
    FOREIGN KEY (admin_id) REFERENCES dbo.admins(admin_id)
);

CREATE TABLE dbo.import_invoice_items (
    invoice_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    cost_each DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (invoice_id, product_id),
    FOREIGN KEY (invoice_id) REFERENCES dbo.import_invoices(invoice_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES dbo.products(product_id)
);

-- Stock Movements
CREATE TABLE dbo.stock_movements (
    mov_id INT IDENTITY PRIMARY KEY,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    type VARCHAR(10) NOT NULL,
    mov_date DATETIME2 DEFAULT SYSUTCDATETIME(),
    FOREIGN KEY (product_id) REFERENCES dbo.products(product_id)
);

-- Membership and Tiers
CREATE TABLE dbo.membership_tiers (
    tier_id INT IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    min_points INT NOT NULL,
    discount_pct DECIMAL(5,2) NOT NULL
);

CREATE TABLE dbo.memberships (
    membership_id INT IDENTITY PRIMARY KEY,
    customer_id INT NOT NULL,
    tier_id INT NOT NULL,
    points INT DEFAULT 0,
    start_date DATE DEFAULT CAST(SYSUTCDATETIME() AS DATE),
    end_date DATE NULL,
    is_current BIT DEFAULT 1,
    FOREIGN KEY (customer_id) REFERENCES dbo.customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (tier_id) REFERENCES dbo.membership_tiers(tier_id)
);
