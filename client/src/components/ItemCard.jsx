import { useNavigate } from "react-router-dom";

function statusBadge(expiryStatus, daysLeft) {
  if (expiryStatus === "EXPIRED") {
    return (
      <span className="text-red-400 text-xs font-semibold">
        EXPIRED
      </span>
    );
  }

  if (expiryStatus === "EXPIRING_SOON") {
    return (
      <span className="text-yellow-300 text-xs font-semibold">
        SOON • {daysLeft}d
      </span>
    );
  }

  return <span className="text-muted text-xs">OK</span>;
}

export default function ItemCard({ item }) {
  const nav = useNavigate();

  const expiryLabel = item.expiryDate
    ? "HSD"
    : item.estimatedExpiryDate
    ? "Ước lượng"
    : "N/A";

  const expiryValue =
    item.expiryDate || item.estimatedExpiryDate || "—";

  function handleClick() {
    nav(`/items/${item.id}`);
  }

  return (
    <div
      className="card cursor-pointer"
      role="button"
      tabIndex={0}
      onClick={handleClick}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          handleClick();
        }
      }}
    >
      <div className="flex justify-between items-start">
        <div>
          <div className="font-bold text-base">
            {item.productName || "Unknown item"}
          </div>

          <div className="text-muted text-sm">
            {expiryLabel}: {expiryValue} • {item.storageType || "—"}
          </div>
        </div>

        <div>
          {statusBadge(item.expiryStatus, item.daysLeft)}
        </div>
      </div>
    </div>
  );
}