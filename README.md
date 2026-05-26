# StudentLife API

A scalable academic productivity platform built for Cambodian university students. StudentLife centralizes assignments, real-time group collaboration, schedule management, AI-powered study planning, and push notifications into a single cohesive backend API.

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Features](#features)
- [API Endpoints](#api-endpoints)
- [Authentication](#authentication)
- [Database Schema](#database-schema)
- [Environment Variables](#environment-variables)
- [Getting Started](#getting-started)
- [CI/CD Pipeline](#cicd-pipeline)
- [Deployment](#deployment)

---

## Overview

StudentLife is a Spring Boot REST API that serves as the backend for the [StudentLife Platform](https://student-life-platform.vercel.app). It is designed around the academic workflows of university students — from tracking assignment deadlines to collaborating in real-time group chats and generating AI-powered study schedules.

**Production API:** `https://studentlifeapis.onrender.com`

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.11 |
| Security | Spring Security + JWT (JJWT 0.12.6) |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL (hosted on Neon) |
| Real-time | Spring WebSocket (SockJS + STOMP) |
| Email | Spring Mail (Gmail SMTP) |
| Image Storage | Cloudinary |
| AI Integration | Groq API (study plan generation) |
| Push Notifications | OneSignal |
| Build Tool | Maven |
| Code Generation | Lombok, MapStruct 1.5.5 |
| Containerization | Docker, Docker Compose |
| Code Quality | Qodana |
| CI/CD | GitHub Actions + Render |

---

## Project Structure

```
src/main/java/com/studentlife/StudentLifeAPIs/
├── Config/           # Security, CORS, WebSocket, Cloudinary configuration
├── Controller/       # REST and WebSocket controllers (11 controllers)
├── DTO/
│   ├── Request/      # Inbound request DTOs
│   └── Response/     # Outbound response DTOs
├── Entity/           # JPA entities (12 entities)
├── Enum/             # Status and type enumerations
├── Exception/        # Custom exception classes and global handler
├── Jwt/              # JWT generation, validation, and filter
├── Repository/       # Spring Data JPA repositories
├── Security/         # Auth entry point and WebSocket interceptor
├── Service/
│   └── Impl/         # Service interfaces and implementations (13 services)
└── Utils/            # Utility helpers
```

---

## Features

### Completed
- **User Authentication** — Register, login, logout with JWT access/refresh tokens stored in secure HTTP-only cookies
- **Role-Based Authorization** — Admin, Student, and Teacher roles with `@PreAuthorize` guards
- **Assignment Management** — Full CRUD, progress tracking (0–100%), and overdue status detection
- **Invite System** — Email-based invitation links with expiring tokens; accept/decline flow
- **Real-time Group Chat** — WebSocket-powered messaging scoped to each assignment group
- **Schedule Management** — One-time and recurring calendar events; assignments auto-generate a linked schedule
- **AI Study Plans** — Groq API integration generates a personalized study plan for any assignment
- **Push Notifications** — OneSignal integration for sending and tracking device-level notifications
- **In-app Notifications** — Stored notifications with unread count and mark-all-read support
- **Image Upload** — Profile pictures and assets managed via Cloudinary
- **Email Notifications** — Invite emails and reminders sent via Gmail SMTP
- **Health Check** — `/health` endpoint for uptime monitoring

### In Progress
- **User Device Tracking** — Recording device info per user session for audit and notification targeting

---

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register a new user |
| POST | `/api/v1/auth/login` | Login and receive tokens |
| POST | `/api/v1/auth/refresh-token` | Refresh the access token |
| POST | `/api/v1/auth/logout` | Invalidate session |

### User Management
| Method | Endpoint | Description | Access |
|---|---|---|---|
| GET | `/api/v1/users` | List all users (paginated) | Admin |
| GET | `/api/v1/users/{id}` | Get user by ID | Admin |
| POST | `/api/v1/users` | Create a user | Admin |
| PUT | `/api/v1/users/{id}` | Update a user | Admin or Self |
| PUT | `/api/v1/users/{id}/disable` | Disable a user account | Admin |
| DELETE | `/api/v1/users/{id}` | Delete a user | Admin |
| GET | `/api/v1/me` | Get authenticated user profile | Authenticated |
| PATCH | `/api/v1/me/update-profile` | Update own profile | Authenticated |

### Assignments
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/assignments` | Create an assignment |
| GET | `/api/v1/assignments/my-assignment` | Get current user's assignments |
| GET | `/api/v1/assignments/{id}` | Get assignment details |
| PUT | `/api/v1/assignments/{id}` | Update assignment |
| PATCH | `/api/v1/assignments/{id}/progress` | Update progress percentage |
| DELETE | `/api/v1/assignments/{id}` | Delete assignment |
| POST | `/api/v1/assignments/{id}/invite` | Invite a user to collaborate |
| POST | `/api/v1/assignments/{id}/accept` | Accept an invitation |
| POST | `/api/v1/assignments/{id}/decline` | Decline an invitation |
| GET | `/api/v1/assignments/{id}/members` | List assignment members |

### Schedules
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/schedule/my-schedule` | Get schedules (optional date range filter) |
| GET | `/api/v1/schedule/{id}` | Get schedule by ID |
| POST | `/api/v1/schedule/one-time` | Create a one-time event |
| POST | `/api/v1/schedule/recurring` | Create a recurring event |
| PUT | `/api/v1/schedule/{id}` | Update a schedule |
| DELETE | `/api/v1/schedule/{id}` | Delete a schedule |

### Group Chat
| Type | Destination | Description |
|---|---|---|
| WebSocket | `/api/v1/ws/chat.send` | Send a message to a group |
| WebSocket | `/api/v1/ws/chat.join` | Join a group chat |
| WebSocket | `/api/v1/ws/chat.leave` | Leave a group chat |
| GET | `/api/v1/chat/groups` | List user's groups |
| GET | `/api/v1/chat/{assignmentId}/history` | Get chat message history |
| DELETE | `/api/v1/chat/{assignmentId}/history` | Clear chat history |

### Notifications
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/notification/send` | Send a notification |
| GET | `/api/v1/notification/unread` | Get unread notifications |
| GET | `/api/v1/notification/unread/count` | Get unread count |
| PUT | `/api/v1/notification/mark-all-read` | Mark all as read |

### Study Plan (AI)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/study-plan/{assignmentId}` | Generate AI study plan via Groq |

### Device & Push Notifications
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/register` | Register OneSignal player ID for device |

### Invitations (Email Link)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/invite/accept?token={token}` | Accept invite via email link |
| GET | `/invite/decline?token={token}` | Decline invite via email link |

### Misc
| Method | Endpoint | Description |
|---|---|---|
| GET | `/health` | Server health check |

---

## Authentication

StudentLife uses **JWT-based stateless authentication** with tokens stored in secure HTTP-only cookies.

### Token Lifecycle
| Token | Expiry | Storage |
|---|---|---|
| Access Token | 15 minutes | `accessToken` HTTP-only cookie |
| Refresh Token | 30 days | Database + cookie |

### Flow
1. User registers or logs in — credentials validated with BCrypt
2. Server issues access and refresh tokens via `Set-Cookie` headers
3. All subsequent requests carry the `accessToken` cookie automatically
4. `JwtAuthFilter` extracts, validates, and loads the user into the `SecurityContext`
5. On expiry, the client calls `/api/v1/auth/refresh-token` to get a new access token

### Roles
| Role | Permissions |
|---|---|
| `ROLE_admin` | Full access to all resources |
| `ROLE_student` | Access to own data, assignments, and collaboration features |
| `ROLE_teacher` | Same as student (extendable) |

WebSocket connections are also authenticated via `WebSocketAuthInterceptor` before the handshake is completed.

---

## Database Schema

### Core Entities

| Entity | Description |
|---|---|
| `Users` | Profiles with authentication fields, university info, and OneSignal player ID |
| `Roles` | Role definitions linked many-to-many with Users |
| `Assignments` | Projects/assignments with status (`PENDING`, `IN_PROGRESS`, `COMPLETED`, `OVERDUE`) and progress (0–100) |
| `AssignmentMembers` | Collaboration records with invite token, expiry, and status (`INVITED`, `ACCEPTED`, `DECLINED`) |
| `Schedules` | Calendar events — one-time (start/end time) or recurring (day of week + time) — optionally linked to an assignment |
| `GroupMessage` | Chat messages scoped to an assignment group |
| `GroupChatMember` | Tracks join/leave times per user per group |
| `Notification` | In-app notifications with types: `CHAT`, `ASSIGNMENT`, `INVITE`, `ANNOUNCEMENT`, `REMINDER`, `SCHEDULE`, `SYSTEM` |
| `UserDevices` | Records device info per user session |
| `RefreshToken` | Stores refresh tokens with expiry for validation and rotation |
| `ReminderLog` | Tracks when assignment reminders were last sent |
| `Notes` | User notes (schema present, minimal usage) |

---

## Environment Variables

Create a `.env` file at the project root (see `.env.example` if available):

```env
# Database (Neon PostgreSQL)
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=your_db_password

# JWT
SPRING_JWT_SECRET=your_jwt_secret_key
SPRING_ACCESS_TOKEN_EXPIRE=900000
SPRING_REFRESH_TOKEN_EXPIRE=2592000000

# Cloudinary
CLOUD_NAME=your_cloud_name
API_KEY=your_api_key
API_SECRET=your_api_secret

# Email (Gmail SMTP)
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587

# Admin Account (seeded on startup)
SPRING_ADMIN_USERNAME=@studentlife
SPRING_ADMIN_EMAIL=admin.studentlife@gmail.com
SPRING_ADMIN_PASSWORD=YourAdminPassword

# Groq AI
SPRING_GROQ_API_KEY=your_groq_api_key

# OneSignal Push Notifications
ONESIGNAL_APP_ID=your_app_id
ONESIGNAL_API_KEY=your_api_key

# App URLs
app.frontend.url=https://student-life-platform.vercel.app
app.backend.url=https://studentlifeapis.onrender.com
app.secure-cookie=true
```

---

## Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- Docker & Docker Compose (optional, for local PostgreSQL)
- PostgreSQL instance (or use the Neon connection string)

### Run Locally

```bash
# 1. Clone the repository
git clone https://github.com/salmonineath/StudentLifeAPIs.git
cd StudentLifeAPIs

# 2. Copy and fill in environment variables
cp .env.example .env

# 3. (Optional) Start a local PostgreSQL with Docker
docker-compose up -d

# 4. Build the project
./mvnw clean package -DskipTests

# 5. Run the application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:5000`.

### Run with Docker

```bash
docker build -t studentlife-api .
docker run --env-file .env -p 5000:5000 studentlife-api
```

---

## CI/CD Pipeline

### Continuous Integration (`ci.yml`)
Triggers on push or pull request to `develop` or `main`.

1. Checkout code
2. Set up Java 21 (Temurin)
3. Cache Maven dependencies
4. Build: `./mvnw clean package -DskipTests`
5. Run tests: `./mvnw test`

### Continuous Deployment (`cd.yml`)
Triggers on push to `main` only.

1. Sends a `curl` request to the Render deploy hook
2. Render pulls the latest image and redeploys automatically

---

## Deployment

The application is deployed on **Render** using Docker.

- **Build:** Multi-stage Dockerfile produces a minimal runtime image
- **Database:** Neon serverless PostgreSQL (connection pooling enabled)
- **Secrets:** Managed via GitHub Secrets (CI/CD) and Render environment variables
- **Branches:** `develop` for active development, `main` for production releases

---

## Development Status

| Module | Status |
|---|---|
| Authentication & Authorization | Complete |
| User Management | Complete |
| Assignment CRUD | Complete |
| Invite System (email + token) | Complete |
| Real-time Group Chat (WebSocket) | Complete |
| Schedule Management | Complete |
| AI Study Plan (Groq) | Complete |
| Push Notifications (OneSignal) | Complete |
| In-app Notifications | Complete |
| Email Notifications | Complete |
| Image Upload (Cloudinary) | Complete |
| User Device Tracking | In Progress |
