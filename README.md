# EventCraft - Task and Schedule Management Module (UC-17)

This is the complete full-stack implementation of the **Task and Schedule Management** module of **EventCraft**, developed for **SE2030 Software Engineering (Year 2 Semester 1 - 2026)** at SLIIT.

- **Module Lead:** IT25104083 – Senanayake Y.I.D.P (Member 5)
- **Use Case:** UC-17 (Manage Tasks & Schedule)
- **Platform:** Java (Spring Boot 3.3.4, Java 22, JDBC)
- **Database:** Microsoft SQL Server (T-SQL)
- **Frontend:** Responsive Vanilla HTML5, CSS3, Modern ES6 JavaScript (served by Spring Boot)
- **Architecture:** Layered MVC + DAO Pattern + Singleton Connection Pooling + Dedicated Frontend API Layer

---

## 1. Prerequisites

1. **Java Development Kit (JDK 17 or higher, JDK 22 recommended)**
2. **Microsoft SQL Server & SQL Server Management Studio (SSMS)**
3. **Apache Maven** (or use the included `mvnw.cmd` wrapper or IntelliJ IDEA)

---

## 2. Database Setup

1. Open **SSMS** and connect to your SQL Server instance.
2. Execute `schema.sql` (found in the root folder).
3. This creates `EventCraftDB`, stub reference tables (`Users`, `Events`), and the primary tables (`Tasks`, `Schedules`) with sample records.

---

## 3. Configuration & Environment Variables

1. Copy `.env.example` to `.env`:
   ```powershell
   Copy-Item .env.example .env
   ```
2. Update `.env` with your SQL Server credentials:
   ```env
   DB_HOST=localhost
   DB_PORT=1433
   DB_NAME=EventCraftDB
   DB_USER=sa
   DB_PASSWORD=YourPasswordHere
   DB_ENCRYPT=false
   DB_TRUST_SERVER_CERT=true
   SERVER_PORT=8080
   ```

---

## 4. Running the Application

In the project root directory:

```powershell
.\mvnw.cmd spring-boot:run
```

Once started, open your web browser and navigate to:
👉 **`http://localhost:8080`**

---

## 5. UI Button to API Endpoint Mapping

| UI Element / Button | Location | Action / Flow | Backend API Endpoint | SQL Operation |
| :--- | :--- | :--- | :--- | :--- |
| **Page Load / Tab Switch** | Header Tabs | Fetches event tasks & metrics | `GET /api/tasks?eventId=1`<br>`GET /api/tasks/metrics/1` | `SELECT ... FROM Tasks` |
| **Status Filter Dropdown** | Task Toolbar | Filters tasks by status | `GET /api/tasks?eventId=1&status={val}` | `SELECT ... WHERE status = ?` |
| **"+ Create New Task"** ➔ **"Save Task"** | Task Modal | Validates form & creates task | `POST /api/tasks` | `INSERT INTO Tasks ...` |
| **"✏ Edit"** (on Task Card) | Task Card | Pre-fills modal with task data | `GET /api/tasks/{id}` | `SELECT ... WHERE task_id = ?` |
| **"Save Task"** (in Edit mode) | Task Modal | Updates deadline, priority, progress | `PUT /api/tasks/{id}` | `UPDATE Tasks SET ...` |
| **"✓ Mark Done"** | Task Card | Quick-completes task (progress = 100) | `PUT /api/tasks/{id}` | `UPDATE Tasks SET progress=100, status='Completed'` |
| **"🗑 Delete"** ➔ **"Yes, Delete"** | Task Card / Modal | Confirms & deletes task | `DELETE /api/tasks/{id}` | `DELETE FROM Tasks WHERE task_id = ?` |
| **"⏱ Schedule & Timeline" Tab** | Tab Bar | Fetches chronological timeline | `GET /api/schedules?eventId=1` | `SELECT ... ORDER BY start_time ASC` |
| **"+ Add Timeline Activity"** ➔ **"Save Activity"** | Schedule Modal | Checks conflicts & creates activity | `POST /api/schedules` | `SELECT COUNT(*)` (conflict check) ➔ `INSERT INTO Schedules ...` |
| **"✏ Edit"** (on Timeline Card) | Timeline Item | Pre-fills modal with activity data | `GET /api/schedules/{id}` | `SELECT ... WHERE schedule_id = ?` |
| **"Save Activity"** (in Edit mode) | Schedule Modal | Validates times & updates activity | `PUT /api/schedules/{id}` | `SELECT COUNT(*)` (conflict check) ➔ `UPDATE Schedules SET ...` |
| **"🗑 Delete"** ➔ **"Yes, Delete"** | Timeline Item / Modal| Confirms & deletes activity | `DELETE /api/schedules/{id}` | `DELETE FROM Schedules WHERE schedule_id = ?` |