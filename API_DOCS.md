# StudentLife API — Documentation

> **Base URL** `https://studentlifeapis.onrender.com`
> **Local URL** `http://localhost:8080`

---

## Overview

### Response Envelope

Every REST endpoint (except paginated ones and direct redirects) returns the same wrapper:

```json
{
  "status": 200,
  "success": true,
  "message": "Human-readable message.",
  "data": { }
}
```

When an error occurs:

```json
{
  "status": 404,
  "success": false,
  "message": "Assignment not found.",
  "data": null
}
```

---

### Authentication

The API uses **JWT** stored as **HTTP-only cookies** (`accessToken` + `refreshToken`).

For non-browser clients (e.g. Swagger, Postman, mobile), pass the token in the `Authorization` header:

```
Authorization: Bearer <accessToken>
```

Cookies are set automatically on login/register. The frontend does **not** need to handle tokens manually when using a browser with `credentials: "include"`.

---

### CORS Allowed Origins

| Origin |
|--------|
| `http://localhost:3000` |
| `http://localhost:5173` |
| `https://student-life-platform.vercel.app` |
| `http://127.0.0.1:5500` |

All requests from a browser must include `credentials: "include"` (or `withCredentials: true`).

---

### Common Status Codes

| Code | Meaning |
|------|---------|
| `200` | OK |
| `201` | Created |
| `400` | Bad Request / Validation Error |
| `401` | Unauthenticated — missing or expired token |
| `403` | Forbidden — authenticated but not allowed |
| `404` | Resource not found |
| `422` | Business logic / semantic validation failure |
| `500` | Unexpected server error |

---

## Table of Contents

