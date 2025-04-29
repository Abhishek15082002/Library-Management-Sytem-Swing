-- Drop old tables if they exist (optional, use with caution)
-- Disable checks temporarily if needed for dropping in specific order
-- SET FOREIGN_KEY_CHECKS=0; -- Use with caution

DROP TABLE IF EXISTS ActivityLog;
DROP TABLE IF EXISTS BookRequests;
DROP TABLE IF EXISTS Notifications;
DROP TABLE IF EXISTS Fines; -- Drop Fines before IssuedBooks if foreign key exists
DROP TABLE IF EXISTS IssuedBooks;
DROP TABLE IF EXISTS RatingsReviews; -- Drop RatingsReviews before Books/Students
DROP TABLE IF EXISTS Books;
DROP TABLE IF EXISTS Students; -- Drop Students before Users if foreign key exists
DROP TABLE IF EXISTS Librarians; -- Drop Librarians before Users if foreign key exists
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Settings;

-- SET FOREIGN_KEY_CHECKS=1; -- Re-enable checks

-- Users Table: Stores login credentials and role for all user types
CREATE TABLE Users (
    username VARCHAR(50) PRIMARY KEY,          -- Unique username for login
    password VARCHAR(255) NOT NULL,             -- Hashed password (Store hashes here!)
    role ENUM('Admin', 'Librarian', 'Student') NOT NULL,
    status ENUM('Active', 'Inactive') DEFAULT 'Active', -- For account management
    email VARCHAR(100) UNIQUE,
    security_question VARCHAR(255),             -- Optional: For password recovery
    security_answer VARCHAR(255)                -- Optional: For password recovery
);

-- Librarians Table: Stores specific librarian details
CREATE TABLE Librarians (
    librarian_id VARCHAR(10) PRIMARY KEY,       -- Format: L001, L002, ...
    username VARCHAR(50) UNIQUE NOT NULL,       -- Links to Users table for login
    name VARCHAR(100),
    FOREIGN KEY (username) REFERENCES Users(username) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Students Table: Stores specific student details
CREATE TABLE Students (
    student_id VARCHAR(10) PRIMARY KEY,         -- Format: S001, S002, ...
    username VARCHAR(50) UNIQUE NOT NULL,       -- Links to Users table for login
    name VARCHAR(100),
    FOREIGN KEY (username) REFERENCES Users(username) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Books Table: Stores book information (Corrected Check Constraint)
CREATE TABLE Books (
    book_id VARCHAR(10) PRIMARY KEY,            -- Format: B001, B002, ...
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    category VARCHAR(100),                      -- For search & filter
    total_copies INT DEFAULT 1 CHECK (total_copies >= 0),
    available_copies INT DEFAULT 1 CHECK (available_copies >= 0), -- Corrected CHECK constraint
    avg_rating DECIMAL(3,2) DEFAULT 0.00        -- Average rating
);

-- Ratings and Reviews Table
CREATE TABLE RatingsReviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    book_id VARCHAR(10),
    student_id VARCHAR(10),
    rating INT CHECK (rating >= 1 AND rating <= 5), -- e.g., 1 to 5 stars
    review TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES Books(book_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    UNIQUE KEY unique_review (book_id, student_id) -- Allow only one review per student per book
);

-- Issued Books Table: Tracks books currently borrowed or previously borrowed
CREATE TABLE IssuedBooks (
    issue_id INT PRIMARY KEY AUTO_INCREMENT,    -- Changed to INT AUTO_INCREMENT
    student_id VARCHAR(10),
    book_id VARCHAR(10),
    issue_date DATE,
    due_date DATE,
    return_date DATE,                           -- Date the book was actually returned
    status ENUM('Issued', 'Returned', 'Overdue', 'HoldRequested', 'HoldActive') DEFAULT 'Issued', -- Added Hold status
    reissue_count INT DEFAULT 0,                -- Track reissues
    hold_request_date DATE,                     -- Date hold was requested
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES Books(book_id) ON DELETE CASCADE
);

-- Fines Table: Tracks fines owed by students
CREATE TABLE Fines (
    fine_id INT PRIMARY KEY AUTO_INCREMENT,      -- Changed to INT AUTO_INCREMENT for simplicity
    student_id VARCHAR(10),
    issue_id INT,                               -- Link to the specific book issue
    fine_amount DECIMAL(10, 2) DEFAULT 0.00,
    status ENUM('Paid', 'Unpaid') DEFAULT 'Unpaid',
    fine_date DATE,                             -- Date the fine was generated/updated
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE SET NULL, -- Keep fine record even if student deleted? Or CASCADE? Decide policy.
    FOREIGN KEY (issue_id) REFERENCES IssuedBooks(issue_id) ON DELETE SET NULL -- Keep fine record even if issue deleted?
);


-- Notifications Table: For reminders and alerts
CREATE TABLE Notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT, -- Changed to INT AUTO_INCREMENT
    user_id VARCHAR(50),                         -- Links to Users(username)
    message TEXT,
    type ENUM('DueDate', 'FineAlert', 'Approval', 'NewBook', 'HoldReady', 'General') DEFAULT 'General',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(username) ON DELETE CASCADE -- Link to username for flexibility
);

-- Book Requests Table: For student requests of new books
CREATE TABLE BookRequests (
    request_id INT PRIMARY KEY AUTO_INCREMENT,   -- Changed to INT AUTO_INCREMENT
    student_id VARCHAR(10),
    title VARCHAR(255),
    author VARCHAR(255),
    reason TEXT,
    status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending',
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    librarian_notes TEXT,                       -- Optional notes from librarian reviewing request
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE
);

-- Activity Log Table: Tracks user actions (optional but good practice)
CREATE TABLE ActivityLog (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50),                       -- User performing the action
    activity_type VARCHAR(100),                 -- e.g., 'LOGIN', 'ADD_BOOK', 'ISSUE_BOOK'
    details TEXT,                               -- Optional details about the activity
    activity_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (username) REFERENCES Users(username) ON DELETE SET NULL -- Keep log even if user deleted
);

-- Settings Table: For application-level settings
CREATE TABLE Settings (
    setting_key VARCHAR(50) PRIMARY KEY,        -- e.g., 'DarkMode', 'FinePerDay'
    setting_value VARCHAR(255)
);
