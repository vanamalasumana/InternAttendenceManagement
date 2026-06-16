# SyncIn — Intern Attendance Management Platform

> A full-stack web application for managing intern attendance, cohort tracking, leave requests, and QR-based punch-in — built for Cognizant's internship program.

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Tech Stack](#tech-stack)
4. [Database Schema](#database-schema)
5. [Backend — Spring Boot](#backend--spring-boot)
   - [Setup & Configuration](#setup--configuration)
   - [Authentication & Security](#authentication--security)
   - [REST API Reference](#rest-api-reference)
6. [Frontend — Angular](#frontend--angular)
   - [Setup](#frontend-setup)
   - [Routes](#routes)
   - [Components](#components)
   - [Services](#services)
7. [User Roles & Flows](#user-roles--flows)
8. [Sprint Breakdown](#sprint-breakdown)
9. [Running the Project Locally](#running-the-project-locally)
10. [Environment Configuration](#environment-configuration)
11. [Folder Structure](#folder-structure)

---

## Project Overview

**SyncIn** is an attendance and leave management system built for intern cohorts. It supports three user roles:

| Role   | Capabilities |
|--------|-------------|
| **ADMIN** | Hardcoded sudo user; creates POC accounts via admin panel |
| **POC** (Point of Contact) | Creates cohorts, onboards interns, promotes/demotes CRs, manages leave inbox |
| **CR** (Class Representative) | Generates daily QR tokens, monitors daily attendance, marks defaulters |
| **INTERN** | Scans QR code to punch in, submits leave requests, views attendance calendar & health |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      Browser                            │
│          Angular 21 (Standalone Components)             │
│          Tailwind CSS  |  html5-qrcode                  │
│          Port: 4200                                     │
└────────────────────────┬────────────────────────────────┘
                         │  HTTP/REST (JSON)
                         │  JWT in Authorization header
┌────────────────────────▼────────────────────────────────┐
│               Spring Boot 3.5 (Java 21)                 │
│               No Spring Security — Custom JwtInterceptor│
│               Port: 8081                                │
└────────────────────────┬────────────────────────────────┘
                         │  JPA / Hibernate
┌────────────────────────▼────────────────────────────────┐
│                    MySQL 8                              │
│                Database: syncindb                       │
└─────────────────────────────────────────────────────────┘
```

---

## Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Runtime |
| Spring Boot | 3.5.14 | Web framework |
| Spring Data JPA | (Boot managed) | ORM / DB access |
| Hibernate | (Boot managed) | JPA implementation |
| MySQL Connector/J | (Boot managed) | DB driver |
| jjwt (io.jsonwebtoken) | 0.11.5 | JWT generation & validation |
| Lombok | (Boot managed) | Boilerplate reduction |
| Maven | 3.x | Build tool |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Angular | 21 | SPA framework |
| TypeScript | 5.x | Language |
| Tailwind CSS | 3.x | Utility-first styling |
| html5-qrcode | 2.x | Camera QR scanner |
| Angular Signals | Built-in | Reactive state management |
| RxJS | 7.x | HTTP & async |

---

## Database Schema

### `users` table
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `emp_id` | VARCHAR | PK, NOT NULL, UNIQUE | Employee ID (string) |
| `name` | VARCHAR | — | Full name |
| `email` | VARCHAR | UNIQUE | Email address |
| `mobile_no` | VARCHAR | — | Mobile number |
| `password_hash` | VARCHAR | — | Plain-text password (Sprint 1 style) |
| `is_first_login` | BOOLEAN | DEFAULT true | Force password change on first login |
| `role` | ENUM | NOT NULL | `INTERN`, `CR`, `POC` |
| `batch_code` | VARCHAR | FK → cohorts(batch_code), nullable | Cohort assignment |

### `cohorts` table
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `batch_code` | VARCHAR | PK | Unique cohort/batch identifier |
| `track_name` | VARCHAR | — | Training track name |
| `poc_emp_id` | VARCHAR | FK → users(emp_id) | Owning POC employee ID |

### `attendance` table
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `attendance_id` | BIGINT | PK, AUTO_INCREMENT | — |
| `emp_id` | VARCHAR | FK → users(emp_id) | Which user |
| `date` | DATE | — | Attendance date |
| `status` | ENUM | — | `PRESENT`, `ABSENT`, `AL`, `UL` |

**Attendance Status Values:**
- `PRESENT` — Intern punched in via QR
- `ABSENT` — Defaulter (marked by CR) or no punch-in
- `AL` — Approved Leave (POC approved; counts as present for health)
- `UL` — Unapproved/Unofficial Leave (leave rejected or marked UL)

### `daily_tokens` table
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | — |
| `batch_code` | VARCHAR | — | Cohort this token belongs to |
| `date` | DATE | — | Valid date (today only) |
| `token` | VARCHAR | — | UUID token value |

### `leave_requests` table
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | — |
| `intern_emp_id` | VARCHAR | — | Intern who submitted |
| `batch_code` | VARCHAR | — | Intern's cohort |
| `reason` | VARCHAR | — | Short reason |
| `description` | TEXT | — | Detailed description |
| `request_date` | DATE | — | Leave date (defaults to today) |
| `status` | ENUM | — | `PENDING`, `APPROVED`, `REJECTED`, `UNAPPROVED` |

---

## Backend — Spring Boot

### Setup & Configuration

**`application.properties`**
```properties
server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/syncindb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

app.jwt.secret=SyncInSuperSecretKeyForJWTGeneration2024SyncInSuperSecretKeyForJWTGeneration2024
app.jwt.expiration=86400000
```

> `ddl-auto=update` means Hibernate auto-creates/updates tables on startup — **no manual SQL migration needed**.

### Authentication & Security

SyncIn uses **stateless JWT authentication WITHOUT Spring Security**.

#### How it works:

1. **Login** → `POST /auth/login` or `POST /admin/login`
2. **Server** validates credentials, returns a JWT token
3. **Client** stores token in `localStorage`:
   - Regular users → `jwt_token`
   - Admin → `admin_token`
4. **Every subsequent request** includes `Authorization: Bearer <token>` header
5. **`JwtInterceptor`** (Spring `HandlerInterceptor`) validates the token and sets `empId` and `role` as request attributes
6. **Controllers** read `role` from `request.getAttribute("role")` for authorization

#### JWT Token Claims:
```json
{
  "empId": "EMP001",
  "role": "INTERN",
  "iat": 1715000000,
  "exp": 1715086400
}
```

#### Excluded paths (no JWT required):
- `POST /auth/login`
- `POST /admin/login`

#### Admin credentials (hardcoded):
```
username: sudo
password: racingcar123
```

---

### REST API Reference

#### 🔐 Auth Endpoints (`/auth`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/login` | None | Login (INTERN/CR/POC) |
| PUT | `/auth/change-password` | JWT | Change password (first-login flow) |
| GET | `/auth/me` | JWT | Get current user's profile (name, role, empId) |

**POST `/auth/login`** Request:
```json
{ "empId": "EMP001", "password": "tempPass123" }
```
Response:
```json
{
  "token": "<jwt>",
  "role": "INTERN",
  "firstLogin": true,
  "empId": "EMP001",
  "name": "Rahul Sharma"
}
```

---

#### 🛡️ Admin Endpoints (`/admin`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/admin/login` | None | Admin sudo login |
| POST | `/admin/create-poc` | Admin JWT | Create a new POC user |

**POST `/admin/create-poc`** Request:
```json
{
  "empId": "POC001",
  "name": "Priya Mehta",
  "email": "priya@cognizant.com",
  "mobileNo": "9876543210",
  "passwordHash": "Welcome@123"
}
```

---

#### 👥 POC Endpoints (`/poc`)

All require `role = POC` in JWT.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/poc/cohorts` | Create a new cohort |
| GET | `/poc/cohorts` | List POC's cohorts |
| POST | `/poc/interns` | Onboard a new intern (generates temp password, logs it) |
| GET | `/poc/cohorts/{batchCode}/interns` | List interns of a cohort |
| PUT | `/poc/interns/{empId}/promote` | Promote intern → CR (max 2 CRs per cohort) |
| PUT | `/poc/interns/{empId}/demote` | Demote CR → INTERN |
| GET | `/poc/leaves/pending` | Get all pending leave requests across POC's cohorts |
| PUT | `/poc/leaves/{leaveId}/approve` | Approve leave (creates AL attendance) |
| PUT | `/poc/leaves/{leaveId}/reject` | Reject leave request |
| PUT | `/poc/leaves/{leaveId}/mark-ul` | Mark as unapproved leave (creates UL attendance) |
| GET | `/poc/cohorts/{batchCode}/summary` | Attendance summary (daily/weekly/monthly) |
| GET | `/poc/cohorts/{batchCode}/attendance-sheet` | Date-range attendance matrix |

**GET `/poc/cohorts/{batchCode}/summary`** Query params:
- `range`: `daily` (default) | `weekly` | `monthly`
- `date`: ISO date string (defaults to today)

**GET `/poc/cohorts/{batchCode}/attendance-sheet`** Query params:
- `from`: ISO date (defaults to 7 days ago)
- `to`: ISO date (defaults to today)

---

#### 📋 CR Endpoints (`/cr`)

All require `role = CR` in JWT.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/cr/generate-token` | Generate today's QR token (idempotent — returns existing if already generated) |
| GET | `/cr/today-token` | Get today's token value |
| GET | `/cr/present-today` | List interns marked PRESENT today |
| PUT | `/cr/mark-defaulter/{attendanceId}` | Change PRESENT → ABSENT for a defaulter |
| GET | `/cr/daily-analytics` | Attendance status for all cohort members on a date |
| GET | `/cr/attendance-sheet` | Date-range attendance matrix for the cohort |

**Defaulter marking rules:**
- Only works for **today's** records
- Cannot modify `AL` or `UL` statuses (locked by POC)
- Only `PRESENT` → `ABSENT` is allowed

---

#### 🧑‍💻 Intern Endpoints (`/intern`)

All require `role = INTERN` or `CR` in JWT.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/intern/punch-in` | Punch in using QR token |
| GET | `/intern/today-status` | Get today's attendance status |
| POST | `/intern/leave` | Submit a leave request |
| GET | `/intern/leave` | Get own leave history |
| GET | `/intern/calendar` | Attendance calendar for a month |
| GET | `/intern/health` | Attendance health score for a month |

**POST `/intern/punch-in`** Request:
```json
{ "token": "<daily-uuid-token>" }
```

**POST `/intern/leave`** Request:
```json
{
  "reason": "Medical",
  "description": "Doctor appointment",
  "date": "2026-05-20"
}
```
> `date` is optional; defaults to today.

**GET `/intern/health`** Response:
```json
{
  "empId": "EMP001",
  "from": "2026-05-01",
  "to": "2026-05-31",
  "totalDays": 31,
  "effectiveDays": 28,
  "attendancePercentage": 90.32,
  "bucket": "HEALTHY"
}
```

**Health Buckets:**
| Bucket | Condition |
|--------|-----------|
| `HEALTHY` | ≥ 87.01% |
| `AT_RISK` | 85.00% – 87.00% |
| `CRITICAL` | < 85.00% |

> Health formula: `(PRESENT + AL) / totalDays × 100`

---

## Frontend — Angular

### Frontend Setup

```bash
cd frontend/syncin-app
npm install
ng serve           # runs on http://localhost:4200
```

### Routes

| Path | Component | Access |
|------|-----------|--------|
| `/` | `UserLogin` | Public |
| `/admin` | `AdminSudo` | Admin only |
| `/change-password` | `ChangePassword` | First-login JWT |
| `/dashboard` | `Layout > PocDashboard` | POC |
| `/dashboard/cr` | `Layout > CrDashboard` | CR |
| `/dashboard/intern` | `Layout > InternDashboard` | INTERN |

**Route Guards (implicit):**
- After login, `role` from JWT response determines which dashboard to navigate to
- First-login flag forces redirect to `/change-password`
- If `admin_token` exists and user visits `/`, they are redirected to `/admin`

### Components

#### `UserLogin`
- Single login form for all INTERN/CR/POC users
- Sends `POST /auth/login`
- Redirects based on `role` and `firstLogin` flag

#### `AdminSudo`
- Admin login form at `/admin`
- After login, shows a form to create new POC accounts
- Uses `admin_token` in `localStorage`

#### `ChangePassword`
- Shown to first-time users after login
- Calls `PUT /auth/change-password`
- Redirects to dashboard after success

#### `Layout`
- Persistent sidebar shell for all dashboards
- **Sidebar features:**
  - "Hi, `<name>`" welcome message (fetched from `localStorage` after login)
  - Role-based nav links
  - **Leave Inbox** (POC only) — shows red badge with pending leave count (hidden when 0)
  - Hamburger toggle (☰) at top-right of sidebar; appears in top bar when sidebar is closed
  - Logout button clears all localStorage keys
- **Signals used:** `sidebarOpen`, `role`, `userName`, `pendingLeaveCount`
- Polls pending leave count on init (POC only)

#### `PocDashboard`
- **Cohort management**: Create cohorts, list cohorts, expand to see interns
- **Intern management**: Onboard interns, change role (Intern ↔ CR via dropdown, max 2 CRs enforced on frontend with alert)
- **Leave Inbox section**: Approve / Reject / Mark UL each pending leave
- **Cohort Summary**: Daily/Weekly/Monthly attendance breakdown with health buckets
- **Attendance Sheet**: Date-range matrix table

#### `CrDashboard`
- **QR Token**: Generate today's QR code (displayed via `qrcode` canvas), view token string
- **Camera Scanner**: html5-qrcode camera feed with inner green border scanning box and outer padding
- **Present Today / Defaulter Marking**: List of present interns; one-click to mark as defaulter
- **Daily Analytics**: Date selector, status breakdown table for all cohort members
- **Attendance Sheet**: Date-range matrix

#### `InternDashboard`
- **Punch-in** section: Camera QR scan or manual token entry
- **Today's status** badge (PRESENT / ABSENT / NOT_MARKED)
- **Leave Form**: Submit leave with reason, description, date
- **Attendance Calendar**: Color-coded month view
  - 🟢 PRESENT, 🔴 ABSENT, 🟡 AL, 🟠 UL
- **Health Bar**: Visual progress bar with HEALTHY / AT_RISK / CRITICAL badge
- **Leave History**: Table of past leave requests and their status

### Services

#### `AuthService` (`services/auth.ts`)
| Method | Description |
|--------|-------------|
| `login(empId, password)` | POST to `/auth/login`; stores `jwt_token`, `role`, `emp_id`, `user_name` |
| `changePassword(newPass)` | PUT to `/auth/change-password` |
| `getMyProfile()` | GET `/auth/me` |
| `getRole()` | Returns `localStorage.getItem('role')` |
| `getUserName()` | Returns `localStorage.getItem('user_name')` |
| `getToken()` | Returns `localStorage.getItem('jwt_token')` |
| `logout()` | Clears all localStorage keys, navigates to `/` |

#### `AttendanceApiService` (`services/attendance-api.ts`)
| Method | Endpoint |
|--------|----------|
| `punchIn(token)` | POST `/intern/punch-in` |
| `getTodayStatus()` | GET `/intern/today-status` |
| `submitLeave(body)` | POST `/intern/leave` |
| `getMyLeaves()` | GET `/intern/leave` |
| `getCalendar(month?)` | GET `/intern/calendar` |
| `getHealth(month?)` | GET `/intern/health` |
| `generateToken()` | POST `/cr/generate-token` |
| `getTodayToken()` | GET `/cr/today-token` |
| `getPresentToday()` | GET `/cr/present-today` |
| `markDefaulter(id)` | PUT `/cr/mark-defaulter/{id}` |
| `getCrDailyAnalytics(date?)` | GET `/cr/daily-analytics` |
| `getCrAttendanceSheet(from,to)` | GET `/cr/attendance-sheet` |

#### `PocApiService` (`services/poc-api.ts`)
| Method | Endpoint |
|--------|----------|
| `getMyCohorts()` | GET `/poc/cohorts` |
| `createCohort(body)` | POST `/poc/cohorts` |
| `onboardIntern(body)` | POST `/poc/interns` |
| `getInterns(batchCode)` | GET `/poc/cohorts/{batchCode}/interns` |
| `promote(empId)` | PUT `/poc/interns/{empId}/promote` |
| `demote(empId)` | PUT `/poc/interns/{empId}/demote` |
| `getPendingLeaves()` | GET `/poc/leaves/pending` |
| `approveLeave(id)` | PUT `/poc/leaves/{id}/approve` |
| `rejectLeave(id)` | PUT `/poc/leaves/{id}/reject` |
| `markUlLeave(id)` | PUT `/poc/leaves/{id}/mark-ul` |
| `getCohortSummary(batchCode, range, date?)` | GET `/poc/cohorts/{batchCode}/summary` |
| `getAttendanceSheet(batchCode, from, to)` | GET `/poc/cohorts/{batchCode}/attendance-sheet` |

#### `AuthInterceptor` (`interceptors/auth.interceptor.ts`)
Automatically attaches `Authorization: Bearer <token>` to every outgoing HTTP request using the `jwt_token` from localStorage.

---

## User Roles & Flows

### Admin Flow
```
/admin → Admin login (sudo / racingcar123)
       → Create POC (empId, name, email, mobile, password)
```

### POC Flow
```
/ → Login → /dashboard
         → Create Cohort (batch_code, track_name)
         → Onboard Intern (temp password auto-generated, logged in backend console)
         → Set Role (INTERN / CR via dropdown; max 2 CRs enforced)
         → Leave Inbox (Approve / Reject / Mark UL)
         → View Cohort Summary (Daily/Weekly/Monthly health)
         → View Attendance Sheet
```

### CR Flow
```
/ → Login → /dashboard/cr
         → Generate QR Token for today (shown as QR + camera scanner for own use)
         → View Present Interns Today
         → Mark Defaulters (PRESENT → ABSENT)
         → View Daily Analytics
         → View Attendance Sheet
```

### Intern Flow
```
/ → Login → /change-password (first time only)
          → /dashboard/intern
          → Punch In (scan QR via camera or enter token manually)
          → View Today's Status
          → Submit Leave Request
          → View Calendar (color-coded monthly view)
          → View Health Bar (attendance percentage + bucket)
          → View Leave History
```

---

## Sprint Breakdown

### Sprint 1 — Foundation & Auth
- Spring Boot project setup (port 8081, MySQL connection)
- `User` entity with `emp_id` as PK, `Role` enum (INTERN, CR, POC)
- JWT auth via jjwt (no Spring Security)
- `JwtInterceptor` as `HandlerInterceptor`
- `POST /auth/login`, `PUT /auth/change-password`
- Admin sudo login (`POST /admin/login`) + `POST /admin/create-poc`
- Angular standalone components, Tailwind CSS setup
- `UserLogin`, `AdminSudo`, `ChangePassword` components
- `AuthInterceptor` for automatic JWT injection
- Route-based navigation by role

### Sprint 2 — POC Dashboard & Intern Onboarding
- `Cohort` entity (batch_code PK, track_name, poc_emp_id FK)
- `User` ↔ `Cohort` ManyToOne relationship via `batch_code`
- POC CRUD: create cohort, onboard intern, promote/demote CR
- Max 2 CRs per cohort (enforced backend + frontend alert)
- Temp password generation (SecureRandom, 8 chars, logged via SLF4J)
- Angular POC dashboard with cohort cards, intern table with role dropdown

### Sprint 3 — QR Attendance & CR Monitoring
- `DailyToken` entity — UUID token per cohort per day
- `Attendance` entity — emp_id + date + status
- `GET /cr/generate-token` — idempotent daily QR
- `POST /intern/punch-in` — validates token, cohort, date; marks PRESENT
- Camera-based QR scanning with html5-qrcode (outer box + inner green scan frame)
- `GET /cr/present-today` + `PUT /cr/mark-defaulter/{id}`
- Intern dashboard: punch-in, today's status
- CR dashboard: QR display, present list, defaulter marking

### Sprint 4 — Leave System & Analytics
- `LeaveRequest` entity with `LeaveStatus` enum
- `LeaveManagementService` — full leave lifecycle
- Intern: submit leave, leave history
- POC: pending inbox, approve/reject/mark-UL (creates attendance record)
- Attendance health formula: `(PRESENT + AL) / totalDays`
- Health buckets: HEALTHY ≥ 87%, AT_RISK 85-87%, CRITICAL < 85%
- Color-coded monthly calendar
- CR: daily analytics, attendance sheet
- POC: cohort summary (daily/weekly/monthly), attendance sheet matrix
- Sidebar: leave inbox badge, "Hi, `<name>`" welcome

---

## Running the Project Locally

### Prerequisites
- Java 21+
- Maven 3.x
- MySQL 8.x (running on localhost:3306)
- Node.js 18+ and npm
- Angular CLI (`npm install -g @angular/cli`)

### Step 1: Setup MySQL
```sql
-- MySQL will auto-create the database on first backend start
-- Just ensure MySQL is running with user: root / password: root
-- Or update application.properties with your credentials
```

### Step 2: Run the Backend
```bash
cd backend
./mvnw spring-boot:run
```
> Backend starts on **http://localhost:8081**
> On first run, Hibernate creates all tables automatically.

### Step 3: Run the Frontend
```bash
cd frontend/syncin-app
npm install
ng serve
```
> Frontend starts on **http://localhost:4200**

### Step 4: Create First Admin POC
1. Open http://localhost:4200/admin
2. Login with `sudo` / `racingcar123`
3. Create a POC account (fill empId, name, email, mobile, password)

### Step 5: Login as POC
1. Open http://localhost:4200
2. Login with the POC credentials you just created
3. Create a cohort, onboard interns, assign CRs

---

## Environment Configuration

The frontend uses a single environment file:

**`src/environments/environment.ts`**
```typescript
const hostname = window.location.hostname;
export const environment = {
  apiUrl: hostname === 'localhost'
    ? 'http://localhost:8081'
    : 'https://<your-devtunnel-id>-8081.inc1.devtunnels.ms'
};
```

> When running locally, `apiUrl` automatically resolves to `http://localhost:8081`.  
> When deployed via VS Code Dev Tunnels, update the devtunnel URL accordingly.

---

## Folder Structure

```
SyncIn/
├── backend/                          # Spring Boot application
│   ├── pom.xml
│   └── src/main/java/com/intqeasd007/SyncIn/
│       ├── SyncInApplication.java
│       ├── config/
│       │   └── WebConfig.java        # CORS + JWT interceptor registration
│       ├── controller/
│       │   ├── AdminController.java  # /admin/**
│       │   ├── AuthController.java   # /auth/**
│       │   ├── POCController.java    # /poc/**
│       │   ├── CRController.java     # /cr/**
│       │   └── InternController.java # /intern/**
│       ├── dto/
│       │   ├── LoginRequest.java
│       │   ├── LoginResponse.java
│       │   ├── CreateUserRequest.java
│       │   ├── CreateCohortRequest.java
│       │   ├── OnboardInternRequest.java
│       │   ├── ChangePasswordRequest.java
│       │   └── SubmitLeaveRequest.java
│       ├── entity/
│       │   ├── User.java
│       │   ├── Cohort.java
│       │   ├── Attendance.java
│       │   ├── AttendanceStatus.java # Enum: PRESENT, ABSENT, AL, UL
│       │   ├── DailyToken.java
│       │   ├── LeaveRequest.java
│       │   ├── LeaveStatus.java      # Enum: PENDING, APPROVED, REJECTED, UNAPPROVED
│       │   └── Role.java             # Enum: INTERN, CR, POC
│       ├── repository/
│       │   ├── UserRepository.java
│       │   ├── CohortRepository.java
│       │   ├── AttendanceRepository.java
│       │   ├── DailyTokenRepository.java
│       │   └── LeaveRequestRepository.java
│       ├── security/
│       │   ├── JwtUtil.java          # Token generation & validation
│       │   └── JwtInterceptor.java   # Validates JWT on every request
│       └── service/
│           └── LeaveManagementService.java
│
└── frontend/syncin-app/              # Angular application
    ├── angular.json
    ├── package.json
    ├── tailwind.config.js
    └── src/
        ├── environments/
        │   └── environment.ts        # API URL config
        └── app/
            ├── app.routes.ts         # Route definitions
            ├── app.config.ts         # App bootstrap config
            ├── components/
            │   ├── admin-sudo/       # Admin panel
            │   ├── change-password/  # First-login password change
            │   ├── user-login/       # Common login page
            │   ├── layout/           # Sidebar shell
            │   ├── poc-dashboard/    # POC features
            │   ├── cr-dashboard/     # CR features
            │   └── intern-dashboard/ # Intern features
            ├── interceptors/
            │   └── auth.interceptor.ts  # Auto-attach JWT
            └── services/
                ├── auth.ts              # Auth API + localStorage
                ├── attendance-api.ts    # Intern & CR API calls
                └── poc-api.ts           # POC API calls
```

---

## Known Limitations / Dev Notes

1. **Passwords are stored as plain text** — suitable for demo/prototype only; production should use BCrypt.
2. **Admin credentials are hardcoded** — `sudo` / `racingcar123` in `AdminController.java`.
3. **No route guards** — navigation is purely handled by login redirection logic; direct URL access is not blocked.
4. **`ddl-auto=update`** — fine for development; use `validate` or Flyway migrations in production.
5. **CORS is wide open** (`allowedOrigins("*")`) — acceptable for dev; restrict in production.
6. **Temp password logged in plain text** to backend console — a developer shortcut; use email delivery in production.

---

*Documentation generated: May 2026 | SyncIn v1.0 (Sprint 4)*

