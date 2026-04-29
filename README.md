# 🚀 Expiry Tracker

AI-powered food expiry management system that helps users track expiring items and generate recipes intelligently.

---

# 🧠 Overview

Expiry Tracker is a full-stack application that combines **AI, backend engineering, and cloud deployment** to reduce food waste and improve inventory management.

Key capabilities:

* 📸 Scan expiry dates from product images (AI Vision)
* 🧠 Evaluate data reliability using a decision engine
* 📦 Manage food inventory with expiry tracking
* 🍳 Generate recipes based on expiring ingredients
* ⏰ Automated expiry reminders
* 🔐 Secure authentication using JWT

---

# 🏗 Architecture

```text
User (HTTPS)
   ↓
CloudFront (Frontend - S3)
   ↓
CloudFront (Backend - HTTPS Proxy)
   ↓
Elastic Beanstalk (Spring Boot API)
   ↓
RDS (MySQL)
```

Monorepo structure:

```text
client/   → React + Vite + Tailwind
server/   → Spring Boot (API + AI logic)
docker/   → Dev environments
```

---

# ⚙️ Features

## 📸 AI Expiry Scanning

* Upload 1–2 product images
* Extract expiry date using OpenAI Vision
* Handles blurry or missing data
* Returns structured result with confidence

---

## 🧠 Decision Engine

* Status: `CONFIRMED / REVIEW / REJECTED`
* Validates:

  * Date format
  * Logical correctness (past/future)
  * Confidence thresholds

---

## 📦 Item Management

* Add / delete / consume items
* Expiry states:

  * Fresh
  * Expiring Soon
  * Expired
* Server-side filtering, sorting, pagination

---

## 🍳 Recipe Recommendation (AI + DB Hybrid)

* Uses expiring ingredients (≤ 3 days)
* Strategy:

  * Database-first reuse
  * AI fallback generation
* Filters non-cookable items

---

## ⏰ Reminder System

* Daily scheduled job (cron)
* Uses MailHog in development (configured in backend)

---

## 🔐 Authentication

* JWT-based (stateless)
* Token sent via Authorization header

---

# 🛠 Tech Stack

### Frontend

* React
* Vite
* Tailwind CSS

### Backend

* Spring Boot
* Spring Security (JWT)
* JPA / Hibernate
* MySQL

### AI

* OpenAI Vision API

### DevOps / Infrastructure

* Docker (development)
* MailHog (email testing)

### AWS

* S3 (frontend hosting)
* CloudFront (CDN + HTTPS)
* Elastic Beanstalk (backend)
* RDS (MySQL)

---

# 🌐 Deployment (AWS)

### Frontend

* Hosted on **AWS S3**
* Delivered via **CloudFront (HTTPS)**

### Backend

* Deployed on **Elastic Beanstalk**
* Accessed via **CloudFront (HTTPS proxy)**

### Benefits

* HTTPS enabled for all user traffic
* No mixed content issues
* Production-style architecture

---

# 🔐 Environment Setup

Create environment files in the **root folder**.

---

## ⚙️ `.env.dev` (Local Development)

```env
SPRING_PROFILES_ACTIVE=dev

# DATABASE (Docker MySQL)
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
MYSQL_ROOT_PASSWORD=

# AI
OPENAI_API_KEY=

# SECURITY
JWT_SECRET=
```

👉 Used for:

* Local development
* Docker MySQL
* Fast testing & debugging

---

## ⚙️ `.env.prod` (Production Simulation - Local Only)

```env
SPRING_PROFILES_ACTIVE=prod

# AWS RDS
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=

# AI
OPENAI_API_KEY=

# SECURITY
JWT_SECRET=
```

👉 Used for:

* Connecting to AWS RDS from local machine
* Testing production configuration
* Allowing Hibernate to create/update schema

---

## ❗ Important Notes

* `.env.prod` is **NOT used in AWS deployment**
* AWS uses **Environment Variables (Elastic Beanstalk)**
* Do NOT commit `.env` files

---

# 🚀 Getting Started (Local Development)

## 1. Clone project

```bash
git clone <repo-url>
cd expiry-tracker
```

---

## 2. Create `.env.dev`

Copy from README and fill values

---

## 3. Start database

```bash
docker compose -f docker-compose.dev.yml up mysql
```

---

## 4. Run backend

* Use IntelliJ + EnvFile plugin
* Load `.env.dev`
* Run Spring Boot

---

## 5. Run frontend

```bash
cd client
npm install
npm run dev
```

---

## 6. Access

| Service  | URL                   |
| -------- | --------------------- |
| Frontend | http://localhost:5173 |
| Backend  | http://localhost:8080 |
| MailHog  | http://localhost:8025 |

---

# 🔄 Optional: Test Production Config Locally

```bash
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

👉 Uses `.env.prod`
👉 Connects to AWS RDS

---

# 🐳 Docker

```bash
docker compose -f docker-compose.dev.yml up
```

---

# 🔐 Security

JWT flow:

```text
Login → JWT
      ↓
Request → JwtAuthenticationFilter
      ↓
SecurityContext
      ↓
Controller → Service → Database
```

---

# ⚠️ Troubleshooting

## Mobile cannot connect

Use LAN IP instead of localhost

---

## Docker networking issue

Use service name instead of localhost:

```text
backend:8080
```

---

## Backend logs

```bash
docker logs expiry-backend
```

---

# 📌 Current Status

✔ AI Vision
✔ Decision Engine
✔ Item Management
✔ Recipe AI (DB + AI hybrid)
✔ Reminder system (MailHog)
✔ JWT authentication
✔ AWS deployment (S3 + CloudFront + EB + RDS)

---

# 📈 Future Improvements

* Role-based authorization (ADMIN / USER)
* AWS SES for production email
* CI/CD pipeline
* Rate limiting
* Custom domain

---

# 👨‍💻 Author

Full-stack project demonstrating:

* Backend architecture & scalability
* AI integration
* AWS deployment
* Production-style system design
