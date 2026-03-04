import { useState } from "react";
import TopBar from "../components/TopBar";
import BottomNav from "../components/BottomNav";
import { Outlet } from "react-router-dom";

export default function AppLayout() {
  const [search, setSearch] = useState("");

  return (
    <div className="min-h-screen bg-bg text-white flex flex-col">

      <TopBar search={search} onSearchChange={setSearch} />

      {/* scrollable content */}
      <main className="flex-1 overflow-y-auto">
        <div className="mx-auto w-full max-w-3xl px-4 py-4 pb-28">
          <Outlet context={{ search }} />
        </div>
      </main>

      <BottomNav />

    </div>
  );
}