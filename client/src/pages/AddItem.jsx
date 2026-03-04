import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../data/mockApi";
import { CalendarDaysIcon } from "@heroicons/react/24/outline";

export default function AddItem() {

  const navigate = useNavigate();

  const [mode, setMode] = useState("scan");

  const [name, setName] = useState("");
  const [purchaseDate, setPurchaseDate] = useState("");
  const [expiryDate, setExpiryDate] = useState("");

  const [quantity, setQuantity] = useState(1);
  const [unit, setUnit] = useState("pcs");

  const [imageFiles, setImageFiles] = useState([]);
  const [imagePreviews, setImagePreviews] = useState([]);

  const [detecting, setDetecting] = useState(false);
  const [scanMessage, setScanMessage] = useState("");
  const [needsReview, setNeedsReview] = useState(false);

  const [productNameAccepted, setProductNameAccepted] = useState(true);

  function todayISO() {
    const now = new Date();
    const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 10);
  }

  const today = todayISO();

  function handleImageChange(e) {

    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    setImageFiles(files);

    const previews = files.map(file => URL.createObjectURL(file));
    setImagePreviews(previews);

    setScanMessage("");
    setNeedsReview(false);
  }

  function removeImage(index) {

    const newFiles = imageFiles.filter((_, i) => i !== index);
    const newPreviews = imagePreviews.filter((_, i) => i !== index);

    setImageFiles(newFiles);
    setImagePreviews(newPreviews);
  }

  async function handleScan() {

    if (!imageFiles.length) {
      alert("Please take at least one photo first.");
      return;
    }

    setDetecting(true);

    try {

      const formData = new FormData();

      imageFiles.forEach(file => {
        formData.append("images", file);
      });

      const response = await fetch(
        "http://localhost:8080/api/vision/scan",
        {
          method: "POST",
          body: formData
        }
      );

      if (!response.ok) {
        alert("Scan failed");
        return;
      }

      const data = await response.json();

      console.log("SCAN RESPONSE:", data);

      if (data.productName) {
        setName(data.productName);
      }

      if (data.expiryDate && data.expiryDate !== "UNKNOWN") {
        setExpiryDate(data.expiryDate);
      }

      setProductNameAccepted(data.productNameAccepted ?? true);

      setScanMessage(data.message || "");
      setNeedsReview(data.needsUserReview || false);

    } catch (err) {

      console.error(err);
      alert("Scan failed");

    } finally {

      setDetecting(false);

    }
  }

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

  function switchMode(next) {

    setMode(next);

    if (next !== "scan") {
      setImageFiles([]);
      setImagePreviews([]);
      setScanMessage("");
      setNeedsReview(false);
      setDetecting(false);
    }
  }

  return (

    <div className="mx-auto max-w-xl space-y-4">

      <h2 className="text-xl font-semibold">Add Item</h2>

      <div className="flex gap-2">

        <Pill active={mode === "scan"} onClick={() => switchMode("scan")}>
          Scan
        </Pill>

        <Pill active={mode === "manual"} onClick={() => switchMode("manual")}>
          Manual
        </Pill>

      </div>

      {mode === "scan" && (

        <div className="rounded-2xl border border-line bg-card p-4 space-y-3">

          <p className="text-sm text-muted text-center">
            Take photos of the product and expiry date
          </p>

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

          {imageFiles.length > 0 && (
            <p className="text-xs text-center text-muted">
              {imageFiles.length} photo{imageFiles.length > 1 ? "s" : ""} selected
            </p>
          )}

          {imageFiles.length === 1 && (
            <p className="text-xs text-yellow-400 text-center">
              Tip: take photo of the expiry label for best accuracy
            </p>
          )}

          {imagePreviews.length > 0 && (

            <div className="grid grid-cols-2 gap-2">

              {imagePreviews.map((src, index) => (

                <div key={index} className="relative">

                  <img
                    src={src}
                    alt="preview"
                    className="w-full max-h-48 object-contain rounded-xl border border-line"
                  />

                  <button
                    onClick={() => removeImage(index)}
                    className="absolute top-1 right-1 bg-black/70 text-white text-xs px-2 py-1 rounded-lg"
                  >
                    ✕
                  </button>

                </div>

              ))}

            </div>

          )}

          <button
            onClick={handleScan}
            disabled={detecting || imageFiles.length === 0}
            className="w-full rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-black disabled:opacity-50"
          >
            {detecting ? "Scanning..." : "Scan images"}
          </button>

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

      <div className="rounded-2xl border border-line bg-card p-4 space-y-3">

        <Field label="Food name">

          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. Milk"
            className="w-full rounded-xl border border-line bg-bg px-3 py-2 text-sm"
          />

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