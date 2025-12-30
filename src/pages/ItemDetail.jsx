import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../data/mockApi";

export default function ItemDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let alive = true;

    (async () => {
      try {
        const data = await api.getItemById(id);
        if (alive) setItem(data);
      } catch (e) {
        console.error(e);
      } finally {
        if (alive) setLoading(false);
      }
    })();

    return () => {
      alive = false;
    };
  }, [id]);

  if (loading) {
    return (
      <div className="rounded-2xl border border-line bg-card p-4 text-muted">
        Loading item...
      </div>
    );
  }

  if (!item) {
    return (
      <div className="rounded-2xl border border-line bg-card p-4 text-muted">
        Item not found
      </div>
    );
  }

  async function handleDelete() {
    await api.deleteItem(item.id);
    navigate("/");
  }

  async function handleConsume() {
    await api.updateItem(item.id, { status: "CONSUMED" });
    navigate("/");
  }

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">{item.name}</h2>
        <button
          onClick={() => navigate("/")}
          className="rounded-xl border border-line px-3 py-2 text-sm text-muted hover:text-white"
        >
          Back
        </button>
      </div>

      {/* Info card */}
      <div className="rounded-2xl border border-line bg-card p-4 space-y-3">
        <Row label="Category" value={item.category} />
        <Row label="Storage" value={item.storageType} />
        <Row
          label="Quantity"
          value={`${item.quantity} ${item.unit}`}
        />
        <Row label="Purchase date" value={item.purchaseDate} />
        <Row
          label="Expiry date"
          value={item.effectiveExpiry || "—"}
        />
        <Row
          label="Days left"
          value={item.daysLeft ?? "—"}
        />

        <div className="pt-2">
          <StatusBadge status={item.expiryStatus} />
        </div>
      </div>

      {/* Actions */}
      <div className="flex gap-2">
        <button
          onClick={handleConsume}
          className="flex-1 rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-black"
        >
          Mark as consumed
        </button>

        <button
          onClick={handleDelete}
          className="flex-1 rounded-xl border border-red-400/40 bg-card px-4 py-2 text-sm font-semibold text-red-300"
        >
          Delete
        </button>
      </div>
    </div>
  );
}

/* ----------------- Small reusable UI parts ----------------- */

function Row({ label, value }) {
  return (
    <div className="flex items-center justify-between gap-4 text-sm">
      <span className="text-muted">{label}</span>
      <span className="text-white">{value}</span>
    </div>
  );
}

function StatusBadge({ status }) {
  if (status === "EXPIRED") {
    return (
      <span className="inline-block rounded-full border border-red-400/40 px-3 py-1 text-xs text-red-300">
        Expired
      </span>
    );
  }

  if (status === "EXPIRING_SOON") {
    return (
      <span className="inline-block rounded-full border border-yellow-400/40 px-3 py-1 text-xs text-yellow-200">
        Expiring soon
      </span>
    );
  }

  return (
    <span className="inline-block rounded-full border border-line px-3 py-1 text-xs text-muted">
      OK
    </span>
  );
}
