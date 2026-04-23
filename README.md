# 🚀 Expiry Tracker

AI-powered food expiry management system that helps users track expiring items and generate recipes using AI.

---

# 🧠 Overview

Expiry Tracker is a full-stack application that combines:

* 📸 AI Vision (scan expiry date from images)
* 🗓 Expiry tracking & decision engine
* 🍳 Recipe recommendation (AI + DB hybrid)
* ⏰ Automated reminder system (cron job)
* 📧 Email testing via MailHog (development)
* 🔐 JWT-based authentication (stateless)

---

# 🏗 Architecture

```text id="arch1"
client/   → React + Vite + Tailwind
server/   → Spring Boot (API + AI logic)
.env      → Environment variables
docker-compose.* → Dev / Prod environments
```

System flow:

```text id="arch2"
Frontend (JWT)
    ↓
Backend (Spring Boot)
    ↓
MySQL
    ↓
OpenAI API (Vision + Recipe)
    ↓
MailHog (Email testing)
```

---

# ⚙️ Features

## 📸 AI Expiry Scanning

* Upload 1–2 images of product packaging
* Extract expiry date using OpenAI Vision
* Handles blurry / partial / missing data
* Returns confidence + decision status

---

## 🧠 Expiry Decision Engine

* Classifies results into:

  * ✅ CONFIRMED
  * ⚠️ REVIEW
  * ❌ REJECTED
* Validates date format & plausibility
* Prevents incorrect AI outputs

---

## 📦 Item Management

* Add / delete / consume items
* Expiry status:

  * Fresh
  * Expiring soon
  * Expired
* Dashboard with server-side filtering, sorting, pagination

---

## 🍳 Recipe Recommendation (Hybrid AI + DB)

* Uses **expiring ingredients (≤ 3 days)** as priority
* Strategy:

  1. Reuse recipes from database
  2. Fallback to AI generation
* Ensures recipes include expiring items
* Filters non-cookable items (snacks, ready meals)

---

## ⏰ Reminder System

* Daily cron job
* Detects expiring items
* Sends notifications

---

## 📧 Email Testing (MailHog)

```text id="mailhog"
http://localhost:8025
```

---

## 🔐 Authentication (JWT)

* Login returns JWT token
* Token stored in localStorage (frontend)
* Sent via Authorization header:

```text id="auth1"
Bearer <token>
```

* Backend validates token via filter
* Uses SecurityContext for user context

Protected APIs:

* Products
* Recipes
* Notifications
* Profile
* Scan (prevent abuse)

---

# 🛠 Tech Stack

## Frontend

* React
* Vite
* Tailwind CSS
* React Router (HashRouter)
* react-hot-toast

## Backend

* Spring Boot
* JPA / Hibernate
* MySQL
* Spring Security (JWT)
* OpenAI API (Responses API)

## Tools

* IntelliJ IDEA
* EnvFile Plugin
* Docker
* MailHog

---

# 🚀 Getting Started (IntelliJ - Recommended)

## 1. Clone project

```bash id="clone"
git clone <repo-url>
cd expiry-tracker
```

---

## 2. Setup Environment Variables

Create `.env` in project root:

```env id="env"
JWT_SECRET=your-secret-key
OPENAI_API_KEY=your-openai-key
MYSQL_ROOT_PASSWORD=root
```

⚠️ Do NOT commit `.env`

---

## 3. Install IntelliJ Plugin

Install:

```text id="plugin"
EnvFile
```

---

## 4. Configure Run (Backend)

* Run → Edit Configurations
* Enable:

```text id="envfile"
☑ Enable EnvFile
```

* Add `.env`
* Leave other options OFF

---

## 5. Run Backend

Run Spring Boot from IntelliJ

---

## 6. Run Frontend

```bash id="frontend"
cd client
npm install
npm run dev
```

---

## 7. Access

* Frontend: http://localhost:5173
* Backend: http://localhost:8080
* MailHog: http://localhost:8025

---

# 🐳 Docker Setup

## 📦 Dev Mode (Recommended for Development)

```bash id="dev1"
docker compose -f docker-compose.dev.yml up
```

Background:

```bash id="dev2"
docker compose -f docker-compose.dev.yml up -d
```

Stop:

```bash id="dev3"
docker compose -f docker-compose.dev.yml down
```

Reset DB:

```bash id="dev4"
docker compose -f docker-compose.dev.yml down -v
```

---

## 🏭 Prod Mode (Simulate Production)

```bash id="prod1"
docker compose -f docker-compose.prod.yml up --build
```

Background:

```bash id="prod2"
docker compose -f docker-compose.prod.yml up -d --build
```

Stop:

```bash id="prod3"
docker compose -f docker-compose.prod.yml down
```

---

# 🌐 Services

| Service  | URL                   |
| -------- | --------------------- |
| Backend  | http://localhost:8080 |
| Frontend | http://localhost:5173 |
| MailHog  | http://localhost:8025 |

---

# 🔐 Security Design

## JWT Flow

```text id="jwtflow"
Login → JWT → stored in frontend
    ↓
Request → JwtAuthenticationFilter
    ↓
SecurityContext
    ↓
Controller → Service → DB
```

## Key Notes

* Payload is readable (Base64)
* Signature ensures integrity
* Secret key stored in environment variables
* Stateless (no session)

---

# 🧪 Testing

## Implemented

* ExpiryDecisionEngine
* ExpiryService
* RecipeAggregationService

## Planned

* RecipeService
* ScanExpiryService
* Controller layer
* Cron job logic

---

# 📌 Current Status

✔ Sprint 1 — AI Vision
✔ Sprint 2 — Decision Engine
✔ Sprint 3 — Persistence
✔ Sprint 4 — Items + Dashboard
✔ Sprint 5 — Reminder System
✔ Sprint 6 — Recipe AI
✔ JWT Authentication

---

# 📈 Future Improvements

* Role-based authorization (ADMIN / USER)
* Refresh token flow
* AWS deployment (S3 + RDS + SES)
* CI/CD pipeline
* Rate limiting for AI endpoints

---

# 🧠 Design Decisions

* Layered monolith architecture
* JWT over session (stateless)
* DB-first recipe reuse (reduce AI cost)
* AI fallback strategy
* Environment-based config (.env)

---

# ⚠️ Troubleshooting

## ❌ Missing frontend dependency

```text id="err1"
Failed to resolve import react-datepicker
```

Fix:

```bash id="fix1"
cd client
npm install
```

# 👨‍💻 Author

Full-stack project demonstrating:

* Backend architecture (Spring Boot)
* AI integration (Vision + Generation)
* JWT authentication & security
* Real-world system design
