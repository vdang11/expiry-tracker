import { useEffect, useMemo, useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import { api } from "../data/mockApi";

export default function Dashboard() {

  const navigate = useNavigate();

  const ctx = useOutletContext() || {};
  const search = ctx.search || "";

  const [items, setItems] = useState([]);
  const [filter, setFilter] = useState("all");
  const [loading, setLoading] = useState(true);

  const currentUser = useMemo(() => {
    try {
      const raw = localStorage.getItem("currentUser");
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }, []);

  // redirect nếu chưa login
  useEffect(() => {
    if (!currentUser) {
      navigate("/login", { replace: true });
    }
  }, [currentUser, navigate]);

  // load items
  useEffect(() => {

    if (!currentUser) return;

    let alive = true;

    (async () => {
      try {

        const data = await api.getItems();

        if (alive) {
          setItems(data || []);
        }

      } finally {
        if (alive) setLoading(false);
      }
    })();

    return () => {
      alive = false;
    };

  }, [currentUser]);

  // summary
  const summary = useMemo(() => {

    let expired = 0;
    let soon = 0;
    let ok = 0;

    for (const it of items) {

      if (it?.expiryStatus === "EXPIRED") expired++;

      else if (it?.expiryStatus === "EXPIRING_SOON") soon++;

      else if (it?.expiryStatus === "OK") ok++;

    }

    return { expired, soon, ok };

  }, [items]);

  // filtering
  const filteredItems = useMemo(() => {

    let list = [...items];

    // search
    if (search && search.trim()) {

      const q = search.toLowerCase();

      list = list.filter((it) => {

        const name = (it?.name || "").toLowerCase();

        return name.includes(q);

      });

    }

    // filter status
    if (filter !== "all") {

      list = list.filter((it) => {

        if (filter === "expired") return it?.expiryStatus === "EXPIRED";

        if (filter === "soon") return it?.expiryStatus === "EXPIRING_SOON";

        if (filter === "ok") return it?.expiryStatus === "OK";

        return true;

      });

    }

    return list;

  }, [items, search, filter]);

  return (

    <div className="space-y-4">

      {/* header */}
      <div className="flex justify-between items-center p-2">

        <h2 className="text-lg font-semibold">
          Expiry Overview
        </h2>

        <button
          onClick={() => navigate("/add")}
          className="rounded-xl bg-accent px-4 py-2 text-sm font-semibold text-black"
        >
          + Add
        </button>

      </div>

      {/* summary cards */}
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

      {/* filter pills */}
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

      {/* list */}
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
              key={it?.id}
              onClick={() => navigate(`/items/${it?.id}`)}
              className="w-full rounded-2xl border border-line bg-card p-4 text-left transition hover:border-accent/50"
            >

              <div className="flex items-start justify-between gap-3">

                <div>

                  <div className="text-base font-semibold">
                    {it?.name || "Unknown item"}
                  </div>

                  <div className="mt-1 text-sm text-muted">
                    Exp: {it?.effectiveExpiry || "—"}
                  </div>

                </div>

                <StatusBadge
                  status={it?.expiryStatus}
                  daysLeft={it?.daysLeft}
                />

              </div>

            </button>

          ))}

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
      <div className={`text-2xl font-bold ${toneClass}`}>
        {value}
      </div>

      <div className="mt-1 text-sm text-muted">
        {label}
      </div>

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
        {(daysLeft ?? "?")}d
      </span>
    );
  }

  if (status === "CONSUMED") {
    return (
      <span className="rounded-full border border-red-400/40 px-2 py-1 text-xs text-red-300">
        Consumed
      </span>
    );
  }

  return (
    <span className="rounded-full border border-line px-2 py-1 text-xs text-muted">
      OK
    </span>
  );
}