-- Clear existing data from tables (optional, run if you want a clean slate)
-- Run DELETE statements in reverse order of dependencies or disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS=0; -- Be careful with this in production!
DELETE FROM Settings;
DELETE FROM ActivityLog;
DELETE FROM BookRequests;
DELETE FROM Notifications;
DELETE FROM Fines;
DELETE FROM IssuedBooks;
DELETE FROM RatingsReviews;
DELETE FROM Books;
DELETE FROM Students;
DELETE FROM Librarians;
DELETE FROM Users;
SET FOREIGN_KEY_CHECKS=1; -- Re-enable checks

-- 1. Settings
INSERT INTO Settings (setting_key, setting_value) VALUES
('DefaultBorrowingPeriodDays', '14'),
('FinePerDay', '1.50'), -- Example: 1.50 currency units per day
('MaxReissuesAllowed', '2');

-- 2. Users (Passwords are plain text - REPLACE WITH HASHES!)
-- REMINDER: Generate hashes using PasswordUtils or similar and replace 'adminpass', etc.
INSERT INTO Users (username, password, role, status, email, security_question, security_answer) VALUES
('admin', 'admin', 'Admin', 'Active', 'admin@library.com', NULL, NULL), -- Use plain 'admin' for testing if skipping hashing
('saksham_lib', 'librarian1', 'Librarian', 'Active', 'saksham.lib@library.com', 'What is your favourite book?', 'Sapiens'),
('abhishek_stu', 'student1', 'Student', 'Active', 'abhishek.stu@school.com', 'What city were you born in?', 'Delhi'),
('anoushka_stu', 'student2', 'Student', 'Active', 'anoushka.stu@school.com', 'What is your pet''s name?', 'Max'),
('aantriksh_stu', 'student3', 'Student', 'Inactive', 'aantriksh.stu@school.com', NULL, NULL); -- Inactive student

-- 3. Librarians (Link to Users)
INSERT INTO Librarians (librarian_id, username, name) VALUES
('L001', 'saksham_lib', 'Saksham');

-- 4. Students (Link to Users)
INSERT INTO Students (student_id, username, name) VALUES
('S001', 'abhishek_stu', 'Abhishek'),
('S002', 'anoushka_stu', 'Anoushka'),
('S003', 'antriksh_stu', 'Antriksh'); -- Linked to inactive user

-- 5. Books (Using popular books)
INSERT INTO Books (book_id, title, author, category, total_copies, available_copies, avg_rating) VALUES
('B001', 'The Alchemist', 'Paulo Coelho', 'Fiction', 8, 6, 4.60),
('B002', 'Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', 'Non-Fiction', 5, 3, 4.80),
('B003', 'Atomic Habits', 'James Clear', 'Self-Help', 10, 10, 4.90),
('B004', '1984', 'George Orwell', 'Dystopian Fiction', 4, 0, 4.70), -- Currently unavailable
('B005', 'To Kill a Mockingbird', 'Harper Lee', 'Classic Fiction', 6, 6, 4.50),
('B006', 'The Psychology of Money', 'Morgan Housel', 'Finance', 7, 7, 4.75);

-- 6. IssuedBooks (Create scenarios for student S001 / abhishek_stu)
-- Assuming today is 2025-04-30 for calculating dates

-- Scenario 1: Book currently issued, not overdue (Issued 10 days ago)
-- Issue ID will be 1 (AUTO_INCREMENT starts at 1)
INSERT INTO IssuedBooks (student_id, book_id, issue_date, due_date, status, reissue_count) VALUES
('S001', 'B001', '2025-04-20', '2025-05-04', 'Issued', 0); -- 'The Alchemist', Due in 4 days (14 day period)

-- Scenario 2: Book overdue (Issued 20 days ago)
-- Issue ID will be 2
INSERT INTO IssuedBooks (student_id, book_id, issue_date, due_date, status, reissue_count) VALUES
('S001', 'B002', '2025-04-10', '2025-04-24', 'Overdue', 0); -- 'Sapiens', Was due 6 days ago

-- Scenario 3: Book returned (Issued 30 days ago, returned 10 days ago)
-- Issue ID will be 3
INSERT INTO IssuedBooks (student_id, book_id, issue_date, due_date, return_date, status, reissue_count) VALUES
('S001', 'B003', '2025-03-31', '2025-04-14', '2025-04-20', 'Returned', 0); -- 'Atomic Habits', Returned 6 days after due date

-- Scenario 4: Book issued, reached max reissues (Issued 40 days ago, reissued twice)
-- Issue ID will be 4
INSERT INTO IssuedBooks (student_id, book_id, issue_date, due_date, status, reissue_count) VALUES
('S001', 'B006', '2025-03-21', '2025-05-02', 'Issued', 2); -- 'The Psychology of Money', Reached max reissues (2)

-- Scenario 5: Book issued to another student (anoushka_stu)
-- Issue ID will be 5
INSERT INTO IssuedBooks (student_id, book_id, issue_date, due_date, status, reissue_count) VALUES
('S002', 'B005', '2025-04-25', '2025-05-09', 'Issued', 0); -- 'To Kill a Mockingbird'

-- 7. Fines (Link to overdue IssuedBooks record for S001 / abhishek_stu)
-- Fine for Issue ID 2 ('Sapiens', due 2025-04-24, today is 2025-04-30 -> 6 days overdue * 1.50/day = 9.00)
-- Fine ID will be 1
INSERT INTO Fines (student_id, issue_id, fine_amount, status, fine_date) VALUES
('S001', 2, 9.00, 'Unpaid', '2025-04-30'); -- Assuming Issue ID 2 corresponds to the B002 entry above

-- Fine for Issue ID 3 ('Atomic Habits', due 2025-04-14, returned 2025-04-20 -> 6 days overdue * 1.50/day = 9.00)
-- Fine ID will be 2
INSERT INTO Fines (student_id, issue_id, fine_amount, status, fine_date) VALUES
('S001', 3, 9.00, 'Unpaid', '2025-04-20');

-- 8. Notifications (For student S001 / abhishek_stu)
INSERT INTO Notifications (user_id, message, type, is_read, created_at) VALUES
('abhishek_stu', 'Book ''Sapiens: A Brief History of Humankind'' (B002) was due on 2025-04-24. Please return it soon.', 'DueDate', FALSE, '2025-04-25 09:00:00'),
('abhishek_stu', 'A fine of 9.00 has been applied for the overdue book ''Sapiens: A Brief History of Humankind'' (B002).', 'FineAlert', FALSE, '2025-04-30 10:00:00'),
('abhishek_stu', 'Welcome to the Smart Library System!', 'General', TRUE, '2025-04-01 12:00:00');

-- 9. Book Requests (From student S001 / abhishek_stu)
INSERT INTO BookRequests (student_id, title, author, reason, status, request_date) VALUES
('S001', 'Ikigai', 'Héctor García', 'Heard good things about this book.', 'Pending', '2025-04-28 11:00:00');

-- 10. Ratings and Reviews (From student S001 / abhishek_stu for a returned book)
INSERT INTO RatingsReviews (book_id, student_id, rating, review, review_date) VALUES
('B003', 'S001', 5, 'Excellent book on building good habits and breaking bad ones. Very practical advice.', '2025-04-21 14:00:00'); -- Review for 'Atomic Habits'
-- Note: Application logic should ideally update Books.avg_rating when a review is added/updated.