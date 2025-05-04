# Smart Library Management System (Java Swing)
# Capstone Project | Object Oriented Programming Lab CSEG2020
## Introduction

This project is a desktop application developed in Java using the Swing GUI toolkit to create a comprehensive Library Management System. It allows different types of users (Administrators, Librarians, Students) to interact with the library's resources efficiently. The system facilitates book management, borrowing and returning processes, fine tracking, user management, and report generation.

## Features

The system provides role-based access control with distinct functionalities for each user type:

**Admin:**
* Manage Librarian accounts (Add/View/Delete).
* Manage User accounts (Activate/Deactivate status).
* View Fine Management dashboard (View unpaid fines, Waive fines).
* View System Reports (Available Books, Borrowed Books, Fines by Student, Monthly Fine Summary).
* Logout functionality.

**Librarian (Partially Implemented/Planned):**
* Manage Books (Add/View/Delete).
* Issue books to students.
* View all issued books.
* Process book returns and calculate fines.
* Manage student records.
* Approve book requests from students.
* View overdue books and potentially notify students.
* Logout functionality.

**Student:**
* Search and view available books (by Title, Author, Category).
* Borrow available books.
* View currently borrowed books, including due dates and status.
* Return borrowed books.
* Request reissues for borrowed books (subject to limits and fines).
* View and manage (pay/waive - placeholder) fines associated with borrowed books.
* Request new books not currently in the library.
* View system notifications (Due dates, Fines, Approvals - placeholder).
* Logout functionality.

**Additional Planned Features (from requirements):**
* QR Code Scanning
* Email Notifications
* Interactive Dashboards
* Book Ratings & Reviews
* Dark Mode
* Database Backup & Restore

## Technologies Used

* **Language:** Java (JDK 8 or higher recommended)
* **GUI:** Java Swing
* **Database:** MySQL (or any relational database compatible with JDBC)
* **Database Connectivity:** JDBC (MySQL Connector/J)
* **IDE (Development):** VS Code (with Java Extension Pack)

## Project Structure
```tree
Library-Management-System-Swing/
├── src/
│   └── library/
│       ├── LoginPage.java             # Main application entry point
│       ├── DatabaseConnection.java    # DB connection utility
│       ├── UserSession.java           # Manages logged-in user session
│       ├── backend/                   # Business logic & DB interaction services
│       │   ├── AdminService.java
│       │   ├── StudentService.java
│       │   └── LibrarianService.java
│       └── frontend/                  # GUI classes (Frames, Dashboards)
│           ├── LoginFrame.java
│           ├── AdminDashboard.java
│           ├── LibrarianDashboard.java
│           └── StudentDashboard.java
├── lib/                               # External libraries (e.g., JDBC driver)
│   └── mysql-connector-j-x.x.x.jar
├── sql/                               # Database setup scripts
│   ├── schemas.sql
│   └── sample_data.sql
├── bin/                               # Compiled .class files (IGNORED BY GIT)
├── run.bat                            # Batch script to compile and run (Windows)
├── .gitignore                         # Specifies intentionally untracked files
└── README.md
```
## Setup Instructions

1.  **Database Setup:**
    * Ensure you have a MySQL server (or compatible database) installed and running.
    * Create a database named `library_db` (or update the name in `src/library/DatabaseConnection.java`).
    * Execute the `sql/schemas.sql` script using a database tool (like MySQL Workbench, DBeaver, etc.) to create the necessary tables.
    * Optionally, execute `sql/sample_data.sql` to populate the database with sample users, books, and transactions for testing.
2.  **Configure Database Connection:**
    * Open `src/library/DatabaseConnection.java`.
    * Update the `DB_URL`, `DB_USER`, and `DB_PASSWORD` constants with your actual database credentials.
3.  **JDBC Driver:**
    * Download the MySQL Connector/J JDBC driver JAR file (e.g., `mysql-connector-j-x.x.x.jar`).
    * Place the downloaded JAR file into the `lib/` directory in the project root.
    * **Important:** Ensure the filename in the `run_library.bat` script (`JDBC_JAR_PATH` variable) exactly matches the name of the JAR file you placed in the `lib` folder.
4.  **JDK:** Make sure you have a compatible Java Development Kit (JDK 8+) installed and configured in your system's PATH.

## Running the Application

**Using the Batch Script (Windows):**

1.  Navigate to the project's root directory (`Library-Management-Sytem-Swing`).
2.  Ensure the `JDBC_JAR_PATH` variable inside `run_library.bat` points correctly to your JDBC driver in the `lib` folder (relative path `lib\your-driver-name.jar` is usually best).
3.  Double-click `run_library.bat` or run it from the command line within the project directory.
4.  The script will compile the source code into the `bin/` directory and then launch the application.

**Default Login Credentials (from `sample_data.sql`):**

* **Admin:** `admin` / `admin`
* **Librarian:** `lib1` / `librarian1`
* **Student:** `stu1` / `student1`
* **Student:** `stu2` / `student2`

*(Remember: Passwords in `sample_data.sql` are plain text for now, futrue scope: hashing)*


## Contributors

| Name              | Role                 |
|-------------------|----------------------|
| Abhishek Yadav    | Frontend & Design    |
| Anoushka Pandey   | Frontend & Design    |
| Aantriksh Sood    | Backend & Database   |
| Saksham Agarwal   | Backend & Database   |


