import { LogOut } from "lucide-react";
import { useNavigate, useLocation } from "react-router-dom";
import Logo from "../assets/logo.svg";

export default function TopBar({ search, onSearchChange }) {
  const navigate = useNavigate();
  const location = useLocation();
  const currentUser = JSON.parse(localStorage.getItem("currentUser"));

  const initials = currentUser?.username?.[0]?.toUpperCase() || "?";

  const handleLogout = () => {
    localStorage.removeItem("currentUser");
    localStorage.removeItem("expiry-tracker-items");
    navigate("/login", { replace: true });
  };

  // show search only on dashboard/root
  const showSearch =
    location.pathname === "/" ||
    location.pathname.startsWith("/dashboard");

  return (
   <div className="mx-auto w-full max-w-3xl px-4 py-3 flex items-center gap-4">
  
  {/* Logo */}
  <button onClick={() => navigate("/")} className="flex items-center gap-2">
    <img src={Logo} className="h-8 w-8" />
    <span className="text-lg font-bold">Expiry Tracker</span>
  </button>

  {/* Search */}
  {showSearch && (
    <input
      value={search}
      onChange={(e)=>onSearchChange(e.target.value)}
      placeholder="Search food..."
      className="flex-1 rounded-lg bg-slate-800 border border-line px-3 py-2 text-sm text-white placeholder-muted focus:border-accent/70 outline-none"
    />
  )}

  {/* User */}
  <div className="flex items-center gap-3 ml-auto">
    <span className="size-6 flex items-center justify-center rounded-full bg-slate-700 text-xs font-medium">
      {initials}
    </span>
    <span className="text-xs opacity-80 text-muted">{currentUser?.username}</span>
    <button onClick={handleLogout} className="p-1 rounded-md border border-line hover:border-accent transition">
      <LogOut size={14} />
    </button>
  </div>
</div>

  );
}
