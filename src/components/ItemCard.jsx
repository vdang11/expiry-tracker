import React from "react";
import { useNavigate } from "react-router-dom";

function statusBadge(expiryStatus, daysLeft) {
  if (expiryStatus === "EXPIRED") return <span className="badge danger">EXPIRED</span>;
  if (expiryStatus === "EXPIRING_SOON") return <span className="badge warn">SOON • {daysLeft}d</span>;
  return <span className="badge">OK</span>;
}

export default function ItemCard({ item }) {
  const nav = useNavigate();

  const expiryLabel = item.expiryDate ? "HSD" : item.estimatedExpiryDate ? "Ước lượng" : "N/A";
  const expiryValue = item.effectiveExpiry || "—";

  return (
    <div className="card" role="button" tabIndex={0} onClick={() => nav(`/items/${item.id}`)}>
      <div className="row" style={{ justifyContent: "space-between" }}>
        <div>
          <div style={{ fontWeight: 800, fontSize: 16 }}>{item.name}</div>
          <div style={{ color: "var(--muted)", fontSize: 13 }}>
            {expiryLabel}: {expiryValue} • {item.storageType}
          </div>
        </div>
        <div>{statusBadge(item.expiryStatus, item.daysLeft)}</div>
      </div>
    </div>
  );
}
