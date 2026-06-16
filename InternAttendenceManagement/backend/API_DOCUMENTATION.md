# SyncIn Backend — API Documentation

> **Base URL:** `http://localhost:8081`  
> **Auth mechanism:** Stateless JWT via `Authorization: Bearer <token>` header  
> **Content-Type:** `application/json` (all request/response bodies)

---

## Table of Contents

1. [Authentication & Security Overview](#authentication--security-overview)
2. [Auth Endpoints (`/auth`)](#auth-endpoints-auth)
3. [Admin Endpoints (`/admin`)](#admin-endpoints-admin)
4. [POC Endpoints (`/poc`)](#poc-endpoints-poc)
5. [CR Endpoints (`/cr`)](#cr-endpoints-cr)
6. [Intern Endpoints (`/intern`)](#intern-endpoints-intern)
7. [Error Response Format](#error-response-format)
8. [Enums & Constants](#enums--constants)

---

## Authentication & Security Overview

| Concept | Detail |
|---------|--------|
| Library | `io.jsonwebtoken` (jjwt 0.11.5) |
| Algorithm | HMAC-SHA256 |
| Token lifetime | 24 hours (86400000 ms) |
| Token claims | `empId`, `role`, `iat`, `exp` |
| Excluded paths | `POST /auth/login`, `POST /admin/login` |
| Interceptor | `JwtInterceptor` (Spring `HandlerInterceptor`) |

**How it flows:**
1. Client calls login endpoint → receives JWT
2. Client stores JWT in `localStorage`
3. Every request includes header: `Authorization: Bearer <jwt>`
4. `JwtInterceptor` validates token, sets `empId` and `role` as request attributes
5. Controllers read `request.getAttribute("empId")` / `request.getAttribute("role")`

---

## Auth Endpoints (`/auth`)

### POST `/auth/login`

Login for INTERN, CR, and POC users.

**Auth required:** ❌ None

**Request Body:**
```json
{
  "empId": "EMP001",
  "password": "myPassword123"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "INTERN",
  "firstLogin": true,
  "empId": "EMP001",
  "name": "Rahul Sharma"
}
```

**Error Responses:**
| Status | Body |
|--------|------|
| 401 | `{ "error": "Invalid credentials" }` |

---

### PUT `/auth/change-password`

Change password (typically on first login).

**Auth required:** ✅ JWT (any role)

**Request Body:**
```json
{
  "newPassword": "NewSecurePass@456"
}
```

**Success Response (200):**
```json
{
  "message": "Password changed successfully"
}
```

**Error Responses:**
| Status | Body |
|--------|------|
| 401 | `{ "error": "Unauthorized" }` — no valid JWT |
| 404 | `{ "error": "User not found" }` |

---

### GET `/auth/me`

Get current authenticated user's profile.

**Auth required:** ✅ JWT (any role)

**Success Response (200):**
```json
{
  "empId": "EMP001",
  "name": "Rahul Sharma",
  "role": "INTERN"
}
```

**Error Responses:**
| Status | Body |
|--------|------|
| 401 | `{ "error": "Unauthorized" }` |
| 404 | `{ "error": "User not found" }` |

---

## Admin Endpoints (`/admin`)

### POST `/admin/login`

Admin sudo login (hardcoded credentials).

**Auth required:** ❌ None

**Credentials:** `sudo` / `racingcar123`

**Request Body:**
```json
{
  "username": "sudo",
  "password": "racingcar123"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Error Responses:**
| Status | Body |
|--------|------|
| 401 | `{ "error": "Invalid admin credentials" }` |

---

### POST `/admin/create-poc`

Create a new POC (Point of Contact) user account.

**Auth required:** ✅ Admin JWT (`role = ADMIN`)

**Request Body:**
```json
{
  "empId": "POC001",
  "name": "Priya Mehta",
  "email": "priya@cognizant.com",
  "mobileNo": "9876543210",
  "passwordHash": "Welcome@123"
}
```

**Success Response (200):**
```json
{
  "empId": "POC001",
  "name": "Priya Mehta",
  "role": "POC"
}
```

**Error Responses:**
| Status | Body |
|--------|------|
| 403 | `{ "error": "Forbidden" }` — not ADMIN role |

---

## POC Endpoints (`/poc`)

> All endpoints require `role = POC` in JWT.

### POST `/poc/cohorts`

Create a new cohort/batch.

**Request Body:**
```json
{
  "batchCode": "BATCH-2026-A",
  "trackName": "Full Stack Java"
}
```

**Success Response (200):**
```json
{
  "batchCode": "BATCH-2026-A",
  "trackName": "Full Stack Java"
}
```

**Error Responses:**
| Status | Body |
|--------|------|
| 400 | `{ "error": "Batch code already exists" }` |
| 403 | `{ "error": "Forbidden" }` |

---

### GET `/poc/cohorts`

List all cohorts owned by the authenticated POC.

**Success Response (200):**
```json
[
  {
    "batchCode": "BATCH-2026-A",
    "trackName": "Full Stack Java",
    "pocEmpId": "POC001"
  }
]
```

---

### POST `/poc/interns`

Onboard (create) a new intern into a cohort. Generates a random 8-character temporary password.

**Request Body:**
```json
{
  "empId": "INT001",
  "name": "Kushik Kumar",
  "email": "kushik@cognizant.com",
  "mobileNo": "9876543211",
  "batchCode": "BATCH-2026-A"
}
```

**Success Response (200):**
```json
{
  "empId": "INT001",
  "name": "Kushik Kumar",
  "role": "INTERN",
  "batchCode": "BATCH-2026-A",
  "tempPassword": "Ab3kPq7n"
}
```

> ⚠️ The `tempPassword` is also logged in the backend console via SLF4J for developer testing.

**Error Responses:**
| Status | Body |
|--------|------|
| 400 | `{ "error": "Invalid cohort" }` |
| 403 | `{ "error": "Forbidden" }` |

---

### GET `/poc/cohorts/{batchCode}/interns`

List all interns/CRs in a specific cohort.

**Path Params:** `batchCode` — cohort identifier

**Success Response (200):**
```json
[
  {
    "empId": "INT001",
    "name": "Kushik Kumar",
    "email": "kushik@cognizant.com",
    "mobileNo": "9876543211",
    "role": "INTERN"
  },
  {
    "empId": "INT002",
    "name": "Ravi Singh",
    "email": "ravi@cognizant.com",
    "mobileNo": "9876543212",
    "role": "CR"
  }
]
```

---

### PUT `/poc/interns/{empId}/promote`

Promote an INTERN to CR. Maximum 2 CRs per cohort enforced.

**Path Params:** `empId` — employee ID of intern to promote

**Success Response (200):**
```json
{
  "empId": "INT001",
  "name": "Kushik Kumar",
  "role": "CR"
}
```

**Error Responses:**
| Status | Body |
|--------|------|
| 400 | `{ "error": "User not found" }` |
| 400 | `{ "error": "User is already a CR" }` |
| 400 | `{ "error": "Maximum 2 CRs allowed per cohort" }` |
| 403 | `{ "error": "Forbidden" }` |

---

### PUT `/poc/interns/{empId}/demote`

Demote a CR back to INTERN.

**Path Params:** `empId` — employee ID of CR to demote

**Success Response (200):**
```json
{
  "empId": "INT001",
  "name": "Kushik Kumar",
  "role": "INTERN"
}
```

**Error Responses:**
| Status | Body |
|--------|------|
| 400 | `{ "error": "User not found" }` |
| 400 | `{ "error": "User is not a CR" }` |
| 403 | `{ "error": "Forbidden" }` |

---

### GET `/poc/leaves/pending`

Get all pending leave requests across all cohorts owned by this POC.

**Success Response (200):**
```json
[
  {
    "id": 1,
    "internEmpId": "INT001",
    "batchCode": "BATCH-2026-A",
    "reason": "Medical",
    "description": "Doctor appointment",
    "requestDate": "2026-05-20",
    "status": "PENDING"
  }
]
```

---

### PUT `/poc/leaves/{leaveId}/approve`

Approve a pending leave request. Creates an `AL` attendance record for that date.

**Path Params:** `leaveId` — leave request ID

**Success Response (200):**
```json
{
  "message": "Leave approved",
  "leaveId": 1,
  "status": "APPROVED"
}
```

**Error Responses:**
| Status | Body |
|--------|------|
| 400 | `{ "error": "..." }` — leave not found or not pending |
| 403 | `{ "error": "Forbidden" }` |

---

### PUT `/poc/leaves/{leaveId}/reject`

Reject a pending leave request.

**Path Params:** `leaveId` — leave request ID

**Success Response (200):**
```json
{
  "message": "Leave rejected",
  "leaveId": 1,
  "status": "REJECTED"
}
```

---

### PUT `/poc/leaves/{leaveId}/mark-ul`

Mark a pending leave request as Unapproved Leave. Creates a `UL` attendance record.

**Path Params:** `leaveId` — leave request ID

**Success Response (200):**
```json
{
  "message": "Leave marked as UL",
  "leaveId": 1,
  "status": "UNAPPROVED"
}
```

---

### GET `/poc/cohorts/{batchCode}/summary`

Get attendance health summary for all members in a cohort.

**Path Params:** `batchCode`

**Query Params:**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `range` | string | `daily` | One of: `daily`, `weekly`, `monthly` |
| `date` | string (ISO) | today | Anchor date for the range |

**Success Response (200):**
```json
{
  "batchCode": "BATCH-2026-A",
  "range": "monthly",
  "from": "2026-05-01",
  "to": "2026-05-31",
  "rows": [
    {
      "empId": "INT001",
      "name": "Kushik Kumar",
      "role": "INTERN",
      "attendancePercentage": 90.32,
      "bucket": "HEALTHY"
    },
    {
      "empId": "INT002",
      "name": "Ravi Singh",
      "role": "CR",
      "attendancePercentage": 84.5,
      "bucket": "CRITICAL"
    }
  ]
}
```

---

### GET `/poc/cohorts/{batchCode}/attendance-sheet`

Get date-wise attendance matrix for all cohort members.

**Path Params:** `batchCode`

**Query Params:**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `from` | string (ISO) | 7 days ago | Start date |
| `to` | string (ISO) | today | End date |

**Success Response (200):**
```json
{
  "batchCode": "BATCH-2026-A",
  "from": "2026-05-14",
  "to": "2026-05-20",
  "dates": ["2026-05-14", "2026-05-15", "2026-05-16", "2026-05-19", "2026-05-20"],
  "rows": [
    {
      "empId": "INT001",
      "name": "Kushik Kumar",
      "role": "INTERN",
      "days": {
        "2026-05-14": "PRESENT",
        "2026-05-15": "PRESENT",
        "2026-05-16": "AL",
        "2026-05-19": "PRESENT",
        "2026-05-20": "ABSENT"
      }
    }
  ]
}
```

---

## CR Endpoints (`/cr`)

> All endpoints require `role = CR` in JWT.

### POST `/cr/generate-token`

Generate today's QR attendance token for the CR's cohort. Idempotent — returns existing token if already generated.

**Request Body:** None

**Success Response (200) — new token:**
```json
{
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "batchCode": "BATCH-2026-A",
  "date": "2026-05-20"
}
```

**Success Response (200) — already exists:**
```json
{
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "batchCode": "BATCH-2026-A",
  "date": "2026-05-20",
  "message": "Token already generated for today"
}
```

---

### GET `/cr/today-token`

Get today's token value (without generating a new one).

**Success Response (200):**
```json
{
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "batchCode": "BATCH-2026-A",
  "date": "2026-05-20"
}
```

> If no token exists yet, `token` will be an empty string `""`.

---

### GET `/cr/present-today`

Get list of interns who punched in today (status = PRESENT).

**Success Response (200):**
```json
[
  {
    "attendanceId": 42,
    "empId": "INT001",
    "name": "Kushik Kumar",
    "status": "PRESENT"
  }
]
```

---

### PUT `/cr/mark-defaulter/{attendanceId}`

Mark a PRESENT intern as ABSENT (defaulter). Only allowed for today's records.

**Path Params:** `attendanceId` — attendance record ID

**Success Response (200):**
```json
{
  "attendanceId": 42,
  "empId": "INT001",
  "name": "Kushik Kumar",
  "status": "ABSENT",
  "message": "Marked as defaulter"
}
```

**Error Responses:**
| Status | Body |
|--------|------|
| 400 | `{ "error": "Attendance record not found" }` |
| 400 | `{ "error": "Can only modify today's attendance" }` |
| 400 | `{ "error": "Cannot modify: status is locked (AL/UL)" }` |
| 400 | `{ "error": "Can only mark PRESENT as defaulter" }` |
| 403 | `{ "error": "Forbidden" }` — record not in CR's cohort |

---

### GET `/cr/daily-analytics`

Get attendance status for all cohort members on a specific date.

**Query Params:**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `date` | string (ISO) | today | Target date |

**Success Response (200):**
```json
{
  "batchCode": "BATCH-2026-A",
  "date": "2026-05-20",
  "rows": [
    {
      "empId": "INT001",
      "name": "Kushik Kumar",
      "role": "INTERN",
      "status": "PRESENT"
    },
    {
      "empId": "INT002",
      "name": "Ravi Singh",
      "role": "CR",
      "status": "ABSENT"
    }
  ]
}
```

> Members without an attendance record for that date show `"status": "ABSENT"`.

---

### GET `/cr/attendance-sheet`

Get date-range attendance matrix for the CR's cohort.

**Query Params:**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `from` | string (ISO) | 7 days ago | Start date |
| `to` | string (ISO) | today | End date |

**Success Response (200):**
```json
{
  "batchCode": "BATCH-2026-A",
  "from": "2026-05-14",
  "to": "2026-05-20",
  "dates": ["2026-05-14", "2026-05-15", "2026-05-19", "2026-05-20"],
  "rows": [
    {
      "empId": "INT001",
      "name": "Kushik Kumar",
      "role": "INTERN",
      "days": {
        "2026-05-14": "PRESENT",
        "2026-05-15": "ABSENT",
        "2026-05-19": "PRESENT",
        "2026-05-20": "PRESENT"
      }
    }
  ]
}
```

---

## Intern Endpoints (`/intern`)

> All endpoints require `role = INTERN` or `role = CR` in JWT (unless noted otherwise).

### POST `/intern/punch-in`

Punch in using a QR token. Validates token belongs to the intern's cohort and is today's token.

**Request Body:**
```json
{
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**Success Response (200):**
```json
{
  "message": "Punch-in successful!",
  "status": "PRESENT",
  "date": "2026-05-20"
}
```

**Error Responses:**
| Status | Body |
|--------|------|
| 400 | `{ "error": "Token is required" }` |
| 400 | `{ "error": "Already punched in today", "status": "PRESENT" }` |
| 400 | `{ "error": "Invalid QR token" }` |
| 400 | `{ "error": "Token does not match your cohort" }` |
| 400 | `{ "error": "Token has expired (not today's token)" }` |
| 403 | `{ "error": "Forbidden" }` |

---

### GET `/intern/today-status`

Get today's attendance status for the authenticated user.

**Success Response (200) — punched in:**
```json
{
  "status": "PRESENT",
  "date": "2026-05-20",
  "punchedIn": true
}
```

**Success Response (200) — not yet punched in:**
```json
{
  "status": "NOT_MARKED",
  "date": "2026-05-20",
  "punchedIn": false
}
```

---

### POST `/intern/leave`

Submit a leave request. Only `role = INTERN` allowed.

**Auth required:** ✅ JWT (`role = INTERN` only)

**Request Body:**
```json
{
  "reason": "Medical",
  "description": "Doctor appointment in the afternoon",
  "date": "2026-05-22"
}
```

> `date` is optional — defaults to today if omitted.

**Success Response (200):**
```json
{
  "message": "Leave submitted",
  "leaveId": 5,
  "status": "PENDING"
}
```

**Error Responses:**
| Status | Body |
|--------|------|
| 400 | `{ "error": "..." }` — validation failure |
| 403 | `{ "error": "Only intern can submit leave" }` |

---

### GET `/intern/leave`

Get leave history for the authenticated user.

**Success Response (200):**
```json
[
  {
    "id": 5,
    "internEmpId": "INT001",
    "batchCode": "BATCH-2026-A",
    "reason": "Medical",
    "description": "Doctor appointment",
    "requestDate": "2026-05-22",
    "status": "PENDING"
  },
  {
    "id": 3,
    "internEmpId": "INT001",
    "batchCode": "BATCH-2026-A",
    "reason": "Personal",
    "description": "Family function",
    "requestDate": "2026-05-18",
    "status": "APPROVED"
  }
]
```

---

### GET `/intern/calendar`

Get color-coded attendance calendar for a month.

**Query Params:**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `month` | string (ISO, e.g. `2026-05`) | current month | Target month |

**Success Response (200):**
```json
{
  "empId": "INT001",
  "month": "2026-05",
  "days": {
    "2026-05-01": "PRESENT",
    "2026-05-02": "PRESENT",
    "2026-05-03": "ABSENT",
    "2026-05-06": "AL",
    "2026-05-07": "PRESENT",
    "2026-05-08": "UL"
  }
}
```

> Only dates with attendance records are included. Dates not in the map have no record.

---

### GET `/intern/health`

Get attendance health score and bucket for a month.

**Query Params:**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `month` | string (ISO, e.g. `2026-05`) | current month | Target month |

**Success Response (200):**
```json
{
  "empId": "INT001",
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
| `HEALTHY` | attendance ≥ 87.01% |
| `AT_RISK` | 85.00% – 87.00% |
| `CRITICAL` | < 85.00% |

**Formula:** `(PRESENT + AL) / totalDays × 100`

---

## Error Response Format

All error responses follow a consistent format:

```json
{
  "error": "Human-readable error message"
}
```

**Common HTTP status codes used:**
| Code | Meaning |
|------|---------|
| 200 | Success |
| 400 | Bad Request — validation failure, business rule violation |
| 401 | Unauthorized — missing or invalid JWT |
| 403 | Forbidden — valid JWT but insufficient role/permissions |
| 404 | Not Found — resource does not exist |

---

## Enums & Constants

### Role (`com.intqeasd007.SyncIn.entity.Role`)
| Value | Description |
|-------|-------------|
| `INTERN` | Regular intern |
| `CR` | Class Representative (max 2 per cohort) |
| `POC` | Point of Contact (manages cohorts) |

### AttendanceStatus (`com.intqeasd007.SyncIn.entity.AttendanceStatus`)
| Value | Description |
|-------|-------------|
| `PRESENT` | Punched in via QR token |
| `ABSENT` | Defaulter or no punch-in |
| `AL` | Approved Leave (counts as present for health) |
| `UL` | Unapproved Leave (counts as absent for health) |

### LeaveStatus (`com.intqeasd007.SyncIn.entity.LeaveStatus`)
| Value | Description |
|-------|-------------|
| `PENDING` | Awaiting POC action |
| `APPROVED` | POC approved → creates AL attendance |
| `REJECTED` | POC rejected |
| `UNAPPROVED` | POC marked as UL → creates UL attendance |

---

## Quick Test with cURL

```bash
# 1. Admin login
curl -X POST http://localhost:8081/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"sudo","password":"racingcar123"}'

# 2. Create POC (use token from step 1)
curl -X POST http://localhost:8081/admin/create-poc \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{"empId":"POC001","name":"Priya","email":"priya@cog.com","mobileNo":"9876543210","passwordHash":"Welcome@123"}'

# 3. POC login
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"empId":"POC001","password":"Welcome@123"}'

# 4. Create cohort (use POC token)
curl -X POST http://localhost:8081/poc/cohorts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <poc_token>" \
  -d '{"batchCode":"BATCH-01","trackName":"Java Full Stack"}'

# 5. Onboard intern
curl -X POST http://localhost:8081/poc/interns \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <poc_token>" \
  -d '{"empId":"INT001","name":"Kushik","email":"kushik@cog.com","mobileNo":"9999999999","batchCode":"BATCH-01"}'
```

---

*Generated: May 2026 | SyncIn v1.0*

