# 📚 ScholarMS – Student Management System

A Java Swing desktop application for managing students, marks, and academic reports, with a MySQL database backend.

---

## 🖥️ Features

- 🔐 Login system
- 👤 Register, update, and delete students
- 📝 Marks entry by class and term
- 📊 Academic reports with ranking
- 🔍 Search students by ID, name, or class

---

## ⚙️ Requirements

| Tool | Version |
|------|---------|
| JDK  | 17 or higher |
| MySQL | 8.0 or higher |
| XAMPP / WAMP | Any recent version |
| mysql-connector-j | 8.x (included in `/lib`) |

---

## 🚀 Installation & Setup

### Step 1 — Install Java JDK
1. Download JDK 17+ from https://www.oracle.com/java/technologies/downloads/
2. Install and set `JAVA_HOME` in your environment variables

### Step 2 — Install XAMPP
1. Download from https://www.apachefriends.org/
2. Install and start **Apache** and **MySQL** from the XAMPP Control Panel

### Step 3 — Set up the Database
1. Open your browser → go to `http://localhost/phpmyadmin`
2. Click **"New"** → create a database named `student_ms`
3. Select `student_ms` → click the **"Import"** tab
4. Click **"Choose File"** → select `database.sql` from this project
5. Click **"Execute"** — tables and sample data will be created automatically

### Step 4 — Configure Database Connection (if needed)
Open `src/StudentManagementSystem2.java` and check these lines:

```java
static final String DB_URL      = "jdbc:mysql://localhost:3306/student_ms";
static final String DB_USER     = "root";
static final String DB_PASSWORD = "";
```

Change `DB_PASSWORD` if your MySQL has a password set.

### Step 5 — Compile and Run

**Option A – Using an IDE (IntelliJ / Eclipse / NetBeans)**
1. Open the project folder in your IDE
2. Add `lib/mysql-connector-j.jar` to the project classpath
3. Run `StudentManagementSystem2.java`

**Option B – Using Command Line**
```bash
# Compile
javac -cp "lib/mysql-connector-j.jar" src/StudentManagementSystem2.java -d out/

# Run
java -cp "out;lib/mysql-connector-j.jar" StudentManagementSystem2
```
> On Mac/Linux, replace `;` with `:` in the classpath

---

## 🔑 Default Login

| Username | Password  |
|----------|-----------|
| admin    | admin123  |

---

## 📁 Project Structure

```
StudentManagementSystem/
├── src/
│   └── StudentManagementSystem2.java
├── lib/
│   └── mysql-connector-j.jar
├── database.sql
└── README.md
```

---

## 👨‍💻 Built With

- Java Swing (GUI)
- MySQL (Database)
- JDBC (Database connectivity)
- DAO Pattern (Data Access Object)

---

## 📌 Notes

- Sample student data is included in `database.sql`
- The system auto-generates Student IDs in format `STD-2025-XXX`
- Marks are saved per subject, term, and year
