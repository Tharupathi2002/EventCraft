-- EventCraft Database Schema for SQL Server Management Studio (SSMS 22)
-- Vendor & Venue Management Module

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'EventCraftDB')
BEGIN
    CREATE DATABASE EventCraftDB;
END;
GO

USE EventCraftDB;
GO

-- 1. Users Table (Hosts, Vendors, Venue Providers, Admins)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'users')
BEGIN
    CREATE TABLE users (
        user_id INT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(100) NOT NULL,
        email NVARCHAR(100) NOT UNIQUE NOT NULL,
        password NVARCHAR(255) NOT NULL,
        role NVARCHAR(50) NOT NULL, -- 'HOST', 'VENDOR', 'VENUE_PROVIDER', 'ADMIN'
        account_status NVARCHAR(50) DEFAULT 'ACTIVE',
        last_login DATETIME NULL
    );
END;

-- 2. Venues Table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'venues')
BEGIN
    CREATE TABLE venues (
        venue_id INT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(150) NOT NULL,
        street NVARCHAR(150) NOT NULL,
        city NVARCHAR(100) NOT NULL,
        capacity INT NOT NULL,
        rate_per_day DECIMAL(10,2) NOT NULL,
        description NVARCHAR(MAX),
        image_url NVARCHAR(255),
        rating DECIMAL(3,2) DEFAULT 4.5,
        status NVARCHAR(50) DEFAULT 'AVAILABLE', -- 'AVAILABLE', 'BOOKED', 'MAINTENANCE'
        provider_id INT NULL
    );
END;

-- 3. Vendors Table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'vendors')
BEGIN
    CREATE TABLE vendors (
        vendor_id INT IDENTITY(1,1) PRIMARY KEY,
        business_name NVARCHAR(150) NOT NULL,
        service_type NVARCHAR(100) NOT NULL, -- Catering, Photography, Decor, Music/DJ, Event Planner
        contact_info NVARCHAR(150) NOT NULL,
        base_price DECIMAL(10,2) NOT NULL,
        description NVARCHAR(MAX),
        image_url NVARCHAR(255),
        rating DECIMAL(3,2) DEFAULT 4.8,
        status NVARCHAR(50) DEFAULT 'AVAILABLE',
        user_id INT NULL
    );
END;

-- 4. Bookings Table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'bookings')
BEGIN
    CREATE TABLE bookings (
        booking_id INT IDENTITY(1,1) PRIMARY KEY,
        event_id INT DEFAULT 1,
        host_name NVARCHAR(100) NOT NULL,
        host_email NVARCHAR(100) NOT NULL,
        venue_id INT NULL,
        vendor_id INT NULL,
        booking_type NVARCHAR(50) NOT NULL, -- 'VENUE', 'VENDOR'
        booking_date DATE NOT NULL,
        event_date DATE NOT NULL,
        amount DECIMAL(10,2) NOT NULL,
        booking_status NVARCHAR(50) NOT NULL, -- 'PENDING', 'CONFIRMED', 'CANCELLED'
        special_requirements NVARCHAR(MAX)
    );
END;
