-- ============================================================================
-- EventCraft: Task and Schedule Management Module (Database Schema)
-- Module Lead: IT25104083 - Senanayake Y.I.D.P (Member 5)
-- Database: Microsoft SQL Server (T-SQL)
-- ============================================================================

USE master;
GO

IF DB_ID('EventCraftDB') IS NOT NULL
BEGIN
    ALTER DATABASE EventCraftDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE EventCraftDB;
END
GO

CREATE DATABASE EventCraftDB;
GO

USE EventCraftDB;
GO

-- Minimal Reference Stub Tables (External dependencies)
CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(150) NOT NULL UNIQUE,
    user_role NVARCHAR(50) NOT NULL CHECK (user_role IN ('Host', 'Collaborator', 'Guest', 'Admin'))
);
GO

CREATE TABLE Events (
    event_id INT IDENTITY(1,1) PRIMARY KEY,
    event_title NVARCHAR(150) NOT NULL,
    event_date DATETIME2 NOT NULL,
    host_id INT NOT NULL,
    CONSTRAINT FK_Events_Host FOREIGN KEY (host_id) REFERENCES Users(user_id)
);
GO

-- Module Table 1: Tasks
CREATE TABLE Tasks (
    task_id INT IDENTITY(1,1) PRIMARY KEY,
    event_id INT NOT NULL,
    title NVARCHAR(150) NOT NULL,
    description NVARCHAR(MAX) NULL,
    deadline DATETIME2 NOT NULL,
    priority NVARCHAR(20) NOT NULL DEFAULT 'Medium',
    status NVARCHAR(20) NOT NULL DEFAULT 'Pending',
    progress INT NOT NULL DEFAULT 0,
    assigned_to INT NULL,
    created_by INT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_Tasks_Event FOREIGN KEY (event_id) REFERENCES Events(event_id) ON DELETE CASCADE,
    CONSTRAINT FK_Tasks_Assignee FOREIGN KEY (assigned_to) REFERENCES Users(user_id) ON DELETE SET NULL,
    CONSTRAINT FK_Tasks_Creator FOREIGN KEY (created_by) REFERENCES Users(user_id),

    CONSTRAINT CK_Tasks_Priority CHECK (priority IN ('Low', 'Medium', 'High')),
    CONSTRAINT CK_Tasks_Status CHECK (status IN ('Pending', 'In Progress', 'Completed')),
    CONSTRAINT CK_Tasks_Progress CHECK (progress >= 0 AND progress <= 100)
);
GO

-- Module Table 2: Schedules (Timeline & Agenda)
CREATE TABLE Schedules (
    schedule_id INT IDENTITY(1,1) PRIMARY KEY,
    event_id INT NOT NULL,
    activity_name NVARCHAR(150) NOT NULL,
    description NVARCHAR(MAX) NULL,
    start_time DATETIME2 NOT NULL,
    end_time DATETIME2 NOT NULL,
    activity_type NVARCHAR(50) NOT NULL DEFAULT 'Activity',
    location_or_stage NVARCHAR(100) NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_Schedules_Event FOREIGN KEY (event_id) REFERENCES Events(event_id) ON DELETE CASCADE,
    CONSTRAINT CK_Schedules_Type CHECK (activity_type IN ('Milestone', 'Activity', 'Session', 'Keynote', 'Break')),
    CONSTRAINT CK_Schedules_TimeOrder CHECK (end_time > start_time)
);
GO

-- Sample Data for Testing
INSERT INTO Users (full_name, email, user_role) VALUES
('Sarah Jenkins', 'sarah.j@eventcraft.com', 'Host'),
('Alex Rivera', 'alex.r@eventcraft.com', 'Collaborator'),
('Priya Patel', 'priya.p@eventcraft.com', 'Collaborator');

INSERT INTO Events (event_title, event_date, host_id) VALUES
('Tech Innovators Summit 2026', '2026-11-20 09:00:00', 1);

INSERT INTO Tasks (event_id, title, description, deadline, priority, status, progress, assigned_to, created_by) VALUES
(1, 'Confirm Catering & Dietary Menus', 'Verify vegan and gluten-free meal counts with the vendor.', '2026-10-15 17:00:00', 'High', 'Completed', 100, 2, 1),
(1, 'Design & Print Delegate Badges', 'Print RFID delegate badges with sponsor branding.', '2026-11-01 12:00:00', 'Medium', 'In Progress', 60, 3, 1),
(1, 'Stage & AV Equipment Soundcheck', 'Conduct full frequency and microphone test on the main stage.', '2026-11-19 18:00:00', 'High', 'Pending', 0, 1, 1),
(1, 'Prepare Welcome Gift Bags', 'Assemble sponsor swag and print event agendas for gift bags.', '2026-11-10 16:00:00', 'Low', 'Pending', 0, 2, 1);

INSERT INTO Schedules (event_id, activity_name, description, start_time, end_time, activity_type, location_or_stage) VALUES
(1, 'Registration & Welcome Coffee', 'Guests check in, collect badges and breakfast.', '2026-11-20 08:30:00', '2026-11-20 09:30:00', 'Activity', 'Foyer & Reception'),
(1, 'Opening Keynote: Future of Cloud & AI', 'Delivered by Guest of Honour Dr. Aris Thorne.', '2026-11-20 09:30:00', '2026-11-20 10:45:00', 'Keynote', 'Main Auditorium'),
(1, 'Morning Networking Break', 'Refreshments and sponsor booth visits.', '2026-11-20 10:45:00', '2026-11-20 11:15:00', 'Break', 'Exhibition Hall'),
(1, 'Panel Discussion: Modern Engineering Practices', 'Interactive panel session with Q&A.', '2026-11-20 11:15:00', '2026-11-20 12:30:00', 'Session', 'Main Auditorium'),
(1, 'VIP Networking Lunch & Milestone Review', 'Midday milestone celebration and lunch.', '2026-11-20 12:30:00', '2026-11-20 13:45:00', 'Milestone', 'Banquet Hall');
GO
