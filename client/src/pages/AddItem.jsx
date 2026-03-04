/**
 * AddItem Page
 *
 * Responsibilities:
 * 1) Add a food item manually OR by AI scan.
 * 2) Upload product images and call backend Vision API.
 * 3) Receive AI response (productName + expiryDate).
 * 4) Auto-fill detected fields + show helpful message.
 * 5) Validate form before saving item.
 *
 * Scan flow:
 * Take photos -> FE sends images -> BE Vision API -> AI extracts ->
 * FE fills fields + shows guidance message.
 *
 * Backend endpoint:
 * POST /api/vision/scan  (multipart form-data: images[])
 *
 * Example response:
 * {
 *   productName: "Milk",
 *   expiryDate: "2026-03-10",
 *   productNameAccepted: true,
 *   needsUserReview: false
 * }
 *
 * Image rules:
 * - Maximum 2 images
 * - Recommended: 1) product front  2) expiry label
 */

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../data/mockApi";
import { CalendarDaysIcon } from "@heroicons/react/24/outline";

const MAX_IMAGES = 2;

export default function AddItem() {
  const navigate = useNavigate();

  // -----------------------------
  // Mode: "scan" (AI) | "manual"
  // -----------------------------
  const [mode, setMode] = useState("scan");

  // -----------------------------
  // Food info fields
  // -----------------------------
  const [name, setName] = useState("");
  const [purchaseDate, setPurchaseDate] = useState("");
  const [expiryDate, setExpiryDate] = useState("");

  // -----------------------------
  // Quantity info
  // -----------------------------
  const [quantity, setQuantity] = useState(1);
  const [unit, setUnit] = useState("pcs");

  // -----------------------------
  // Image upload state
  // imageFiles: File[]
  // imagePreviews: string[] (Object URLs)
  // -----------------------------
  const [imageFiles, setImageFiles] = useState([]);
  const [imagePreviews, setImagePreviews] = useState([]);

  // -----------------------------
  // AI scan state
  // detecting: loading state for scan call
  // scanMessage: user guidance message (success/warning)
  // needsReview: AI says expiry date might be low confidence
  // productNameAccepted: AI says product name might be low confidence
  // -----------------------------
  const [detecting, setDetecting] = useState(false);
  const [scanMessage, setScanMessage] = useState("");
  const [needsReview, setNeedsReview] = useState(false);
  const [productNameAccepted, setProductNameAccepted] = useState(true);

  /**
   * Get today's date in ISO format (YYYY-MM-DD)
   *
   * Why:
   * - HTML date inputs expect YYYY-MM-DD
   * - Avoid timezone offset issues by converting to local ISO date
   */
  function todayISO() {
    const now = new Date();
    const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 10);
  }

  const today = todayISO();

  /**
   * Build dynamic UX message after scan.
   *
   * Goals:
   * - If only one side detected (name or expiry), guide user to upload the right photo.
   * - If nothing detected, ask user to retake clearer photos.
   */
  function buildScanMessage(data) {
    const hasName = !!(data.productName && data.productName.trim());
    const hasExpiry = !!(data.expiryDate && data.expiryDate !== "UNKNOWN");

    if (hasName && hasExpiry) {
      return "Product name and expiry date detected.";
    }

    if (hasExpiry) {
      return "Expiry date detected. Please scan a photo showing the product name.";
    }

    if (hasName) {
      return "Product name detected. Please scan a photo showing the expiry label.";
    }

    return "Could not detect product name or expiry date. Please take clearer photos and try again.";
  }

  /**
   * Handle selecting images
   *
   * Features:
   * - Deduplicate images (same name + size)
   * - Append new images (keep existing)
   * - Enforce MAX_IMAGES (2)
   * - Generate preview URLs
   * - Reset scan state to avoid stale messages/results
   *
   * Important:
   * - Always reset input value so user can select the same file again later
   */
  function handleImageChange(e) {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    // dedupe (name + size)
    const uniqueFiles = files.filter((newFile) => {
      return !imageFiles.some(
        (oldFile) =>
          oldFile.name === newFile.name && oldFile.size === newFile.size
      );
    });

    // append (keep old)
    const updatedFiles = [...imageFiles, ...uniqueFiles];

    // max 2 images
    if (updatedFiles.length > MAX_IMAGES) {
      alert("Max 2 photos only: 1) product front  2) expiry label.");
      // reset input so user can re-select
      e.target.value = null;
      return;
    }

    setImageFiles(updatedFiles);

    // previews for UI
    const previews = updatedFiles.map((file) => URL.createObjectURL(file));
    setImagePreviews(previews);

    // clear old scan state (avoid stale scan message)
    setScanMessage("");
    setNeedsReview(false);
    setProductNameAccepted(true);

    // allow selecting more photos after choosing
    e.target.value = null;
  }

  /**
   * Remove selected image by index
   *
   * Also clears scan state to avoid stale AI results when images changed.
   * Optional: if user removes ALL images, clear AI-filled fields too.
   */
  function removeImage(index) {
    const updatedFiles = imageFiles.filter((_, i) => i !== index);
    const updatedPreviews = imagePreviews.filter((_, i) => i !== index);

    setImageFiles(updatedFiles);
    setImagePreviews(updatedPreviews);

    // clear stale scan message/results
    setScanMessage("");
    setNeedsReview(false);
    setProductNameAccepted(true);

    // optional: if user removed all images, clear AI-filled fields
    if (updatedFiles.length === 0) {
      setName("");
      setExpiryDate("");
    }
  }

  /**
   * Manual scan only (user must click scan button)
   *
   * Steps:
   * 1) Build FormData with images[] (multipart)
   * 2) POST to backend /api/vision/scan
   * 3) Apply AI results only when they are valid
   * 4) Set review flags + dynamic message
   */
  async function handleScan() {
    if (!imageFiles.length) {
      alert("Please take at least one photo first.");
      return;
    }

    setDetecting(true);

    try {
      const formData = new FormData();
      imageFiles.forEach((file) => formData.append("images", file));

      const response = await fetch("http://localhost:8080/api/vision/scan", {
        method: "POST",
        body: formData,
      });

      if (!response.ok) {
        alert("Scan failed");
        return;
      }

      const data = await response.json();
      console.log("SCAN RESPONSE:", data);

      // fill product name if detected
      if (data.productName && data.productName.trim()) {
        setName(data.productName.trim());
      }

      // fill expiry date if detected and not UNKNOWN
      if (data.expiryDate && data.expiryDate !== "UNKNOWN") {
        setExpiryDate(data.expiryDate);
      } else {
        setExpiryDate("");
      }

      // flags from backend
      setProductNameAccepted(data.productNameAccepted ?? true);
      setNeedsReview(data.needsUserReview ?? false);

      // dynamic message for user guidance
      setScanMessage(buildScanMessage(data));
    } catch (err) {
      console.error(err);
      alert("Scan failed");
    } finally {
      setDetecting(false);
    }
  }

  /**
   * Save item (manual or scan mode)
   *
   * Validation rules:
   * - name required
   * - purchaseDate required
   * - purchaseDate cannot be in future
   * - expiryDate cannot be in past
   * - expiryDate cannot be earlier than purchaseDate
   */
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

    if (purchaseDate > today) {
      alert("Purchase date cannot be in the future.");
      return;
    }

    if (expiryDate && expiryDate < today) {
      alert("Expiry date cannot be in the past.");
      return;
    }

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

  /**
   * Switch between Scan and Manual modes
   *
   * When leaving scan mode, we reset scan state so it doesn't leak into manual mode.
   */
  function switchMode(next) {
    setMode(next);

    if (next !== "scan") {
      setImageFiles([]);
      setImagePreviews([]);
      setScanMessage("");
      setNeedsReview(false);
      setDetecting(false);
      setProductNameAccepted(true);
    }
  }

  return (
    <div className="mx-auto max-w-xl space-y-4">
      <h2 className="text-xl font-semibold">Add Item</h2>

      {/* Mode switch */}
      <div className="flex gap-2">
        <Pill active={mode === "scan"} onClick={() => switchMode("scan")}>
          Scan
        </Pill>

        <Pill active={mode === "manual"} onClick={() => switchMode("manual")}>
          Manual
        </Pill>
      </div>

      {/* Scan section (only in scan mode) */}
      {mode === "scan" && (
        <div className="rounded-2xl border border-line bg-card p-4 space-y-3">
          <p className="text-sm text-muted text-center">
            Take photos of the product and expiry date
          </p>

          {/* Upload / capture images */}
          <label className="flex items-center justify-center gap-2 rounded-xl bg-accent px-4 py-3 text-sm font-semibold text-black cursor-pointer">
            📷 Take Photo
            <input
              type="file"
              accept="image/*"
              capture="environment"
              multiple
              onChange={handleImageChange}
              className="hidden"
            />
          </label>

          {/* Selected count + helpful hint when only 1 image */}
          {imageFiles.length > 0 && (
            <p className="text-xs text-center text-muted">
              {imageFiles.length} photo{imageFiles.length > 1 ? "s" : ""} selected
              {imageFiles.length === 1 ? (
                <span className="block text-yellow-400 mt-1">
                  Tip: take photo of the expiry label for best accuracy
                </span>
              ) : null}
            </p>
          )}

          {/* Preview grid */}
          {imagePreviews.length > 0 && (
            <div className="grid grid-cols-2 gap-2">
              {imagePreviews.map((src, index) => (
                <div key={index} className="relative">
                  <img
                    src={src}
                    alt={`Preview ${index}`}
                    className="w-full max-h-48 object-contain rounded-xl border border-line"
                  />

                  {/* Remove image */}
                  <button
                    onClick={() => removeImage(index)}
                    className="absolute top-1 right-1 bg-black/70 text-white text-xs px-2 py-1 rounded-lg"
                    title="Remove photo"
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* Manual scan button */}
          <button
            onClick={handleScan}
            disabled={detecting || imageFiles.length === 0}
            className="w-full rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-black disabled:opacity-50"
          >
            {detecting ? "Scanning..." : "Scan images"}
          </button>

          {/* Scan result message box */}
          {scanMessage && (
            <div
              className={
                "rounded-xl p-3 text-sm border text-center " +
                (needsReview
                  ? "bg-yellow-900/30 border-yellow-600 text-yellow-300"
                  : "bg-green-900/30 border-green-600 text-green-300")
              }
            >
              {scanMessage}
            </div>
          )}
        </div>
      )}

      {/* Form section (always visible) */}
      <div className="rounded-2xl border border-line bg-card p-4 space-y-3">
        <Field label="Food name">
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. Milk"
            className="w-full rounded-xl border border-line bg-bg px-3 py-2 text-sm"
          />

          {/* Low confidence warning for product name */}
          {!productNameAccepted && (
            <p className="text-xs text-yellow-400">
              ⚠ Please verify product name
            </p>
          )}
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
            {/* Low confidence warning for expiry date */}
            {needsReview && (
              <p className="text-xs text-yellow-400">
                ⚠ Please verify expiry date
              </p>
            )}

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

/**
 * Pill component
 * Used for toggling Scan / Manual modes.
 */
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

/**
 * Field component
 * Reusable wrapper: label + children (input/select)
 */
function Field({ label, children }) {
  return (
    <label className="block space-y-1">
      <div className="text-xs text-muted">{label}</div>
      {children}
    </label>
  );
}