1. [Auth](#1-auth)
2. [Users (Admin)](#2-users-admin)
3. [Me (Current User)](#3-me-current-user)
4. [Assignments](#4-assignments)
5. [Schedules](#5-schedules)
6. [Group Chat — REST](#6-group-chat--rest)
7. [Group Chat — WebSocket](#7-group-chat--websocket)
8. [Notifications](#8-notifications)
9. [Study Plan (AI)](#9-study-plan-ai)
10. [Invites (Email Link)](#10-invites-email-link)
11. [Push Notifications (OneSignal)](#11-push-notifications-onesignal)
12. [Devices](#12-devices)
13. [Health Check](#13-health-check)

---

## 1. Auth

### 1.1 Register

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/auth/register` |
| **Auth** | None |

**Request Body**

```json
{
  "fullname": "Mony Dara",
  "username": "monydara",
  "email": "monydara@example.com",
  "password": "secret123"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `fullname` | string | Yes | Display name |
| `username` | string | Yes | Must be unique |
| `email` | string | Yes | Must be unique, valid email |
| `password` | string | Yes | Plain text — hashed server-side |

**Response** `201`

```json
{
  "status": 201,
  "success": true,
  "message": "Register successful.",
  "data": {
    "accessToken": "eyJhbGci...",
    "user": {
      "id": 1,
      "fullname": "Mony Dara",
      "username": "monydara",
      "email": "monydara@example.com",
      "phone": null,
      "university": null,
      "major": null,
      "academicYear": null,
      "roles": ["student"],
      "createdAt": "2026-05-26T07:00:00Z",
      "updatedAt": "2026-05-26T07:00:00Z"
    }
  }
}
```

**Notes**
- Sets `accessToken` and `refreshToken` as HTTP-only cookies automatically.
- New accounts are assigned the `student` role by default.
- Returns `422` if email or username is already taken.

---

### 1.2 Login

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/auth/login` |
| **Auth** | None |

**Request Body**

```json
{
  "email_or_username": "monydara@example.com",
  "password": "secret123"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `email_or_username` | string | Yes | Accepts either email or username |
| `password` | string | Yes | |

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGci...",
    "user": {
      "id": 1,
      "fullname": "Mony Dara",
      "username": "monydara",
      "email": "monydara@example.com",
      "phone": null,
      "university": null,
      "major": null,
      "academicYear": null,
      "roles": ["student"],
      "createdAt": "2026-05-26T07:00:00Z",
      "updatedAt": "2026-05-26T07:00:00Z"
    }
  }
}
```

**Notes**
- Sets `accessToken` and `refreshToken` cookies automatically.
- Returns `422` if credentials are invalid.
- Device info is tracked silently on login (does not affect the response).

---

### 1.3 Refresh Token

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/auth/refresh-token` |
| **Auth** | Refresh token cookie (sent automatically) |

**Request Body** — None

**Response** `200`

```json
{
  "status": 201,
  "success": true,
  "message": "New access token generate successfully.",
  "data": {
    "accessToken": "eyJhbGci..."
  }
}
```

**Notes**
- Reads `refreshToken` cookie automatically — no body needed.
- Issues a new `accessToken` + `refreshToken` pair (old refresh token is rotated/deleted).
- Returns `401` if the refresh token is missing, expired, or revoked.
- Call this when you receive a `401` on any authenticated endpoint.

---

### 1.4 Logout

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/auth/logout` |
| **Auth** | Refresh token cookie (optional) |

**Request Body** — None

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "User logout successfully.",
  "data": null
}
```

**Notes**
- Clears `accessToken` and `refreshToken` cookies.
- Deletes the refresh token from the database (invalidates the session server-side).
- Safe to call even if no session exists.

---

## 2. Users (Admin)

> All endpoints in this group require the `admin` role.

### 2.1 Get All Users (Paginated)

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/users` |
| **Auth** | Required — `admin` role |

**Query Parameters**

| Param | Type | Default | Notes |
|-------|------|---------|-------|
| `page` | int | `0` | Zero-based page index |
| `size` | int | `10` | Items per page |
| `search` | string | — | Optional. Filters by name, username, or email (case-insensitive, partial match) |
| `role` | string | — | Optional. Filters by role. Use `"student"` or `"admin"`. Omit or pass `"all"` to return all roles |

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Get users successfully.",
  "data": {
    "items": [
      {
        "id": 1,
        "fullname": "Mony Dara",
        "username": "monydara",
        "email": "monydara@example.com",
        "phone": "+85512345678",
        "university": "RUPP",
        "major": "Computer Science",
        "academicYear": "Year 3",
        "roles": ["student"],
        "createdAt": "2026-05-26T07:00:00Z",
        "updatedAt": "2026-05-26T07:00:00Z"
      }
    ],
    "pagination": {
      "currentPage": 1,
      "pageSize": 10,
      "totalElements": 42,
      "totalPages": 5,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

---

### 2.2 Get User by ID

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/users/{id}` |
| **Auth** | Required — `admin` role |

**Path Parameters**

| Param | Type | Notes |
|-------|------|-------|
| `id` | Long | User ID |

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "User retrieved successfully.",
  "data": {
    "id": 1,
    "fullname": "Mony Dara",
    "username": "monydara",
    "email": "monydara@example.com",
    "phone": null,
    "university": null,
    "major": null,
    "academicYear": null,
    "roles": ["student"],
    "createdAt": "2026-05-26T07:00:00Z",
    "updatedAt": "2026-05-26T07:00:00Z"
  }
}
```

---

### 2.3 Create User

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/users` |
| **Auth** | Required — `admin` role |

**Request Body**

```json
{
  "fullname": "Sopheap Rath",
  "username": "sopheap",
  "email": "sopheap@example.com",
  "password": "password123"
}
```

**Response** `200`

```json
{
  "status": 201,
  "success": true,
  "message": "User created successfully.",
  "data": { ... }
}
```

---

### 2.4 Update User

| | |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/v1/users/{id}` |
| **Auth** | Required — `admin` role **or** the user themselves |

**Path Parameters** — `id`: User ID

**Request Body** (all fields optional)

```json
{
  "fullname": "Mony Dara Updated",
  "phone": "+85598765432",
  "university": "RUPP",
  "major": "Computer Science",
  "academic_year": "Year 4"
}
```

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "User updated successfully.",
  "data": { ... }
}
```

---

### 2.5 Disable User

| | |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/v1/users/{id}/disable` |
| **Auth** | Required — `admin` role |

**Path Parameters** — `id`: User ID

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "User disabled successfully.",
  "data": null
}
```

**Notes**
- Sets `isActive = false`. The user can no longer log in.
- Returns `400` if the user is already disabled.

---

### 2.6 Delete User

| | |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/v1/users/{id}` |
| **Auth** | Required — `admin` role |

**Path Parameters** — `id`: User ID

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "User permanently deleted.",
  "data": null
}
```

**Notes** — Permanent, irreversible deletion.

---

## 3. Me (Current User)

### 3.1 Get My Profile

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/me` |
| **Auth** | Required |

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Get auth user successfully.",
  "data": {
    "id": 1,
    "fullname": "Mony Dara",
    "username": "monydara",
    "email": "monydara@example.com",
    "phone": "+85512345678",
    "university": "RUPP",
    "major": "Computer Science",
    "academicYear": "Year 3",
    "roles": ["student"],
    "createdAt": "2026-05-26T07:00:00Z",
    "updatedAt": "2026-05-26T07:00:00Z"
  }
}
```

---

### 3.2 Update My Profile

| | |
|---|---|
| **Method** | `PATCH` |
| **URL** | `/api/v1/me/update-profile` |
| **Auth** | Required |

**Request Body** (all fields optional — only provided fields are updated)

```json
{
  "fullname": "Mony Dara",
  "phone": "+85512345678",
  "university": "RUPP",
  "major": "Computer Science",
  "academic_year": "Year 3"
}
```

| Field | Type | Notes |
|-------|------|-------|
| `fullname` | string | Display name |
| `phone` | string | Phone number |
| `university` | string | University name |
| `major` | string | Major/faculty |
| `academic_year` | string | e.g. `"Year 3"` |

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Update profile successfully.",
  "data": { ... }
}
```

---

## 4. Assignments

### 4.1 Create Assignment

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/assignments` |
| **Auth** | Required |

**Request Body**

```json
{
  "title": "Math Final Report",
  "description": "Chapter 5–8 summary",
  "subject": "Mathematics",
  "startDate": "2026-06-01T08:00:00",
  "dueDate": "2026-06-20T23:59:00"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `title` | string | Yes | |
| `description` | string | No | |
| `subject` | string | Yes | |
| `startDate` | datetime | Yes | ISO 8601 format |
| `dueDate` | datetime | Yes | Must be in the future |

**Response** `201`

```json
{
  "status": 201,
  "success": true,
  "message": "Assignment created successfully.",
  "data": {
    "id": 10,
    "title": "Math Final Report",
    "description": "Chapter 5–8 summary",
    "subject": "Mathematics",
    "startDate": "2026-06-01T08:00:00",
    "dueDate": "2026-06-20T23:59:00",
    "status": "PENDING",
    "progress": 0,
    "scheduleId": 5,
    "createdAt": "2026-05-26T07:00:00Z",
    "updatedAt": "2026-05-26T07:00:00Z"
  }
}
```

**Notes**
- Automatically creates a linked schedule entry on the calendar.
- `scheduleId` in the response points to that auto-created schedule.
- `status` is always `PENDING` on creation.

---

### 4.2 Get My Assignments

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/assignments/my-assignment` |
| **Auth** | Required |

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Get all assignment successfully.",
  "data": [
    {
      "id": 10,
      "title": "Math Final Report",
      "description": "Chapter 5–8 summary",
      "subject": "Mathematics",
      "startDate": "2026-06-01T08:00:00",
      "dueDate": "2026-06-20T23:59:00",
      "status": "IN_PROGRESS",
      "progress": 40,
      "scheduleId": 5,
      "createdAt": "2026-05-26T07:00:00Z",
      "updatedAt": "2026-05-27T10:00:00Z"
    }
  ]
}
```

**Notes**
- Returns assignments the user **owns** AND assignments they have been **invited and accepted**.
- `status` values: `PENDING` | `IN_PROGRESS` | `COMPLETED` | `OVERDUE`

---

### 4.3 Get Assignment by ID

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/assignments/{id}` |
| **Auth** | Required |

**Path Parameters** — `id`: Assignment ID

**Response** `200` — Same shape as a single item in `4.2`.

**Notes**
- Returns `403` if the user is not the owner and has not accepted an invitation.

---

### 4.4 Update Assignment

| | |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/v1/assignments/{id}` |
| **Auth** | Required — owner only |

**Path Parameters** — `id`: Assignment ID

**Request Body** — Same fields as [Create Assignment](#41-create-assignment)

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Assignment update successfully.",
  "data": { ... }
}
```

**Notes** — Only the assignment **owner** can update. Members get `403`.

---

### 4.5 Update Progress

| | |
|---|---|
| **Method** | `PATCH` |
| **URL** | `/api/v1/assignments/{id}/progress` |
| **Auth** | Required — owner only |

**Path Parameters** — `id`: Assignment ID

**Request Body**

```json
{
  "progress": 75
}
```

| Field | Type | Notes |
|-------|------|-------|
| `progress` | int | 0–100 (inclusive) |

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Progress updated successfully.",
  "data": {
    "id": 10,
    "progress": 75,
    "status": "IN_PROGRESS",
    ...
  }
}
```

**Notes**
- Status is derived automatically:
  - `progress = 0` → `PENDING`
  - `1–99` → `IN_PROGRESS`
  - `100` → `COMPLETED`

---

### 4.6 Delete Assignment

| | |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/v1/assignments/{id}` |
| **Auth** | Required — owner only |

**Path Parameters** — `id`: Assignment ID

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Assignment deleted successfully.",
  "data": null
}
```

**Notes** — Also deletes the linked schedule automatically.

---

### 4.7 Invite a Member

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/assignments/{assignmentId}/invite` |
| **Auth** | Required — owner only |

**Path Parameters** — `assignmentId`: Assignment ID

**Request Body**

```json
{
  "email": "sopheap@example.com"
}
```

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Invitation sent successfully.",
  "data": null
}
```

**Notes**
- Sends an invitation email with **Accept / Decline** links (valid for 7 days).
- Sends an in-app notification to the invited user.
- Returns `404` if no user exists with that email.
- Returns `422` if the user is already invited or if you try to invite yourself.

---

### 4.8 Accept Invite (API)

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/assignments/{assignmentId}/accept` |
| **Auth** | Required — invited user |

**Path Parameters** — `assignmentId`: Assignment ID

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Invite accepted successfully.",
  "data": null
}
```

**Notes**
- Adds the user to the group chat.
- Creates a schedule entry for the assignment on the user's calendar.
- Notifies the owner via email and in-app notification.
- Returns `422` if already responded.

---

### 4.9 Decline Invite (API)

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/assignments/{assignmentId}/decline` |
| **Auth** | Required — invited user |

**Path Parameters** — `assignmentId`: Assignment ID

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Invite declined successfully.",
  "data": null
}
```

---

### 4.10 Get Assignment Members

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/assignments/{assignmentId}/members` |
| **Auth** | Required — owner only |

**Path Parameters** — `assignmentId`: Assignment ID

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Get all members successfully",
  "data": [
    {
      "id": 3,
      "userId": 7,
      "fullname": "Sopheap Rath",
      "email": "sopheap@example.com",
      "status": "ACCEPTED"
    }
  ]
}
```

**Notes**
- Only returns members with `ACCEPTED` status.
- `status` values: `INVITED` | `ACCEPTED` | `DECLINED`

---

## 5. Schedules

### 5.1 Get My Schedules

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/schedule/my-schedule` |
| **Auth** | Required — `student` role |

**Query Parameters**

| Param | Type | Required | Notes |
|-------|------|----------|-------|
| `startDate` | date | No | Format: `YYYY-MM-DD` |
| `endDate` | date | No | Format: `YYYY-MM-DD` |

**Example Requests**

```
GET /api/v1/schedule/my-schedule                                        → all schedules
GET /api/v1/schedule/my-schedule?startDate=2026-06-01&endDate=2026-06-30 → monthly view
GET /api/v1/schedule/my-schedule?startDate=2026-06-09&endDate=2026-06-15 → weekly view
GET /api/v1/schedule/my-schedule?startDate=2026-06-09&endDate=2026-06-09 → daily view
```

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Schedules retrieved successfully.",
  "data": [
    {
      "id": 5,
      "title": "Math Final Report",
      "description": "Chapter 5–8 summary",
      "type": "ONE_TIME",
      "startTime": "2026-06-01T08:00:00",
      "endTime": "2026-06-20T23:59:00",
      "dayOfWeek": null,
      "recurringStartTime": null,
      "recurringEndTime": null,
      "location": null,
      "isImportant": true,
      "createdBy": {
        "id": 1,
        "fullname": "Mony Dara",
        "username": "monydara"
      }
    },
    {
      "id": 8,
      "title": "Weekly Study Group",
      "description": null,
      "type": "RECURRING",
      "startTime": null,
      "endTime": null,
      "dayOfWeek": 1,
      "recurringStartTime": "09:00:00",
      "recurringEndTime": "11:00:00",
      "location": "Library Room 3",
      "isImportant": false,
      "createdBy": { ... }
    }
  ]
}
```

**Notes**
- `type: "ONE_TIME"` — uses `startTime` / `endTime`. `dayOfWeek`, `recurringStartTime`, `recurringEndTime` are `null`.
- `type: "RECURRING"` — uses `dayOfWeek` / `recurringStartTime` / `recurringEndTime`. `startTime`, `endTime` are `null`.
- With date params: **RECURRING** schedules always appear; **ONE_TIME** only appears if it overlaps the range.
- Without date params: all schedules are returned.
- `dayOfWeek`: `0` = Sunday, `1` = Monday … `6` = Saturday.

---

### 5.2 Get Schedule by ID

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/schedule/{scheduleId}` |
| **Auth** | Required |

**Path Parameters** — `scheduleId`: Schedule ID

**Response** `200` — Single schedule object (same shape as array items in `5.1`).

**Notes** — Returns `403` if the schedule doesn't belong to the current user.

---

### 5.3 Create One-Time Schedule

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/schedule/one-time` |
| **Auth** | Required — `student` role |

**Request Body**

```json
{
  "title": "Doctor Appointment",
  "description": "Annual checkup",
  "startTime": "2026-06-15T09:00:00",
  "endTime": "2026-06-15T10:30:00",
  "location": "City Hospital",
  "isImportant": false
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `title` | string | Yes | |
| `description` | string | No | |
| `startTime` | datetime | Yes | ISO 8601 |
| `endTime` | datetime | Yes | Must be after `startTime` |
| `location` | string | No | |
| `isImportant` | boolean | No | Defaults to `false` |

**Response** `201`

```json
{
  "status": 201,
  "success": true,
  "message": "Schedule created successfully.",
  "data": { ... }
}
```

**Notes** — Also sends a "Schedule Created" in-app notification to the creator.

---

### 5.4 Create Recurring Schedule

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/schedule/recurring` |
| **Auth** | Required — `student` role |

**Request Body**

```json
{
  "title": "Morning Lecture",
  "description": "Algorithm Design",
  "dayOfWeek": 1,
  "recurringStartTime": "08:00:00",
  "recurringEndTime": "10:00:00",
  "location": "Room A101",
  "isImportant": true
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `title` | string | Yes | |
| `description` | string | No | |
| `dayOfWeek` | int | Yes | 0 (Sun) – 6 (Sat) |
| `recurringStartTime` | time | Yes | `HH:mm:ss` |
| `recurringEndTime` | time | Yes | Must be after `recurringStartTime` |
| `location` | string | No | |
| `isImportant` | boolean | No | Defaults to `false` |

**Response** `201` — Same shape as `5.3`.

---

### 5.5 Update Schedule

| | |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/v1/schedule/{scheduleId}` |
| **Auth** | Required — owner only |

**Path Parameters** — `scheduleId`: Schedule ID

**Request Body** (all fields optional — only provided fields are updated)

```json
{
  "title": "Updated Title",
  "description": "Updated description",
  "location": "New Room",
  "isImportant": true,
  "startTime": "2026-06-15T10:00:00",
  "endTime": "2026-06-15T12:00:00"
}
```

**Notes**
- `startTime` / `endTime` apply only if the schedule is `ONE_TIME`.
- `dayOfWeek` / `recurringStartTime` / `recurringEndTime` apply only if `RECURRING`.
- Passing the wrong type's fields has no effect.
- Also sends a "Schedule Updated" in-app notification.

**Response** `200` — Updated schedule object.

---

### 5.6 Delete Schedule

| | |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/v1/schedule/{scheduleId}` |
| **Auth** | Required — owner only |

**Path Parameters** — `scheduleId`: Schedule ID

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Schedule deleted successfully.",
  "data": null
}
```

---

## 6. Group Chat — REST

### 6.1 Get My Groups

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/chat/groups` |
| **Auth** | Required |

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Group retrieved successfully",
  "data": [
    {
      "assignmentId": 10,
      "assignmentTitle": "Math Final Report",
      "subject": "Mathematics",
      "ownerName": "Mony Dara",
      "ownerUsername": "monydara",
      "memberCount": 3,
      "lastMessage": "Did everyone finish chapter 5?",
      "lastMessageTime": "2026-05-26T14:30:00Z",
      "lastMessageSender": "Sopheap Rath"
    }
  ]
}
```

**Notes**
- Returns groups where the user is the **owner** OR an **accepted member**.
- `lastMessage` is truncated to 50 characters with `…` appended if longer.
- `lastMessageTime` / `lastMessageSender` are `null` if no messages exist yet.

---

### 6.2 Get Chat History

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/chat/{assignmentId}/history` |
| **Auth** | Required |

**Path Parameters** — `assignmentId`: Assignment ID

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Chat history retrieved.",
  "data": [
    {
      "id": 101,
      "assignmentId": 10,
      "senderId": 1,
      "senderFullname": "Mony Dara",
      "senderUsername": "monydara",
      "content": "Did everyone finish chapter 5?",
      "createdAt": "2026-05-26T14:30:00Z"
    }
  ]
}
```

**Notes**
- Messages are ordered **oldest first** (ascending `createdAt`).
- Returns `403` if the user is not the owner or an accepted member.
- Messages older than **5 days** are automatically deleted by the server.

---

### 6.3 Get Group Members

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/chat/{assignmentId}/members` |
| **Auth** | Required |

**Path Parameters** — `assignmentId`: Assignment ID

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Get group member successfully",
  "data": [
    {
      "id": 1,
      "fullname": "Mony Dara",
      "username": "monydara",
      "email": "monydara@example.com",
      "university": "RUPP",
      "major": "Computer Science",
      "academicYear": "Year 3",
      "online": true
    },
    {
      "id": 7,
      "fullname": "Sopheap Rath",
      "username": "sopheap",
      "email": "sopheap@example.com",
      "university": "RUPP",
      "major": "IT",
      "academicYear": "Year 3",
      "online": false
    }
  ]
}
```

**Notes**
- The **owner** is always first in the list.
- `online` reflects real-time WebSocket presence (see Section 7).

---

### 6.4 Clear Chat History

| | |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/v1/chat/{assignmentId}/history` |
| **Auth** | Required — owner only |

**Path Parameters** — `assignmentId`: Assignment ID

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Chat history cleared",
  "data": null
}
```

**Notes** — Permanently deletes **all** messages in the group. Only the assignment owner can do this.

---

## 7. Group Chat — WebSocket

### Connection

**WebSocket Endpoint** `wss://studentlifeapis.onrender.com/api/v1/ws` (SockJS)

