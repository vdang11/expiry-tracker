#!/usr/bin/env node
// Measures how well /api/vision/scan reads expiry dates from real photos.
//
// Put photos in a folder, named  <anything>__<expected>.<jpg|jpeg|png|webp>
//   expected = YYYY-MM-DD, or UNKNOWN when the photo has no readable date
//   e.g.  vinamilk-hop__2027-03-15.jpg   mi-goi-mat-sau__UNKNOWN.jpg
//
// Usage (backend running):
//   API_URL=http://localhost:8080 EMAIL=you@example.com PASSWORD=... \
//     node tools/scan-eval/scan-eval.mjs path/to/photos
//
// Each photo costs one OpenAI vision call.

import { readdir, readFile } from "node:fs/promises";
import path from "node:path";

const API_URL = process.env.API_URL || "http://localhost:8080";
const { EMAIL, PASSWORD } = process.env;
const dir = process.argv[2];

if (!dir || !EMAIL || !PASSWORD) {
  console.error("Usage: API_URL=... EMAIL=... PASSWORD=... node scan-eval.mjs <photo-folder>");
  process.exit(1);
}

const MIME = { ".jpg": "image/jpeg", ".jpeg": "image/jpeg", ".png": "image/png", ".webp": "image/webp" };

async function login() {
  const res = await fetch(`${API_URL}/api/users/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email: EMAIL, password: PASSWORD }),
  });
  if (!res.ok) throw new Error(`Login failed: ${res.status} ${await res.text()}`);
  return (await res.json()).token;
}

async function scan(token, file) {
  const form = new FormData();
  const type = MIME[path.extname(file).toLowerCase()];
  form.append("images", new Blob([await readFile(file)], { type }), path.basename(file));

  const res = await fetch(`${API_URL}/api/vision/scan`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
    body: form,
  });
  if (!res.ok) return { error: `${res.status} ${await res.text()}` };
  return res.json();
}

const token = await login();
const files = (await readdir(dir))
  .filter((f) => MIME[path.extname(f).toLowerCase()] && f.includes("__"))
  .sort();

if (files.length === 0) {
  console.error(`No photos named <name>__<expected>.<ext> in ${dir}`);
  process.exit(1);
}

let correct = 0;
let flagged = 0;
let silentWrong = 0;

for (const f of files) {
  const expected = path.basename(f, path.extname(f)).split("__").pop();
  const r = await scan(token, path.join(dir, f));
  const got = r.error ? "ERROR" : r.expiryDate || "UNKNOWN";
  const review = r.needsUserReview ? "review" : "";
  const ok = got === expected;

  if (ok) correct++;
  else if (r.needsUserReview || got === "UNKNOWN") flagged++;   // wrong, but the user is warned
  else silentWrong++;                                            // wrong and looks confident

  console.log(`${ok ? "OK  " : "FAIL"}  ${f.padEnd(40)} expected ${expected.padEnd(10)} got ${got.padEnd(10)} ${review}`);
  if (r.error) console.log(`      ${r.error}`);
}

console.log(`\n${correct}/${files.length} correct, ${flagged} wrong but flagged for review, ${silentWrong} silently wrong`);
process.exit(silentWrong > 0 ? 2 : 0);
