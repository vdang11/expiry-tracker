import { useNavigate, useLocation } from "react-router-dom";
import Logo from "../assets/logo.svg";

export default function TopBar({ search = "", onSearchChange }) {
  const navigate = useNavigate();
  const location = useLocation();

  const showSearch = location.pathname === "/";

  return (
    <header className="sticky top-0 z-10 border-b border-line bg-bg/80 backdrop-blur">
      <div className="mx-auto flex max-w-3xl items-center gap-3 px-4 py-3">
        {/* Logo + App name */}
        <button
          onClick={() => navigate("/")}
          className="flex items-center gap-2"
        >
          <img
            src={Logo}
            alt="Expiry Tracker"
            className="h-8 w-8"
          />
          <span className="whitespace-nowrap text-lg font-bold">
            Expiry Tracker
          </span>
        </button>

        {/* Search (Dashboard only) */}
        {showSearch && (
          <input
            value={search}
            onChange={(e) => onSearchChange?.(e.target.value)}
            placeholder="Search food…"
            className="ml-2 flex-1 rounded-xl border border-line bg-card px-3 py-2 text-sm outline-none placeholder:text-muted"
          />
        )}
      </div>
    </header>
  );
}
