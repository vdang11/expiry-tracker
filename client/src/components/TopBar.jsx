import { LogOut } from "lucide-react";
import { useNavigate, useLocation } from "react-router-dom";
import { useEffect, useState, useMemo } from "react";
import Logo from "../assets/logo.svg";
import {
  clearCurrentUser,
  getCurrentUser,
  subscribeAuthChange,
} from "../api/authStorage";

export default function TopBar({ search, onSearchChange }) {
  const navigate = useNavigate();
  const location = useLocation();

  const [currentUser, setCurrentUser] = useState(() => getCurrentUser());

  useEffect(() => {
    const unsubscribe = subscribeAuthChange(() => {
      setCurrentUser(getCurrentUser());
    });

    return unsubscribe;
  }, [getCurrentUser]);

  const initial = currentUser?.email
    ? currentUser.email.charAt(0).toUpperCase()
    : "?";

  const displayEmail = useMemo(() => {
    if (!currentUser?.email) return "Guest";

    const [name, domain] = currentUser.email.split("@");

    if (!name || !domain) return currentUser.email;

    if (name.length <= 2) {
      return name[0] + "*****@" + domain;
    }

    return `${name.slice(0, 2)}*****@${domain}`;
  }, [currentUser]);

  const handleLogout = () => {
    clearCurrentUser();
    navigate("/login", { replace: true });
  };

  const showSearch = ["/", "/dashboard"].some((path) =>
    location.pathname.startsWith(path)
  );

  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-3 flex flex-wrap items-center gap-3">
      {/* Logo */}
      <button
        onClick={() => navigate("/")}
        className="flex items-center gap-2 shrink-0 order-1"
      >
        <img src={Logo} className="h-8 w-8" />
        <span className="text-lg font-bold whitespace-nowrap">
          Expiry Tracker
        </span>
      </button>

      {/* User */}
      <div className="flex items-center gap-2 shrink-0 order-2 ml-auto sm:order-3">
        <span className="size-7 flex items-center justify-center rounded-full bg-slate-700 text-xs font-medium">
          {initial}
        </span>

        <div className="relative group text-xs opacity-80 hidden sm:block">
          {displayEmail}

          <span className="absolute left-1/2 top-full mt-1 -translate-x-1/2 hidden group-hover:block bg-slate-800 text-white px-2 py-1 rounded-md border border-slate-700 shadow-md text-xs whitespace-nowrap z-50">
            {currentUser?.email}
          </span>
        </div>

        <button
          aria-label="Logout"
          onClick={handleLogout}
          className="p-2 rounded-md border border-line hover:border-accent transition"
        >
          <LogOut size={16} />
        </button>
      </div>

      {/* Search */}
      {showSearch && (
        <input
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
          placeholder="Search food..."
          className="
            w-full order-3
            sm:order-2 sm:flex-1
            rounded-lg bg-slate-800 border border-line
            px-3 py-2 text-sm text-white
            placeholder-muted
            focus:border-accent/70 outline-none
          "
        />
      )}
    </div>
  );
}