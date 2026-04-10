import { useEffect, useMemo, useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import { api } from "../api/apiClient";

export default function Dashboard() {
  const navigate = useNavigate();

  const ctx = useOutletContext() || {};
  const search = ctx.search || "";

  const [items, setItems] = useState([]);
  const [filter, setFilter] = useState("all");
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState({
    expired: 0,
    soon: 0,
    ok: 0
  });

  // ================= USER =================
const currentUser = useMemo(() => {
  try {
    const raw = localStorage.getItem("currentUser");
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}, [window.location.href]); // 🔥 KEY FIX

  // redirect nếu chưa login
  useEffect(() => {
    if (!currentUser) {
      navigate("/login", { replace: true });
    }
  }, [currentUser, navigate]);

  // ================= LOAD ITEMS =================
  useEffect(() => {
    if (!currentUser) return;

    let alive = true;

    (async () => {
      try {
        const data = await api.getItems();

        if (alive) {
          setItems(data || []);
        }
      } catch (e) {
        console.error(e);
      } finally {
        if (alive) setLoading(false);
      }
    })();

    return () => {
      alive = false;
    };
  }, [currentUser]);

  // ================= LOAD SUMMARY =================
  useEffect(() => {
    if (!currentUser) return;

    (async () => {
      try {
        const data = await api.getSummary();

        setSummary({
          expired: data.expired,
          soon: data.expiringSoon,
          ok: data.fresh
        });
      } catch (e) {
        console.error(e);
      }
    })();
  }, [currentUser]);

  // ================= FILTERING + =================
  const filteredItems = useMemo(() => {

    let list = items.filter(it => it?.itemStatus !== "CONSUMED");

    // search
    if (search && search.trim()) {
      const q = search.toLowerCase();

      list = list.filter(it =>
        (it?.productName || "").toLowerCase().includes(q)
      );
    }

    // filter
    if (filter !== "all") {
      list = list.filter(it => {
        if (filter === "expired") return it?.expiryStatus === "EXPIRED";
        if (filter === "soon") return it?.expiryStatus === "EXPIRING_SOON";
        if (filter === "ok") return it?.expiryStatus === "FRESH";
        return true;
      });
    }

    // ✅ SORT theo expiryDate (gần nhất trước)
    list.sort((a, b) => {
      if (!a.expiryDate) return 1;
      if (!b.expiryDate) return -1;

      return new Date(a.expiryDate) - new Date(b.expiryDate);
    });

    return list;

  }, [items, search, filter]);

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
          onClick={() => setFilter("expired")}
        />

        <SummaryCard
          label="Expiring soon"
          value={summary.soon}
          active={filter === "soon"}
          tone="warn"
          onClick={() => setFilter("soon")}
        />

        <SummaryCard
          label="OK"
          value={summary.ok}
          active={filter === "ok"}
          tone="neutral"
          onClick={() => setFilter("ok")}
        />
      </div>

      {/* ================= FILTER PILLS ================= */}
      <div className="flex flex-wrap gap-2">
        <Pill active={filter === "all"} onClick={() => setFilter("all")}>
          All
        </Pill>

        <Pill active={filter === "expired"} onClick={() => setFilter("expired")}>
          Expired
        </Pill>

        <Pill active={filter === "soon"} onClick={() => setFilter("soon")}>
          Expiring soon
        </Pill>

        <Pill active={filter === "ok"} onClick={() => setFilter("ok")}>
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

        {!loading && filteredItems.length === 0 && (
          <div className="rounded-2xl border border-line bg-card p-4 text-muted">
            No matching items
          </div>
        )}

        {!loading &&
          filteredItems.map((it) => {
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

                    {/* ✅ NEW: days left */}
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
  // ưu tiên hiển thị consumed
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
// ================= HELPER =================
function calculateDaysLeft(expiryDate) {
  if (!expiryDate) return null;

  const today = new Date();
  const exp = new Date(expiryDate);

  // normalize về 00:00
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