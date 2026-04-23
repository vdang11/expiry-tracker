# 🚀 Expiry Tracker

AI-powered food expiry management system that helps users track expiring items and generate recipes using AI.

---

# 🧠 Overview

Expiry Tracker is a full-stack application that combines:

* 📸 AI Vision (scan expiry date from images)
* 🗓 Expiry tracking & decision engine
* 🍳 Recipe recommendation (AI + DB hybrid)
* ⏰ Automated reminder system (cron job)
* 📧 Email testing via MailHog
* 🔐 JWT-based authentication (stateless)

---

# 🏗 Architecture

```
client/   → React + Vite + Tailwind
server/   → Spring Boot (API + AI logic)
.env      → Environment variables
docker-compose.* → Dev / Prod environments
```

---

# ⚙️ Features

## 📸 AI Expiry Scanning

* Upload images (1–2)
* Extract expiry date via OpenAI Vision
* Handles blurry / missing data
* Returns confidence + decision status

## 🧠 Decision Engine

* CONFIRMED / REVIEW / REJECTED
* Validates date format & plausibility

## 📦 Item Management

* Add / delete / consume items
* Expiry states: Fresh / Expiring / Expired
* Server-side filtering, sorting, pagination

## 🍳 Recipe Recommendation

* Uses expiring ingredients (≤ 3 days)
* DB-first → AI fallback
* Filters non-cookable items

## ⏰ Reminder System

* Daily cron job
* Sends notifications

## 🔐 Authentication

* JWT-based (stateless)
* Token stored in frontend
* Sent via Authorization header

---

# 🛠 Tech Stack

Frontend:

* React + Vite + Tailwind

Backend:

* Spring Boot + JPA + MySQL
* Spring Security (JWT)

Tools:

* IntelliJ IDEA
* Docker
* MailHog

---

# 🚀 Getting Started (Local - Recommended)

## 1. Clone project

```bash
git clone <repo-url>
cd expiry-tracker
```

---

## 2. Create `.env`

```env
JWT_SECRET=your-secret-key
OPENAI_API_KEY=your-openai-key
MYSQL_ROOT_PASSWORD=root
```

---

## 3. Run Database (Docker only)

```bash
docker compose -f docker-compose.dev.yml up mysql
```

---

## 4. Run Backend (IntelliJ)

* Install plugin: **EnvFile**
* Enable EnvFile in Run Configuration
* Add `.env`

Run Spring Boot

---

## 5. Run Frontend

```bash
cd client
npm install
npm run dev
```

---

## 6. Access

* Frontend: http://localhost:5173
* Backend: http://localhost:8080
* MailHog: http://localhost:8025

---

# 📱 Mobile Testing

To test on mobile devices:

1. Make sure phone and laptop are on the same WiFi network
2. Use your laptop’s LAN IP (NOT localhost)

Example:

```
http://192.168.x.x:8080
```

> Note:
>
> * `localhost` only works on the same device
> * Works for both local and Docker setups (if ports are exposed)

---

# 🐳 Docker Setup

---

## 📦 Dev Mode

Used for development (hot reload, debugging)

```bash
docker compose -f docker-compose.dev.yml up
```

Run in background:

```bash
docker compose -f docker-compose.dev.yml up -d
```

Stop:

```bash
docker compose -f docker-compose.dev.yml down
```

Reset database:

```bash
docker compose -f docker-compose.dev.yml down -v
```

> Use `--build` only when dependencies or Dockerfile change

---

## 🏭 Prod Mode

Used for production simulation

```bash
docker compose -f docker-compose.prod.yml up --build
```

Run in background:

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Stop:

```bash
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

# 🧠 Dev Workflow (Recommended)

```
Frontend + Backend → run locally
Database → Docker
```

✔ Faster development
✔ Easier debugging
✔ Avoids Docker networking issues

---

# 🔐 Security

JWT Flow:

```
Login → JWT
      ↓
Request → JwtAuthenticationFilter
      ↓
SecurityContext
      ↓
Controller → Service → DB
```

---

# ⚠️ Troubleshooting

## ❌ Missing frontend dependency

```bash
cd client
npm install
```

---

## ❌ Mobile cannot connect

Use LAN IP instead of localhost

---

## ❌ Docker ECONNREFUSED

Cause:

* Frontend calling `localhost` inside container

Fix:

* Use `backend:8080` (service name)

---

## ❌ Backend not starting

```bash
docker logs expiry-backend
```

---

# 📌 Current Status

✔ AI Vision
✔ Decision Engine
✔ Item Management
✔ Reminder System
✔ Recipe AI
✔ JWT Authentication

---

# 📈 Future Improvements

* Role-based authorization (ADMIN / USER)
* AWS deployment (S3 + RDS + SES)
* CI/CD pipeline
* Rate limiting

---

# 👨‍💻 Author

Full-stack project demonstrating:

* Backend architecture
* AI integration
* JWT authentication
* Real-world system design
