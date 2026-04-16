# 🚀 Expiry Tracker

AI-powered food expiry management system that helps users track expiring items and generate recipes using AI.

---

# 🧠 Overview

Expiry Tracker is a full-stack application that combines:

* 📸 AI Vision (scan expiry date from images)
* 🗓 Expiry tracking & decision engine
* ⏰ Automated reminder system (cron job)
* 📧 Email testing via MailHog (development)
* 🍳 AI-powered recipe generation based on expiring items

---

# 🏗 Architecture

Monorepo structure:

```id="arch1"
client/   → React + Vite + Tailwind (Frontend)
server/   → Spring Boot (Backend API + AI)
```

System flow:

```id="arch2"
Frontend (React)
      ↓
Backend (Spring Boot)
      ↓
MySQL Database
      ↓
OpenAI API (Vision + Recipe AI)
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
* Validates date format and plausibility
* Prevents incorrect AI outputs

---

## 📦 Item Management

* Add / delete / consume items
* Track expiry status:

  * Fresh
  * Expiring soon
  * Expired
* Dashboard with filtering & search

---

## 🍳 Recipe Recommendation (AI + DB Hybrid)

* Uses **expiring ingredients (≤ 3 days)** as priority
* Strategy:

  1. Reuse recipes from database
  2. If not enough → generate using AI
* Ensures recipes include expiring items
* Filters non-cookable items (snacks, ready meals)

---

## ⏰ Automated Reminder System (Cron)

* Scheduled job runs daily
* Detects items that are near expiry
* Triggers notification logic

---

## 📧 Email Testing (MailHog)

* Email sending integrated with MailHog (development)
* No real email is sent

MailHog UI:

```id="mailhog"
http://localhost:8025
```

---

## 🔐 Authentication (WIP)

* Basic signup/login implemented
* ❗ JWT authentication: **not yet implemented**

---

# 🧪 Testing

## ✅ Current Coverage

Unit tests implemented for core business logic:

* ExpiryDecisionEngine
* ExpiryService
* IngredientNormalizer
* RecipeIngredientFilter

These cover the most critical and error-prone logic in the system.

---

## ⚠️ Missing / Planned Tests

To improve reliability and production readiness, the following tests are planned:

### 🔹 Service Layer (High Priority)

* RecipeService (core business logic)

  * DB reuse vs AI fallback
  * Ensure expiring ingredients are included
  * Handle empty / edge cases

* RecipeAggregationService

  * Correct filtering (expiry ≤ 3 days)
  * Exclude consumed items

---

### 🔹 AI Integration

* ScanExpiryService

  * Parse OpenAI response
  * Handle invalid / malformed JSON
  * Strip markdown formatting

---

### 🔹 Controller Layer

* ProductController
* UserController

Focus:

* request/response validation
* API contract correctness

---

### 🔹 Cron Job

* Reminder job logic

  * correct item selection
  * exclude expired / consumed items

---

### 🔹 Edge Cases

* Invalid expiry date formats
* Null values
* Past / boundary dates

---

## 🧠 Testing Philosophy

Testing is focused on:

* Core business logic (highest priority)
* AI integration points (high risk)
* Edge cases and data validation

---

# 🛠 Tech Stack

## Frontend

* React
* Vite
* Tailwind CSS
* React Router (HashRouter)

## Backend

* Spring Boot
* JPA / Hibernate
* MySQL
* OpenAI Responses API

## DevOps

* Docker (Dev + Production)
* Docker Compose
* Environment config (.env)
* Spring Scheduler (Cron)
* MailHog

---

# 🚀 Getting Started

## 1. Clone project

```bash id="clone"
git clone <repo-url>
cd expiry-tracker
```

---

## 2. Setup environment variables

Create `.env` file:

```env id="env"
MYSQL_ROOT_PASSWORD=yourpassword
OPENAI_API_KEY=your-openai-api-key
```

---

## 3. Run Dev Mode

```bash id="dev"
docker compose -f docker-compose.dev.yml up
```

---

## 4. Access

* Frontend: http://localhost:5173
* Backend: http://localhost:8080
* MailHog: http://localhost:8025

---

## ⚠️ Dev Note

Backend auto-reload may not work reliably inside Docker (MacOS limitation).

Recommended:

```id="devnote"
DB → Docker  
Backend → run locally  
Frontend → Docker / Vite  
```

---

# 📌 Current Status

✔ Sprint 1 — AI Vision
✔ Sprint 2 — Decision Engine
✔ Sprint 3 — Persistence
✔ Sprint 4 — Dashboard & Items
✔ Sprint 5 — Cron Reminder System
✔ Sprint 6 — Recipe AI

⏳ Pending:

* JWT authentication
* Production email service (e.g. AWS SES)
* AWS deployment
* CI/CD

---

# 🧠 Key Design Decisions

* Layered Monolith architecture
* Expiry-driven logic
* DB-first recipe reuse (reduce AI cost)
* AI fallback strategy
* Safe email testing via MailHog

---

# 📈 Future Improvements

* JWT authentication
* AWS SES integration
* Cloud deployment (S3 + RDS)
* CI/CD pipeline
* Advanced personalization

---

# 👨‍💻 Author

Built as a full-stack project demonstrating:

* Backend architecture (Spring Boot)
* AI integration (Vision + Generation)
* Automation with cron jobs
* Test-driven mindset for critical logic
