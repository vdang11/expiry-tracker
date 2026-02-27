# Expiry Tracker

AI-powered food expiry management system.

## Architecture

Monorepo structure:

client/   → React + Vite + Tailwind (Frontend)
server/   → Spring Boot (Backend API + AI Vision)

## Current Progress

✔ Sprint 1 — OpenAI Vision Integration
✔ Sprint 2 — Confidence Decision Engine
➡ Sprint 3 — Persistence Layer (Next)

## Tech Stack

Frontend:
- React
- Vite
- Tailwind

Backend:
- Spring Boot 3.5
- OpenAI Responses API (Vision)
- MySQL (Sprint 3)

## Run Locally

### Frontend
cd client
npm install
npm run dev

### Backend
cd server
export OPENAI_API_KEY=...
mvn spring-boot:run