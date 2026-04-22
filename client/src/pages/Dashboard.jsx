import { useEffect, useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import { api } from "../api/apiClient";
import {
  getCurrentUser,
  subscribeAuthChange,
} from "../api/authStorage";

const PAGE_SIZE = 20;

export default function Dashboard() {
  const navigate = useNavigate();
  const ctx = useOutletContext() || {};
  const search = ctx.search || "";

  const [userId, setUserId] = useState(() => getCurrentUser()?.id || null);
  const [items, setItems] = useState([]);
  const [filter, setFilter] = useState("all");
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [summary, setSummary] = useState({
    expired: 0,
    soon: 0,
    ok: 0,
  });

  useEffect(() => {
    const unsubscribe = subscribeAuthChange(() => {
      setUserId(getCurrentUser()?.id || null);
    });

    return unsubscribe;
  }, []);

  useEffect(() => {
    if (userId === null) {
      navigate("/login", { replace: true });
    }
  }, [userId, navigate]);

  useEffect(() => {
    setPage(0);
  }, [search, filter]);

  useEffect(() => {
    if (!userId) {
      setItems([]);
      setSummary({
        expired: 0,
        soon: 0,
        ok: 0,
      });
      setTotalPages(0);
      setLoading(false);
      return;
    }

    let alive = true;

    async function loadData() {
      try {
        setLoading(true);

        const [itemsRes, summaryData] = await Promise.all([
          api.getItems(page, PAGE_SIZE, search, filter, "expiryDate", "asc"),
          api.getSummary(),
        ]);

        if (!alive) return;

        setItems(itemsRes?.content || []);
        setTotalPages(itemsRes?.totalPages || 0);

        setSummary({
          expired: summaryData?.expired || 0,
          soon: summaryData?.expiringSoon || 0,
          ok: summaryData?.fresh || 0,
        });
      } catch (e) {
        console.error(e);
      } finally {
        if (alive) {
          setLoading(false);
        }
      }
    }

    loadData();

    return () => {
      alive = false;
    };
  }, [userId, page, search, filter]);

  function handleFilterChange(nextFilter) {
    setFilter(nextFilter);
  }

  return (
    <div className="space-y-4">
      {/* ================= HEADER ================= */}
      <div className="flex justify-between items-center p-2">
        <h2 className="text-lg font-semibold">Expiry Overview</h2>

        <button
          onClick={() => navigate("/add")}
          className="rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-black"
        >
          + Add
        </button>
      </div>

      {/* ================= SUMMARY ================= */}
      <div className="grid grid-cols-3 gap-3">
        <SummaryCard
          label="Expired"
          value={summary.expired}
          active={filter === "expired"}
          tone="danger"
          onClick={() => handleFilterChange("expired")}
        />

        <SummaryCard
          label="Expiring soon"
          value={summary.soon}
          active={filter === "soon"}
          tone="warn"
          onClick={() => handleFilterChange("soon")}
        />

        <SummaryCard
          label="OK"
          value={summary.ok}
          active={filter === "ok"}
          tone="neutral"
          onClick={() => handleFilterChange("ok")}
        />
      </div>

      {/* ================= FILTER PILLS ================= */}
      <div className="flex flex-wrap gap-2">
        <Pill active={filter === "all"} onClick={() => handleFilterChange("all")}>
          All
        </Pill>

        <Pill
          active={filter === "expired"}
          onClick={() => handleFilterChange("expired")}
        >
          Expired
        </Pill>

        <Pill active={filter === "soon"} onClick={() => handleFilterChange("soon")}>
          Expiring soon
        </Pill>

        <Pill active={filter === "ok"} onClick={() => handleFilterChange("ok")}>
          OK
        </Pill>
      </div>

      {/* ================= LIST ================= */}
      <div className="space-y-3">
        {loading && (
          <div className="rounded-2xl border border-line bg-card p-4 text-muted">
            Loading items...
          </div>
        )}

        {!loading && items.length === 0 && (
          <div className="rounded-2xl border border-line bg-card p-4 text-muted">
            No matching items
          </div>
        )}

        {!loading &&
          items.map((it) => {
            const daysLeft = calculateDaysLeft(it?.expiryDate);

            return (
              <button
                key={it?.id}
                onClick={() => navigate(`/items/${it?.id}`)}
                className="w-full rounded-2xl border border-line bg-card p-4 text-left transition hover:border-accent/50"
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="text-base font-semibold">
                      {it?.productName || "Unknown item"}
                    </div>

                    <div className="mt-1 text-sm text-muted">
                      Exp: {it?.expiryDate || "—"}
                    </div>

                    <div className="text-xs text-muted">
                      {formatDaysLeft(daysLeft)}
                    </div>
                  </div>

                  <StatusBadge
                    expiryStatus={it?.expiryStatus}
                    itemStatus={it?.itemStatus}
                  />
                </div>
              </button>
            );
          })}
      </div>

      {/* ================= PAGINATION ================= */}
      <div className="flex justify-center gap-2">
        <button
          disabled={page === 0}
          onClick={() => setPage((p) => p - 1)}
          className="px-3 py-1 border border-line rounded"
        >
          Prev
        </button>

        <span className="text-sm text-muted">
          Page {totalPages === 0 ? 0 : page + 1} / {totalPages}
        </span>

        <button
          disabled={page >= totalPages - 1 || totalPages === 0}
          onClick={() => setPage((p) => p + 1)}
          className="px-3 py-1 border border-line rounded"
        >
          Next
        </button>
      </div>
    </div>
  );
}

function SummaryCard({ label, value, active, tone, onClick }) {
  const toneClass =
    tone === "danger"
      ? "text-red-300"
      : tone === "warn"
      ? "text-yellow-200"
      : "text-white";

  return (
    <button
      onClick={onClick}
      className={
        "rounded-2xl border p-4 text-left transition " +
        (active
          ? "border-accent/60"
          : "border-line hover:border-accent/40")
      }
    >
      <div className={`text-2xl font-bold ${toneClass}`}>{value}</div>
      <div className="mt-1 text-sm text-muted">{label}</div>
    </button>
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

function StatusBadge({ expiryStatus, itemStatus }) {
  if (itemStatus === "CONSUMED") {
    return (
      <span className="rounded-full border border-green-400/40 px-2 py-1 text-xs text-green-300">
        Consumed
      </span>
    );
  }

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