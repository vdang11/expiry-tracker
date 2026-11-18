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
tools/    → Dev tools (scan accuracy check)
.github/  → CI (GitHub Actions)
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
  * AI fallback generation (stops early when a call adds no new coverage)
* Only items classified as cooking ingredients are used (see below)
* Ingredient matching compares whole words (`egg` ≠ `eggplant`, `soy sauce` ≠ `tomato sauce`)

---

## 🏷 Item Classification

Every saved item is classified by AI into one category:

| Category | Used for recipes | Examples |
| -------- | ---------------- | -------- |
| `INGREDIENT` | ✅ | Trứng gà → `egg`, Nước mắm → `fish sauce`, Rau muống → `water spinach` |
| `READY_FOOD` | ❌ | Instant noodles, snacks, soft drinks |
| `MEDICINE` | ❌ | Panadol |
| `COSMETIC` | ❌ | Sunscreen, shampoo |
| `OTHER` | ❌ | Person names, gibberish, objects |

* All categories still get expiry reminders
* Ingredients also get a common English name, so Vietnamese item names match recipes
* Users can correct the category on the item detail page
* Items without a category (saved while AI was unavailable, or created before this feature) are classified automatically on backend startup and before generating recipes

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

# AI (optional: without it the app runs, only scan/recipes/classification are disabled)
OPENAI_API_KEY=

# SECURITY (at least 32 characters; required for HS256)
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

# SECURITY (at least 32 characters, otherwise the backend refuses to start)
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

* Use IntelliJ + EnvFile plugin, load `.env.dev`, run Spring Boot

or from a terminal (Maven wrapper, no Maven install needed):

```bash
cd server
SPRING_PROFILES_ACTIVE=dev OPENAI_API_KEY=sk-... ./mvnw spring-boot:run
```

* Without `JWT_SECRET`, the `dev` profile uses a built-in dev-only secret
* Start MailHog to test reminder emails: `docker compose -f docker-compose.dev.yml up mysql mailhog`

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

Production-like stack on your machine (prod profile, frontend on http://localhost):

```bash
JWT_SECRET=<32+ chars> MYSQL_ROOT_PASSWORD=<pw> OPENAI_API_KEY=<key> \
  docker compose -f docker-compose.prod.yml up --build
```

* The frontend is built against `VITE_API_URL` (default `http://localhost:8080`), not the CloudFront backend
* Email reminders need `SES_SMTP_*`; without them reminders are in-app only

---

# ✅ CI

GitHub Actions (`.github/workflows/ci.yml`) runs on every push to `main`/`develop` and every pull request:

* Backend: `./mvnw -B verify` (unit tests, no database or OpenAI key needed)
* Frontend: `npm ci`, `npm run lint`, `npm run build`

# 📸 Scan accuracy

`tools/scan-eval` runs a folder of real product photos through the scan API and reports correct / flagged / silently wrong results. See `tools/scan-eval/README.md`.

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
* CD (automatic deploy to AWS); CI is in place
* Store quantity, unit and purchase date (the form asks for them but they are not saved yet)
* Rate limiting
* Custom domain

---

# 👨‍💻 Author

Full-stack project demonstrating:

* Backend architecture & scalability
* AI integration
* AWS deployment
* Production-style system design
