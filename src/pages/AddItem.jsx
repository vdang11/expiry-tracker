import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../data/mockApi";
import { CalendarDaysIcon } from "@heroicons/react/24/outline";

export default function AddItem() {
  const navigate = useNavigate();

  // mode
  const [mode, setMode] = useState("manual"); // manual | scan

  // form fields
  const [name, setName] = useState("");
  const [purchaseDate, setPurchaseDate] = useState("");
  const [expiryDate, setExpiryDate] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [unit, setUnit] = useState("pcs");

  // scan mock
  const [detecting, setDetecting] = useState(false);

  async function handleSave() {
    if (!name.trim()) {
      alert("Please enter food name");
      return;
    }

    if (!purchaseDate) {
      alert("Please select purchase date");
      return;
    }

    await api.createItem({
      name,
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
    setDetecting(true);

    setTimeout(() => {
      setName("Milk");
      setExpiryDate("2026-01-05");
      setDetecting(false);
    }, 800);
  }

  return (
    <div className="mx-auto max-w-xl space-y-4">
      <h2 className="text-xl font-semibold">Add Item</h2>

      {/* Mode switch */}
      <div className="flex gap-2">
        <Pill active={mode === "manual"} onClick={() => setMode("manual")}>
          Manual
        </Pill>
        <Pill active={mode === "scan"} onClick={() => setMode("scan")}>
          Scan
        </Pill>
      </div>

      {/* Scan mode */}
      {mode === "scan" && (
        <div className="rounded-2xl border border-line bg-card p-4 space-y-3">
          <p className="text-sm text-muted">
            Chụp hình bao bì, hệ thống sẽ tự nhận diện hạn dùng.
          </p>

          <button
            onClick={handleScan}
            disabled={detecting}
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
