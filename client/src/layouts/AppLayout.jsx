import { useState, useMemo } from "react";
import TopBar from "../components/TopBar";
import BottomNav from "../components/BottomNav";
import { Outlet } from "react-router-dom";

export default function AppLayout() {
  const [search, setSearch] = useState("");

  const outletContext = useMemo(() => ({ search }), [search]);

  return (
    <div className="h-screen bg-bg text-white flex flex-col overflow-hidden">
      <TopBar search={search} onSearchChange={setSearch} />

      <main className="flex-1 min-h-0 overflow-y-auto overscroll-contain">
        <div className="mx-auto w-full max-w-3xl px-4 py-4 pb-28">
          <Outlet context={outletContext} />
        </div>
      </main>

      <BottomNav />
    </div>
  );
}