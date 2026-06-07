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
| mysql-connector-j | 9.7.0 (included in `/lib`) |

---

## 🚀 Easiest Way to Install (Recommended)

> Just download **`ScholarMS_Setup.exe`** — it installs like a real Windows software!

### Step 1 — Install Java JDK
1. Download JDK 17+ from https://www.oracle.com/java/technologies/downloads/
2. Install it on your PC

### Step 2 — Install XAMPP and set up the Database
1. Download XAMPP from https://www.apachefriends.org/
2. Start **Apache** and **MySQL** from XAMPP Control Panel
3. Open browser → go to `http://localhost/phpmyadmin`
4. Click **"New"** → create a database named `student_ms`
5. Select `student_ms` → click **"Import"** tab
6. Click **"Choose File"** → select `database.sql` from this project
7. Click **"Execute"**

### Step 3 — Install ScholarMS
1. Download **`ScholarMS_Setup.exe`** from this repository
2. Double-click it
3. Click **Next → Next → Install → Finish**
4. A shortcut appears on your Desktop 🎉
5. Double-click the shortcut — the app launches!

---

## ▶️ Run without Installing (Alternative)

1. Download **`ScholarMS.jar`** from this repository
2. Make sure Java is installed
3. Double-click `ScholarMS.jar` or run:
```bash
java -jar ScholarMS.jar
```

---

## 🔑 Default Login

| Username | Password  |
|----------|-----------|
| admin    | admin123  |

---

## 🛠️ For Developers (Run from Source)

### Configure Database Connection (if needed)
Open `src/StudentManagementSystem2.java` and check:

```java
static final String DB_URL      = "jdbc:mysql://localhost:3306/student_ms";
static final String DB_USER     = "root";
static final String DB_PASSWORD = "";
```

Change `DB_PASSWORD` if your MySQL has a password set.

### Compile and Run from Source (Windows)
```bash
# Compile
javac -cp "lib/mysql-connector-j-9.7.0.jar" src/StudentManagementSystem2.java -d out

# Run
java -cp "out;lib/mysql-connector-j-9.7.0.jar" StudentManagementSystem2
```

### Build JAR yourself
```bash
# Extract connector into out/
cd out
jar xf "C:\path\to\lib\mysql-connector-j-9.7.0.jar"
cd ..

# Create fat JAR
jar cfm ScholarMS.jar manifest.txt -C out .
```

---

## 📁 Project Structure

```
StudentManagementSystem/
├── src/
│   └── StudentManagementSystem2.java
├── lib/
│   └── mysql-connector-j-9.7.0.jar
├── out/                      ← compiled classes
├── ScholarMS.jar             ← executable JAR
├── ScholarMS.exe             ← Windows executable
├── ScholarMS_Setup.exe       ← Windows installer (recommended)
├── manifest.txt
├── setup_script.iss          ← Inno Setup script
├── database.sql              ← import this in phpMyAdmin
└── README.md
```

---

## 👨‍💻 Built With

- Java Swing (GUI)
- MySQL (Database)
- JDBC (Database connectivity)
- DAO Pattern (Data Access Object)
- Launch4j (JAR to EXE conversion)
- Inno Setup (Windows installer)

---

## 📌 Notes

- Sample student data is included in `database.sql`
- The system auto-generates Student IDs in format `STD-2025-XXX`
- Marks are saved per subject, term, and year
- `ScholarMS_Setup.exe` installs the app like a real Windows software
- The JAR is a fat JAR — includes MySQL connector, no extra classpath needed
