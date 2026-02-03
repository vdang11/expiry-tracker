import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../data/mockApi";
import { CalendarDaysIcon } from "@heroicons/react/24/outline";

export default function AddItem() {
  const navigate = useNavigate();

  // mode
  const [mode, setMode] = useState("scan"); // manual | scan

  // form fields
  const [name, setName] = useState("");
  const [purchaseDate, setPurchaseDate] = useState("");
  const [expiryDate, setExpiryDate] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [unit, setUnit] = useState("pcs");
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState("");


  // scan mock
  const [detecting, setDetecting] = useState(false);

  function handleImageChange(e) {
  const file = e.target.files?.[0];
  if (!file) return;

  setImageFile(file);
  setImagePreview(URL.createObjectURL(file));
}


    // yyyy-mm-dd in local time (safe for <input type="date">)
  function todayISO() {
    const now = new Date();
    const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 10);
  }

  const today = todayISO();

  async function handleSave() {
  const trimmedName = name.trim();
  if (!trimmedName) {
    alert("Please enter food name");
    return;
  }

  if (!purchaseDate) {
    alert("Please select purchase date");
    return;
  }

  // rule 1: purchase date must NOT be in the future
  if (purchaseDate > today) {
    alert("Purchase date cannot be in the future.");
    return;
  }

  // rule 2: expiry date must NOT be in the past (if provided)
  if (expiryDate && expiryDate < today) {
    alert("Expiry date cannot be in the past.");
    return;
  }

  // optional: expiry should not be before purchase
  if (expiryDate && expiryDate < purchaseDate) {
    alert("Expiry date cannot be earlier than purchase date.");
    return;
  }

  await api.createItem({
    name: trimmedName,
    purchaseDate,
    expiryDate: expiryDate || null,
    estimatedExpiryDate: null,
    quantity,
    unit,
    category: "OTHER",
    storageType: "FRIDGE",
    source: mode === "scan" ? "scan" : "manual",
  });

  navigate("/");
}


  // mock scan (sau này thay bằng OCR backend)
  function handleScan() {
  if (!imageFile) {
    alert("Please upload an image first.");
    return;
  }

  setDetecting(true);

  setTimeout(() => {
    // mock result (later: send imageFile to backend)
    setName("Milk");
    setExpiryDate("2026-01-05");
    setDetecting(false);
  }, 800);
}

function switchMode(next) {
  setMode(next);
  if (next !== "scan") {
    setImageFile(null);
    setImagePreview("");
    setDetecting(false);
  }
}



  return (
    <div className="mx-auto max-w-xl space-y-4">
      <h2 className="text-xl font-semibold">Add Item</h2>

      {/* Mode switch */}
      <div className="flex gap-2">
        <Pill active={mode === "scan"} onClick={() => switchMode("scan")}>Scan</Pill>
        <Pill active={mode === "manual"} onClick={() => switchMode("manual")}>Manual</Pill>
      </div>

      {/* Scan mode */}
      {mode === "scan" && (
  <div className="rounded-2xl border border-line bg-card p-4 space-y-3">
    <p className="text-sm text-muted">
      Upload an image first, then scan to detect expiry date.
    </p>

    {/* Upload */}
    <input
      type="file"
      accept="image/*"
      onChange={handleImageChange}
      className="block w-full text-sm"
    />

    {/* Preview */}
    {imagePreview && (
      <img
        src={imagePreview}
        alt="Preview"
        className="w-full max-h-64 object-contain rounded-xl border border-line"
      />
    )}

    {/* Scan */}
    <button
      onClick={handleScan}
      disabled={detecting || !imageFile}
      className="rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-black disabled:opacity-60"
    >
      {detecting ? "Scanning..." : "Scan image"}
    </button>
  </div>
)}


      {/* Form */}
      <div className="rounded-2xl border border-line bg-card p-4 space-y-3">
        <Field label="Food name">
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. Milk"
            className="w-full rounded-xl border border-line bg-bg px-3 py-2 text-sm"
          />
        </Field>

        <div className="grid gap-3 sm:grid-cols-2">
          <Field label="Purchase date">
            <div className="relative">
              <input
                type="date"
                value={purchaseDate}
                max={today}
                onChange={(e) => setPurchaseDate(e.target.value)}
                className="w-full rounded-xl border border-line bg-bg px-3 py-2 pr-10 text-sm"
              />
              <CalendarDaysIcon className="pointer-events-none absolute right-3 top-1/2 h-5 w-5 -translate-y-1/2 text-muted" />
            </div>
          </Field>


          <Field label="Expiry date (optional)">
            <div className="relative">
              <input
                type="date"
                value={expiryDate}
                min={today}
                onChange={(e) => setExpiryDate(e.target.value)}
                className="w-full rounded-xl border border-line bg-bg px-3 py-2 pr-10 text-sm"
              />
              <CalendarDaysIcon className="pointer-events-none absolute right-3 top-1/2 h-5 w-5 -translate-y-1/2 text-muted" />
            </div>
          </Field>

        </div>

        <div className="grid gap-3 sm:grid-cols-2">
          <Field label="Quantity">
            <input
              type="number"
              min="0.1"
              step="0.1"
              value={quantity}
              onChange={(e) => setQuantity(Number(e.target.value))}
              className="w-full rounded-xl border border-line bg-bg px-3 py-2 text-sm"
            />
          </Field>

          <Field label="Unit">
            <select
              value={unit}
              onChange={(e) => setUnit(e.target.value)}
              className="w-full rounded-xl border border-line bg-bg px-3 py-2 text-sm"
            >
              <option value="pcs">pcs</option>
              <option value="kg">kg</option>
              <option value="g">g</option>
              <option value="L">L</option>
              <option value="bunch">bunch</option>
            </select>
          </Field>
        </div>

        <button
          onClick={handleSave}
          className="w-full rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-black"
        >
          Save item
        </button>
      </div>
    </div>
  );
}

/* ===== helpers ===== */

function Pill({ active, onClick, children }) {
  return (
    <button
      onClick={onClick}
      className={
        "rounded-full border px-3 py-1 text-sm transition " +
        (active
          ? "border-accent/60 text-white"
          : "border-line text-muted hover:text-white")
      }
    >
      {children}
    </button>
  );
}

function Field({ label, children }) {
  return (
    <label className="block space-y-1">
      <div className="text-xs text-muted">{label}</div>
      {children}
    </label>
  );
}
