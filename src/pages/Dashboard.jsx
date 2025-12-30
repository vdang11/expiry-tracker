import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../data/mockApi";

export default function Dashboard({ search = "" }) {
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [filter, setFilter] = useState("all"); // all | expired | soon | ok
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let alive = true;

    (async () => {
      try {
        const data = await api.getItems();
        if (alive) setItems(data);
      } finally {
        if (alive) setLoading(false);
      }
    })();

    return () => {
      alive = false;
    };
  }, []);

  // 🔢 Summary counts
  const summary = useMemo(() => {
    let expired = 0;
    let soon = 0;
    let ok = 0;

    for (const it of items) {
      if (it.expiryStatus === "EXPIRED") expired++;
      else if (it.expiryStatus === "EXPIRING_SOON") soon++;
      else ok++;
    }

    return { expired, soon, ok };
  }, [items]);

  // 🔍 Filtered list
  const filteredItems = useMemo(() => {
    let list = items;

    if (search.trim()) {
      const q = search.toLowerCase();
      list = list.filter((it) =>
        it.name.toLowerCase().includes(q)
      );
    }

    if (filter !== "all") {
      list = list.filter((it) => {
        if (filter === "expired") return it.expiryStatus === "EXPIRED";
        if (filter === "soon") return it.expiryStatus === "EXPIRING_SOON";
        if (filter === "ok") return it.expiryStatus === "OK";
        return true;
      });
    }

    return list;
  }, [items, search, filter]);

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">Dashboard</h2>
        <button
          onClick={() => navigate("/add")}
          className="rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-black"
        >
          + Add
        </button>
      </div>

      {/* 🧮 Summary bar */}
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

      {/* Filter pills */}
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

      {/* Content */}
      <div className="space-y-3">
        {loading && (
          <div className="rounded-2xl border border-line bg-card p-4 text-muted">
            Loading items...
          </div>
        )}

        {!loading && filteredItems.length === 0 && (
          <div className="rounded-2xl border border-line bg-card p-4 text-muted">
            Không có thực phẩm phù hợp
          </div>
        )}

        {!loading &&
          filteredItems.map((it) => (
            <button
              key={it.id}
              onClick={() => navigate(`/items/${it.id}`)}
              className="w-full rounded-2xl border border-line bg-card p-4 text-left transition hover:border-accent/50"
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="text-base font-semibold">{it.name}</div>
                  <div className="mt-1 text-sm text-muted">
                    Exp: {it.effectiveExpiry || "—"}
                  </div>
                </div>

                <StatusBadge
                  status={it.expiryStatus}
                  daysLeft={it.daysLeft}
                />
              </div>
            </button>
          ))}
      </div>
    </div>
  );
}

/* ---------------- UI helpers ---------------- */

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

function StatusBadge({ status, daysLeft }) {
  if (status === "EXPIRED") {
    return (
      <span className="rounded-full border border-red-400/40 px-2 py-1 text-xs text-red-300">
        Expired
      </span>
    );
  }

  if (status === "EXPIRING_SOON") {
    return (
      <span className="rounded-full border border-yellow-400/40 px-2 py-1 text-xs text-yellow-200">
        {daysLeft}d
      </span>
    );
  }

  return (
    <span className="rounded-full border border-line px-2 py-1 text-xs text-muted">
      OK
    </span>
  );
}
