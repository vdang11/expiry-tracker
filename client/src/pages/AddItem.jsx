import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/apiClient";
import { CalendarDaysIcon, ArrowPathIcon } from "@heroicons/react/24/outline";
import toast from "react-hot-toast";

const MAX_IMAGES = 2;

const UNIT_GROUPS = [
  {
    label: "Count",
    options: [
      { value: "pcs", label: "Pieces (pcs)" },
      { value: "pack", label: "Pack" },
      { value: "box", label: "Box" },
      { value: "bag", label: "Bag" },
      { value: "bottle", label: "Bottle" },
      { value: "can", label: "Can" },
      { value: "jar", label: "Jar" },
      { value: "tin", label: "Tin" },
      { value: "pouch", label: "Pouch" },
      { value: "slice", label: "Slice" }
    ]
  },
  {
    label: "Weight",
    options: [
      { value: "g", label: "Gram (g)" },
      { value: "kg", label: "Kilogram (kg)" }
    ]
  },
  {
    label: "Liquid",
    options: [
      { value: "ml", label: "Milliliter (ml)" },
      { value: "L", label: "Liter (L)" }
    ]
  },
  {
    label: "Fresh Produce",
    options: [{ value: "bunch", label: "Bunch" }]
  }
];

const API_BASE =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

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

  const previewRef = useRef([]);

  const [detecting, setDetecting] = useState(false);

  const [scanMessage, setScanMessage] = useState("");
  const [scanStatus, setScanStatus] = useState("idle");

  const [needsReview, setNeedsReview] = useState(false);
  const [productNameAccepted, setProductNameAccepted] = useState(true);

  useEffect(() => {
    previewRef.current = imagePreviews;
  }, [imagePreviews]);

  useEffect(() => {
    return () => {
      previewRef.current.forEach((url) => {
        try {
          URL.revokeObjectURL(url);
        } catch {}
      });
    };
  }, []);

  function todayISO() {
    const now = new Date();
    const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 10);
  }

  const today = todayISO();

  function handleImageChange(e) {
    if (detecting) return;

    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    const total = imageFiles.length + files.length;

    if (total > MAX_IMAGES) {
      toast.error("Max 2 photos only: product front + expiry label.");
      e.target.value = null;
      return;
    }

    const uniqueFiles = files.filter((newFile) => {
      return !imageFiles.some(
        (oldFile) =>
          oldFile.name === newFile.name &&
          oldFile.size === newFile.size
      );
    });

    const newPreviews = uniqueFiles.map((file) => URL.createObjectURL(file));

    setImageFiles((prev) => [...prev, ...uniqueFiles]);
    setImagePreviews((prev) => [...prev, ...newPreviews]);

    setScanMessage("");
    setScanStatus("idle");
    setNeedsReview(false);
    setProductNameAccepted(true);

    e.target.value = null;
  }

  function removeImage(index) {
    if (detecting) return;

    const url = imagePreviews[index];

    if (url) {
      try {
        URL.revokeObjectURL(url);
      } catch {}
    }

    const updatedFiles = imageFiles.filter((_, i) => i !== index);
    const updatedPreviews = imagePreviews.filter((_, i) => i !== index);

    setImageFiles(updatedFiles);
    setImagePreviews(updatedPreviews);

    setScanMessage("");
    setScanStatus("idle");
    setNeedsReview(false);
    setProductNameAccepted(true);

    if (updatedFiles.length === 0) {
      setName("");
      setExpiryDate("");
    }
  }

  async function handleScan() {
    if (!imageFiles.length) {
      toast.error("Please take at least one photo first.");
      return;
    }

    setDetecting(true);

    try {
      const formData = new FormData();

      imageFiles.forEach((file) => formData.append("images", file));

      const response = await fetch(`${API_BASE}/api/vision/scan`, {
        method: "POST",
        body: formData
      });

      let data = null;

      try {
        data = await response.json();
      } catch {}

      if (!response.ok) {
        const message =
          data?.message ||
          data?.error ||
          "Scan failed. Please try again.";

        toast.error(message);
        return;
      }

      if (data.productName && data.productName.trim()) {
        setName(data.productName.trim());
      }

      if (data.expiryDate && data.expiryDate !== "UNKNOWN") {
        setExpiryDate(data.expiryDate);
      } else {
        setExpiryDate("");
      }

      setProductNameAccepted(data.productNameAccepted ?? true);
      setNeedsReview(data.needsUserReview ?? false);

      setScanMessage(data.message ?? "");

      if (data.expiryDate === "UNKNOWN") {
        setScanStatus("rejected");
      } else if (data.needsUserReview) {
        setScanStatus("review");
      } else {
        setScanStatus("success");
      }
    } catch (err) {
      console.error(err);
      toast.error("Cannot reach server. Please try again.");
    } finally {
      setDetecting(false);
    }
  }

  const canSave =
    !detecting &&
    name.trim() &&
    quantity > 0 &&
    (mode === "manual" || imageFiles.length > 0);

  async function handleSave() {
    const trimmedName = name.trim();

    if (!trimmedName) {
      toast.error("Please enter food name.");
      return;
    }

    if (!purchaseDate) {
      toast.error("Please select purchase date.");
      return;
    }

    if (purchaseDate > today) {
      toast.error("Purchase date cannot be in the future.");
      return;
    }

    if (!expiryDate) {
      toast.error("Please select expiry date.");
      return;
    }

    if (expiryDate < today) {
      toast.error("Expiry date cannot be in the past.");
      return;
    }

    if (expiryDate < purchaseDate) {
      toast.error("Expiry date cannot be earlier than purchase date.");
      return;
    }

    const raw = localStorage.getItem("currentUser");
    const currentUser = raw ? JSON.parse(raw) : null;

    if (!currentUser) {
      toast.error("User not logged in.");
      return;
    }

    const payload = {
      productName: trimmedName,
      expiryDate: expiryDate,
      confidence: 1.0,
      dateType: "CONFIRMED",
      decisionStatus: "CONFIRMED",
      suggestedAction: "KEEP",
      userId: currentUser.id
    };

    try {
      await api.saveItem(payload);
      toast.success("Item added successfully");
      navigate("/");
    } catch (error) {
      console.error(error);
      toast.error(error.message || "Cannot reach server.");
    }
  }

  function switchMode(next) {
    if (detecting) return;

    setMode(next);

    if (next !== "scan") {
      imagePreviews.forEach((url) => {
        try {
          URL.revokeObjectURL(url);
        } catch {}
      });

      setImageFiles([]);
      setImagePreviews([]);
      setScanMessage("");
      setScanStatus("idle");
      setNeedsReview(false);
      setDetecting(false);
      setProductNameAccepted(true);
    }
  }

  function messageStyle() {
    if (scanStatus === "success") {
      return "bg-green-900/30 border-green-600 text-green-300";
    }

    if (scanStatus === "review") {
      return "bg-yellow-900/30 border-yellow-600 text-yellow-300";
    }

    if (scanStatus === "rejected") {
      return "bg-red-900/30 border-red-600 text-red-300";
    }

    return "";
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

          <label
            className={
              "flex items-center justify-center gap-2 rounded-xl px-4 py-3 text-sm font-semibold text-black " +
              (detecting
                ? "bg-gray-400 cursor-not-allowed"
                : "bg-accent cursor-pointer")
            }
          >
            📷 Take Photo
            <input
              type="file"
              accept="image/*"
              capture="environment"
              multiple
              onChange={handleImageChange}
              className="hidden"
              disabled={detecting}
            />
          </label>

          {imagePreviews.length > 0 && (
            <div className="grid grid-cols-2 gap-2">
              {imagePreviews.map((src, index) => (
                <div key={index} className="relative">
                  <img
                    src={src}
                    alt={`Preview ${index}`}
                    className="w-full max-h-48 object-contain rounded-xl border border-line"
                  />

                  <button
                    disabled={detecting}
                    onClick={() => removeImage(index)}
                    className="absolute top-1 right-1 bg-black/70 text-white text-xs px-2 py-1 rounded-lg disabled:opacity-40 disabled:cursor-not-allowed"
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
            className="w-full flex items-center justify-center gap-2 rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-black disabled:opacity-50"
          >
            {detecting && <ArrowPathIcon className="h-4 w-4 animate-spin" />}
            {detecting ? "Scanning images..." : "Scan images"}
          </button>

          {scanMessage && (
            <div
              className={
                "rounded-xl p-3 text-sm border text-center " + messageStyle()
              }
            >
              {scanMessage}

              {scanStatus === "rejected" && (
                <p className="text-xs text-red-400 mt-2">
                  Tip: take a clearer photo and focus closely on the expiry date
                  label.
                </p>
              )}
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

          <Field label="Expiry date">
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

            {needsReview && (
              <p className="text-xs text-yellow-400 mt-1">
                ⚠ Please verify expiry date
              </p>
            )}
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
              <option value="">Select unit</option>

              {UNIT_GROUPS.map((group) => (
                <optgroup key={group.label} label={group.label}>
                  {group.options.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </optgroup>
              ))}
            </select>
          </Field>
        </div>

        <button
          onClick={handleSave}
          disabled={!canSave}
          className="w-full flex items-center justify-center gap-2 rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-black disabled:opacity-50"
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