**Protocol** STOMP over SockJS

**Authentication** — Send the JWT in the STOMP CONNECT frame:

```
CONNECT
Authorization: Bearer <accessToken>
```

---

### 7.1 Subscribe to Group Messages

**Destination** `/topic/group/{assignmentId}`

Subscribe to this topic **after** joining to receive all new messages in real time.

**Received Message**

```json
{
  "id": 102,
  "assignmentId": 10,
  "senderId": 1,
  "senderFullname": "Mony Dara",
  "senderUsername": "monydara",
  "content": "Let's meet at 3pm.",
  "createdAt": "2026-05-26T14:35:00Z"
}
```

---

### 7.2 Subscribe to Presence Events

**Destination** `/topic/group/{assignmentId}/presence`

Subscribe to receive real-time online/offline presence updates.

**Received Message**

```json
{
  "assignmentId": 10,
  "onlineCount": 2,
  "onlineUserIds": [1, 7]
}
```

---

### 7.3 Subscribe to Personal Notifications

**Destination** `/queue/notifications/{userId}`

Subscribe to receive real-time in-app notifications for the current user.

**Received Message** — Same shape as [Notification Response](#8-notifications).

```json
{
  "id": 55,
  "recipientId": 1,
  "title": "Mony Dara",
  "message": "Let's meet at 3pm.",
  "type": "CHAT",
  "isRead": false,
  "createdAt": "2026-05-26T14:35:00Z",
  "referenceId": 10,
  "link": "/assignments/10"
}
```

---

### 7.4 Send a Message

**Send To** `/app/chat.send`

```json
{
  "assignmentId": 10,
  "content": "Let's meet at 3pm."
}
```

**Notes**
- The response is broadcast to `/topic/group/{assignmentId}` — you receive it via your subscription.
- Triggers 3 notification layers: WebSocket (real-time), in-app DB notification, and OneSignal push.
- Returns `403` if the sender is not the owner or an accepted member.

---

### 7.5 Join Group (Presence)

**Send To** `/app/chat.join`

```json
{
  "assignmentId": 10,
  "content": ""
}
```

**Notes**
- Marks the user as online for that group.
- Broadcasts a presence update to `/topic/group/{assignmentId}/presence`.
- Call this when the user **opens** the group chat screen.

---

### 7.6 Leave Group (Presence)

**Send To** `/app/chat.leave`

```json
{
  "assignmentId": 10,
  "content": ""
}
```

**Notes**
- Marks the user as offline for that group.
- Call this when the user **closes** or navigates away from the group chat screen.

---

## 8. Notifications

### 8.1 Send Notification to Self

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/notification/send` |
| **Auth** | Required |

**Query Parameters**

| Param | Type | Required | Values |
|-------|------|----------|--------|
| `type` | string | Yes | `CHAT` \| `ASSIGNMENT` \| `INVITE` \| `ANNOUNCEMENT` \| `REMINDER` \| `SCHEDULE` \| `SYSTEM` |

**Request Body**

```json
{
  "title": "Reminder",
  "message": "Your assignment is due tomorrow.",
  "referenceId": 10,
  "link": "/assignments/10"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `title` | string | Yes | |
| `message` | string | Yes | |
| `referenceId` | number | No | Id of the entity the notification refers to (e.g. assignment id). Omit/`null` if not applicable. |
| `link` | string | No | Explicit **relative** in-app path to open, e.g. `/assignments/10`. Must start with `/` and must **not** be an absolute URL (`http://...`, `//host`) — such values are rejected with `400`. |

**Response** `201`

```json
{
  "status": 201,
  "success": true,
  "message": "Message sent successfully.",
  "data": {
    "id": 55,
    "recipientId": 1,
    "title": "Reminder",
    "message": "Your assignment is due tomorrow.",
    "type": "SYSTEM",
    "isRead": false,
    "createdAt": "2026-05-26T14:00:00Z",
    "referenceId": 10,
    "link": "/assignments/10"
  }
}
```

---

### 8.2 Get Unread Notifications

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/notification/unread` |
| **Auth** | Required |

**Response** `200`

```json
[
  {
    "id": 55,
    "recipientId": 1,
    "title": "Mony Dara",
    "message": "Let's meet at 3pm.",
    "type": "CHAT",
    "isRead": false,
    "createdAt": "2026-05-26T14:35:00Z",
    "referenceId": 10,
    "link": "/assignments/10"
  }
]
```

**Notes**
- Returns only unread notifications for the currently authenticated user.
- `referenceId` / `link` are **deep-linking** fields. Both are optional and may be `null`. Prefer `referenceId` (the frontend builds `/<section>/<referenceId>` from `type`); use `link` as-is when present (it overrides the derived route). `link` is always a relative in-app path.

---

### 8.3 Get Unread Count

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/notification/unread/count` |
| **Auth** | Required |

**Response** `200`

```json
7
```

**Notes** — Returns a plain integer. Use this for the notification badge.

---

### 8.4 Mark All as Read

| | |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/v1/notification/mark-all-read` |
| **Auth** | Required |

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "All notifications marked as read.",
  "data": null
}
```

---

## 9. Study Plan (AI)

### 9.1 Generate Study Plan

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/study-plan/{assignmentId}` |
| **Auth** | Required — owner only |

**Path Parameters** — `assignmentId`: Assignment ID

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Study plan generated successfully.",
  "data": {
    "assignmentId": 10,
    "plan": "- Day 1–2: Read through the assignment requirements and gather reference materials.\n- Day 3–4: Draft the main sections and complete research.\n- Day 5–6: Write the full report draft.\n- Day 7: Review, edit, and finalize submission."
  }
}
```

**Notes**
- Powered by **Groq AI** (Llama 3.1 model).
- Returns `403` if the user is not the assignment owner.
- The plan is generated dynamically based on the assignment's title, subject, due date, and description.
- This is a live API call — response time may vary (typically 2–5 seconds).
- The plan is **not** saved to the database — generate it on demand each time.

---

## 10. Invites (Email Link)

These endpoints are for **email link handling** — the links embedded in invitation emails. The frontend receives the user after a redirect from these endpoints.

### 10.1 Accept Invite via Token

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/invite/accept` |
| **Auth** | None (token-based) |

**Query Parameters**

| Param | Type | Required | Notes |
|-------|------|----------|-------|
| `token` | string | Yes | UUID from the invitation email |

**Response** — Redirects to the frontend:

| Outcome | Redirect URL |
|---------|-------------|
| Accepted | `{FRONTEND_URL}/invite/result?status=accepted&assignmentId=10` |
| Token invalid | `{FRONTEND_URL}/invite/result?status=invalid` |
| Token expired | `{FRONTEND_URL}/invite/result?status=expired` |
| Already responded | `{FRONTEND_URL}/invite/result?status=already_responded` |

**Notes**
- Tokens expire after **7 days**.
- On accept: adds user to group chat, creates a calendar entry, notifies the owner.

---

### 10.2 Decline Invite via Token

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/invite/decline` |
| **Auth** | None (token-based) |

**Query Parameters** — Same as `10.1`

**Response** — Redirects to the frontend:

| Outcome | Redirect URL |
|---------|-------------|
| Declined | `{FRONTEND_URL}/invite/result?status=declined&assignmentId=10` |
| Token invalid | `{FRONTEND_URL}/invite/result?status=invalid` |
| Token expired | `{FRONTEND_URL}/invite/result?status=expired` |
| Already responded | `{FRONTEND_URL}/invite/result?status=already_responded` |

---

## 11. Push Notifications (OneSignal)

### 11.1 Register Player ID

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/register` |
| **Auth** | Required |

**Request Body**

```json
{
  "playerId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `playerId` | string | Yes | OneSignal Player/Subscription ID |

**Response** `200`

```json
{
  "message": "Player ID registered."
}
```

**Notes**
- Call this once after the user grants push notification permission in the browser/app.
- The stored Player ID is used to send push notifications whenever the user receives a message, assignment reminder, or invite.
- Returns `400` if `playerId` is missing or blank.
- Outgoing pushes carry the deep-linking fields in OneSignal's `data` object so click-through can route to the right item: `data.referenceId` (string) and `data.link` (relative path). Either may be absent. Mirror the in-app routing logic: prefer `link`, then `referenceId`, then the section fallback.

---

## 12. Devices

### 12.1 Get My Devices

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/me/devices` |
| **Auth** | Required |

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Devices retrieved successfully.",
  "data": [
    {
      "id": 1,
      "deviceId": "ua-3f4a1b2c",
      "deviceType": "DESKTOP",
      "deviceName": "Chrome",
      "os": "Windows",
      "browser": "Chrome",
      "ipAddress": "203.0.113.45",
      "firstSeenAt": "2026-05-20T10:00:00Z",
      "lastSeenAt": "2026-05-26T07:00:00Z",
      "active": true
    }
  ]
}
```

**Notes**
- Devices are automatically registered on every **login**.
- `deviceType`: `MOBILE` | `TABLET` | `DESKTOP`

---

### 12.2 Remove Device

| | |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/v1/me/devices/{deviceId}` |
| **Auth** | Required |

**Path Parameters** — `deviceId`: Device record ID (from the list above, not the string `deviceId`)

**Response** `200`

```json
{
  "status": 200,
  "success": true,
  "message": "Device is remove successfully.",
  "data": null
}
```

**Notes**
- Sets `active = false` on the device — does not hard-delete the record.
- Returns `403` if the device doesn't belong to the current user.

---

## 13. Health Check

### 13.1 Server Health

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/health` |
| **Auth** | None |

**Response** `200`

```
Health check run. Server is running.
```

**Notes** — Plain text response. Used by the keep-alive scheduler to prevent Render free-tier spin-down.

---

## Appendix

### Assignment Status Values

| Value | Meaning |
|-------|---------|
| `PENDING` | Not started (progress = 0) |
| `IN_PROGRESS` | Work in progress (progress 1–99) |
| `COMPLETED` | Finished (progress = 100) |
| `OVERDUE` | Past due date without completion |

### Notification Type Values

| Value | Trigger |
|-------|---------|
| `CHAT` | New group chat message |
| `ASSIGNMENT` | Assignment-related event |
| `INVITE` | Group invitation sent/accepted/declined |
| `ANNOUNCEMENT` | Admin announcement |
| `REMINDER` | Automated deadline reminder (72h / 24h / 2h) |
| `SCHEDULE` | Schedule created or updated |
| `SYSTEM` | Manual system notification |

### Reminder Email Schedule

The server automatically sends deadline reminder emails at these windows:

| Window | Trigger |
|--------|---------|
| 72 hours | ~3 days before due date |
| 24 hours | ~1 day before due date |
| 2 hours | ~2 hours before due date |

Each reminder is sent **once only** per assignment per window (idempotent).

### Datetime Formats

| Type | Format | Example |
|------|--------|---------|
| Datetime | ISO 8601 (no timezone) | `"2026-06-20T23:59:00"` |
| Date | ISO 8601 | `"2026-06-20"` |
| Time | `HH:mm:ss` | `"09:00:00"` |
| Timestamp | ISO 8601 UTC | `"2026-05-26T07:00:00Z"` |
