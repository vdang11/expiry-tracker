import { LogOut } from "lucide-react";
import { useNavigate, useLocation } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";
import Logo from "../assets/logo.svg";
import {
  clearCurrentUser,
  getCurrentUser,
  subscribeAuthChange,
} from "../api/authStorage";

export default function TopBar({ search = "", onSearchChange = () => {} }) {
  const navigate = useNavigate();
  const location = useLocation();

  const [currentUser, setCurrentUser] = useState(() => getCurrentUser());
  const [input, setInput] = useState(search || "");

  useEffect(() => {
    const unsubscribe = subscribeAuthChange(() => {
      setCurrentUser(getCurrentUser());
    });

    return unsubscribe;
  }, []);

  useEffect(() => {
    setInput(search || "");
  }, [search]);

  const initial = currentUser?.email
    ? currentUser.email.charAt(0).toUpperCase()
    : "?";

  const displayEmail = useMemo(() => {
    if (!currentUser?.email) return "Guest";

    const [name, domain] = currentUser.email.split("@");

    if (!name || !domain) return currentUser.email;

    if (name.length <= 2) {
      return `${name[0]}*****@${domain}`;
    }

    return `${name.slice(0, 2)}*****@${domain}`;
  }, [currentUser]);

  const handleLogout = () => {
    clearCurrentUser();
    navigate("/login", { replace: true });
  };

  function handleKeyDown(e) {
    if (e.key === "Enter") {
      onSearchChange(input.trim());
    }
  }

  const showSearch =
    location.pathname === "/" || location.pathname === "/dashboard";

  return (
    <div className="mx-auto flex w-full max-w-3xl flex-wrap items-center gap-3 px-4 py-3">
      {/* Logo */}
      <button
        onClick={() => navigate("/")}
        className="order-1 flex shrink-0 items-center gap-2"
      >
        <img src={Logo} alt="Expiry Tracker logo" className="h-8 w-8" />
        <span className="whitespace-nowrap text-lg font-bold">
          Expiry Tracker
        </span>
      </button>

      {/* User */}
      <div className="order-2 ml-auto flex shrink-0 items-center gap-2 sm:order-3">
        <span className="flex size-7 items-center justify-center rounded-full bg-slate-700 text-xs font-medium">
          {initial}
        </span>

        <div className="group relative hidden text-xs opacity-80 sm:block">
          {displayEmail}

          <span className="absolute left-1/2 top-full z-50 mt-1 hidden -translate-x-1/2 whitespace-nowrap rounded-md border border-slate-700 bg-slate-800 px-2 py-1 text-xs text-white shadow-md group-hover:block">
            {currentUser?.email}
          </span>
        </div>

        <button
          aria-label="Logout"
          onClick={handleLogout}
          className="rounded-md border border-line p-2 transition hover:border-accent"
        >
          <LogOut size={16} />
        </button>
      </div>

      {/* Search */}
      {showSearch && (
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Search food..."
          className="
            order-3 w-full
            rounded-lg border border-line bg-slate-800
            px-3 py-2 text-sm text-white
            outline-none placeholder-muted
            focus:border-accent/70
            sm:order-2 sm:flex-1
          "
        />
      )}
    </div>
  );
}