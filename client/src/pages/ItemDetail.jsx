import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api/apiClient";
import toast from "react-hot-toast";
import ConfirmModal from "../components/ConfirmModal";

export default function ItemDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [consuming, setConsuming] = useState(false);
  const daysLeft = calculateDaysLeft(item?.expiryDate);

  // ================= LOAD ITEM =================
  useEffect(() => {
    let alive = true;

    (async () => {
      try {
        const data = await api.getItemById(id);

        if (alive) setItem(data);
      } catch (e) {
        console.error(e);
        toast.error("Failed to load item");
      } finally {
        if (alive) setLoading(false);
      }
    })();

    return () => {
      alive = false;
    };
  }, [id]);

  // ================= DELETE =================
  function handleDeleteClick() {
    setShowDeleteModal(true);
  }

  async function confirmDelete() {
    if (!item) return;

    setDeleting(true);

    try {
      await api.deleteItem(item.id);

      toast.success("Item deleted");

      navigate("/");
    } catch (e) {
      console.error(e);
      toast.error("Delete failed");
    } finally {
      setDeleting(false);
      setShowDeleteModal(false);
    }
  }

  // ================= CONSUME =================
  async function handleConsume() {
    if (!item) return;

    setConsuming(true);

    try {
      await api.consumeItem(item.id);

      toast.success("Marked as consumed");

      navigate("/");
    } catch (e) {
      console.error(e);
      toast.error(e.message || "Update failed");
    } finally {
      setConsuming(false);
    }
  }
  // ================= UI =================
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

  return (
    <div className="space-y-4">
      {/* HEADER */}
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">
          {item.productName}
        </h2>

        <button
          onClick={() => navigate("/")}
          className="rounded-xl border border-line px-3 py-2 text-sm text-muted hover:text-white"
        >
          Back
        </button>
      </div>

      {/* INFO */}
      <div className="rounded-2xl border border-line bg-card p-4 space-y-3">

        <Row label="Expiry date" value={item.expiryDate} />
        <Row label="Days left" value={formatDaysLeft(daysLeft)} />
        <Row label="Date type" value={item.dateType} />
        <Row label="Decision status" value={item.decisionStatus} />
        <Row label="Suggested action" value={item.suggestedAction} />
        <Row label="Confidence" value={`${item.confidence * 100}%`} />
        <Row label="Item status" value={item.itemStatus} />

        <div className="pt-2 flex gap-2">
          <StatusBadge expiryStatus={item.expiryStatus} />
          <ItemStatusBadge itemStatus={item.itemStatus} />
        </div>
      </div>

      {/* ACTIONS */}
      <div className="flex gap-2">
        <button
          disabled={consuming || item.itemStatus === "CONSUMED"}
          onClick={handleConsume}
          className="flex-1 rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-black disabled:opacity-50"
        >
          {consuming ? "Updating..." : "Mark as consumed"}
        </button>

        <button
          disabled={deleting}
          onClick={handleDeleteClick}
          className="flex-1 rounded-xl border border-red-400/40 bg-card px-4 py-2 text-sm font-semibold text-red-300 disabled:opacity-50"
        >
          Delete
        </button>
      </div>

      {/* MODAL */}
      <ConfirmModal
        open={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        onConfirm={confirmDelete}
      />
    </div>
  );
}

/* ================= COMPONENTS ================= */

function Row({ label, value }) {
  return (
    <div className="flex items-center justify-between gap-4 text-sm">
      <span className="text-muted">{label}</span>
      <span className="text-white">{value || "—"}</span>
    </div>
  );
}

function StatusBadge({ expiryStatus }) {
  if (expiryStatus === "EXPIRED") {
    return (
      <span className="rounded-full border border-red-400/40 px-2 py-1 text-xs text-red-300">
        Expired
      </span>
    );
  }

  if (expiryStatus === "EXPIRING_SOON") {
    return (
      <span className="rounded-full border border-yellow-400/40 px-2 py-1 text-xs text-yellow-200">
        Expiring soon
      </span>
    );
  }

  return (
    <span className="rounded-full border border-line px-2 py-1 text-xs text-muted">
      Fresh
    </span>
  );
}

function ItemStatusBadge({ itemStatus }) {
  if (itemStatus === "CONSUMED") {
    return (
      <span className="rounded-full border border-green-400/40 px-2 py-1 text-xs text-green-300">
        Consumed
      </span>
    );
  }

  return (
    <span className="rounded-full border border-line px-2 py-1 text-xs text-muted">
      Active
    </span>
  );
}

function calculateDaysLeft(expiryDate) {
  if (!expiryDate) return null;

  const today = new Date();
  const exp = new Date(expiryDate);

  today.setHours(0, 0, 0, 0);
  exp.setHours(0, 0, 0, 0);

  const diffMs = exp - today;
  return Math.round(diffMs / (1000 * 60 * 60 * 24));
}

function formatDaysLeft(days) {
  if (days === null) return "—";

  if (days < 0) return `Expired ${Math.abs(days)} day(s) ago`;
  if (days === 0) return "Expires today";
  return `${days} day(s) left`;
}
