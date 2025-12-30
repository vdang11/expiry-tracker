import React, { useMemo, useState } from "react";
import { enums } from "../data/mockApi.js";

export default function ItemForm({ initial, onSubmit, submitLabel = "Save Item" }) {
  const [form, setForm] = useState(() => ({
    name: "",
    category: "VEGETABLE",
    purchaseDate: "",
    expiryDate: "",
    estimatedExpiryDate: "",
    quantity: 1,
    unit: "pcs",
    storageType: "FRIDGE",
    source: "manual",
    ...initial,
  }));

  const hasExplicitExpiry = useMemo(() => Boolean(form.expiryDate), [form.expiryDate]);

  function update(key, value) {
    setForm((p) => ({ ...p, [key]: value }));
  }

  function handleSubmit(e) {
    e.preventDefault();

    const payload = {
      name: form.name.trim(),
      category: form.category,
      purchaseDate: form.purchaseDate,
      expiryDate: form.expiryDate || null,
      estimatedExpiryDate: hasExplicitExpiry ? null : (form.estimatedExpiryDate || null),
      quantity: Number(form.quantity),
      unit: form.unit,
      storageType: form.storageType,
      source: form.source,
    };

    onSubmit(payload);
  }

  return (
    <form onSubmit={handleSubmit} className="card" style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <div className="field">
        <label>Name</label>
        <input value={form.name} onChange={(e) => update("name", e.target.value)} placeholder="e.g. Trứng gà" required />
      </div>

      <div className="grid2">
        <div className="field">
          <label>Category</label>
          <select value={form.category} onChange={(e) => update("category", e.target.value)}>
            {enums.category.map(([v, label]) => (
              <option key={v} value={v}>{label}</option>
            ))}
          </select>
        </div>

        <div className="field">
          <label>Storage</label>
          <select value={form.storageType} onChange={(e) => update("storageType", e.target.value)}>
            {enums.storageType.map(([v, label]) => (
              <option key={v} value={v}>{label}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="grid2">
        <div className="field">
          <label>Purchase date</label>
          <input type="date" value={form.purchaseDate} onChange={(e) => update("purchaseDate", e.target.value)} required />
        </div>

        <div className="field">
          <label>Expiry date (optional)</label>
          <input type="date" value={form.expiryDate || ""} onChange={(e) => update("expiryDate", e.target.value)} />
        </div>
      </div>

      {!hasExplicitExpiry && (
        <div className="field">
          <label>Estimated expiry (optional)</label>
          <input
            type="date"
            value={form.estimatedExpiryDate || ""}
            onChange={(e) => update("estimatedExpiryDate", e.target.value)}
          />
        </div>
      )}

      <div className="grid2">
        <div className="field">
          <label>Quantity</label>
          <input type="number" step="0.1" value={form.quantity} onChange={(e) => update("quantity", e.target.value)} />
        </div>
        <div className="field">
          <label>Unit</label>
          <select value={form.unit} onChange={(e) => update("unit", e.target.value)}>
            {enums.unit.map(([v, label]) => (
              <option key={v} value={v}>{label}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="row" style={{ justifyContent: "flex-end" }}>
        <button className="btn primary" type="submit">{submitLabel}</button>
      </div>
    </form>
  );
}
