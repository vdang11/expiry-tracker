import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CalendarDaysIcon, ArrowPathIcon } from "@heroicons/react/24/outline";
import toast from "react-hot-toast";
import { api } from "../api/apiClient";
import "react-datepicker/dist/react-datepicker.css";
import { getCurrentUser } from "../api/authStorage";

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
      { value: "slice", label: "Slice" },
    ],
  },
  {
    label: "Weight",
    options: [
      { value: "g", label: "Gram (g)" },
      { value: "kg", label: "Kilogram (kg)" },
    ],
  },
  {
    label: "Liquid",
    options: [
      { value: "ml", label: "Milliliter (ml)" },
      { value: "L", label: "Liter (L)" },
    ],
  },
  {
    label: "Fresh Produce",
    options: [{ value: "bunch", label: "Bunch" }],
  },
];

function safeRevokeObjectUrl(url) {
  if (!url) return;

  try {
    URL.revokeObjectURL(url);
  } catch (error) {
    console.debug("Failed to revoke object URL:", error);
  }
}

function todayISO() {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
}

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

  const today = useMemo(() => todayISO(), []);

  useEffect(() => {
    previewRef.current = imagePreviews;
  }, [imagePreviews]);

  useEffect(() => {
    return () => {
      previewRef.current.forEach((url) => {
        safeRevokeObjectUrl(url);
      });
    };
  }, []);

  function resetScanState() {
    setScanMessage("");
    setScanStatus("idle");
    setNeedsReview(false);
    setProductNameAccepted(true);
  }

  function clearImages() {
    previewRef.current.forEach((url) => {
      safeRevokeObjectUrl(url);
    });

    setImageFiles([]);
    setImagePreviews([]);
  }

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
          oldFile.name === newFile.name && oldFile.size === newFile.size
      );
    });

    if (!uniqueFiles.length) {
      toast.error("Duplicate image selected.");
      e.target.value = null;
      return;
    }

    const newPreviews = uniqueFiles.map((file) => URL.createObjectURL(file));

    setImageFiles((prev) => [...prev, ...uniqueFiles]);
    setImagePreviews((prev) => [...prev, ...newPreviews]);

    resetScanState();
    e.target.value = null;
  }

  function removeImage(index) {
    if (detecting) return;

    const url = imagePreviews[index];
    safeRevokeObjectUrl(url);

    const updatedFiles = imageFiles.filter((_, i) => i !== index);
    const updatedPreviews = imagePreviews.filter((_, i) => i !== index);

    setImageFiles(updatedFiles);
    setImagePreviews(updatedPreviews);

    resetScanState();

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

      imageFiles.forEach((file) => {
        formData.append("images", file);
      });

      const data = await api.scanImages(formData);

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
    } catch (error) {
      console.error(error);
      toast.error(error.message || "Cannot reach server. Please try again.");
    } finally {
      setDetecting(false);
    }
  }

  function validateForm() {
    const trimmedName = name.trim();

    if (!trimmedName) {
      return "Please enter food name.";
    }

    if (!purchaseDate) {
      return "Please select purchase date.";
    }

    if (!expiryDate) {
      return "Please select expiry date.";
    }

    if (purchaseDate > today) {
      return "Purchase date cannot be in the future.";
    }

    if (expiryDate < purchaseDate) {
      return "Expiry date must be after purchase date.";
    }

    if (quantity <= 0) {
      return "Quantity must be greater than 0.";
    }

    if (!unit) {
      return "Please select a unit.";
    }

    return null;
  }

  const canSave =
    !detecting &&
    Boolean(name.trim()) &&
    quantity > 0 &&
    Boolean(unit) &&
    Boolean(purchaseDate) &&
    Boolean(expiryDate) &&
    (mode === "manual" || imageFiles.length > 0);

  async function handleSave() {
    const validationError = validateForm();

    if (validationError) {
      toast.error(validationError);
      return;
    }

    const currentUser = getCurrentUser();

    if (!currentUser?.id) {
      toast.error("User not logged in.");
      return;
    }

    const payload = {
      productName: name.trim(),
      expiryDate,
      confidence: 1.0,
      dateType: "CONFIRMED",
      decisionStatus: "CONFIRMED",
      suggestedAction: "KEEP",
    };

    try {
      await api.saveItem(payload);

      toast.success("Item added successfully");
      navigate("/dashboard");
    } catch (error) {
      console.error(error);
      toast.error(error.message || "Cannot reach server.");
    }
  }

  function switchMode(next) {
    if (detecting) return;

    setMode(next);

    if (next !== "scan") {
      clearImages();
      resetScanState();
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
        <div className="space-y-3 rounded-2xl border border-line bg-card p-4">
          <p className="text-center text-sm text-muted">
            Take photos of the product and expiry date
          </p>

          <label
            className={
              "flex items-center justify-center gap-2 rounded-xl px-4 py-3 text-sm font-semibold text-black " +
              (detecting
                ? "cursor-not-allowed bg-gray-400"
                : "cursor-pointer bg-accent")
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
                    alt={`Preview ${index + 1}`}
                    className="max-h-48 w-full rounded-xl border border-line object-contain"
                  />

                  <button
                    type="button"
                    disabled={detecting}
                    onClick={() => removeImage(index)}
                    className="absolute right-1 top-1 rounded-lg bg-black/70 px-2 py-1 text-xs text-white disabled:cursor-not-allowed disabled:opacity-40"
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          )}

          <button
            type="button"
            onClick={handleScan}
            disabled={detecting || imageFiles.length === 0}
            className="flex w-full items-center justify-center gap-2 rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-black disabled:opacity-50"
          >
            {detecting && <ArrowPathIcon className="h-4 w-4 animate-spin" />}
            {detecting ? "Scanning images..." : "Scan images"}
          </button>

          {scanMessage && (
            <div
              className={
                "rounded-xl border p-3 text-center text-sm " + messageStyle()
              }
            >
              {scanMessage}

              {scanStatus === "rejected" && (
                <p className="mt-2 text-xs text-red-400">
                  Tip: take a clearer photo and focus closely on the expiry date
                  label.
                </p>
              )}
            </div>
          )}
        </div>
      )}

      <div className="space-y-3 rounded-2xl border border-line bg-card p-4">
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

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field label="Purchase date">
            <div className="relative">
              <input
                type="date"
                value={purchaseDate}
                max={today}
                onChange={(e) => setPurchaseDate(e.target.value)}
                className="w-full appearance-none rounded-xl border border-line bg-bg px-3 py-3 text-base sm:py-2 sm:text-sm"
              />

              <CalendarDaysIcon className="pointer-events-none absolute right-3 top-1/2 hidden h-5 w-5 -translate-y-1/2 text-muted sm:block" />
            </div>
          </Field>

          <Field label="Expiry date">
            <div className="relative">
              <input
                type="date"
                value={expiryDate}
                min={today}
                onChange={(e) => setExpiryDate(e.target.value)}
                className="w-full appearance-none rounded-xl border border-line bg-bg px-3 py-3 text-base sm:py-2 sm:text-sm"
              />

              <CalendarDaysIcon className="pointer-events-none absolute right-3 top-1/2 hidden h-5 w-5 -translate-y-1/2 text-muted sm:block" />
            </div>

            {needsReview && (
              <p className="mt-1 text-xs text-yellow-400">
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
              onChange={(e) => setQuantity(Number(e.target.value) || 0)}
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
          type="button"
          onClick={handleSave}
          disabled={!canSave}
          className="flex w-full items-center justify-center gap-2 rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-black disabled:opacity-50"
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
      type="button"
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