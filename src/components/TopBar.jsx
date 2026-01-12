import { LogOut } from "lucide-react";
import { useNavigate } from "react-router-dom";

export default function TopBar({ search, onSearchChange }) {
  const navigate = useNavigate();
  const currentUser = JSON.parse(localStorage.getItem("currentUser"));
  const initials = currentUser?.username?.[0]?.toUpperCase() || "?";

  const handleLogout = () => {
    localStorage.removeItem("currentUser");
    localStorage.removeItem("expiry-tracker-items");
    navigate("/signup", { replace: true });
  };

  return (
    <div className="px-4 py-3 border-b border-line space-y-3">
      {/* Top row: title + user */}
      <div className="flex items-center justify-between">
        {/* App title */}
        <div className="text-lg font-semibold tracking-tight">
          Expiry Tracker
        </div>

        {/* User + Logout */}
        <div className="flex items-center gap-3">
          <span className="size-6 flex items-center justify-center rounded-full bg-slate-700 text-xs font-medium">
            {initials}
          </span>

          <span className="text-xs opacity-80 text-muted">
            {currentUser?.username}
          </span>

          <button
            onClick={handleLogout}
            className="p-1 rounded-md border border-line hover:border-accent hover:text-white transition"
            title="Logout"
          >
            <LogOut size={14} />
          </button>
        </div>
      </div>

      {/* Search input */}
      <input
        value={search}
        onChange={(e) => onSearchChange(e.target.value)}
        placeholder="Search..."
        className="w-full rounded-lg bg-slate-800 border border-line px-3 py-2 text-sm text-white placeholder-muted focus:border-accent/70 outline-none"
      />
    </div>
  );
}
