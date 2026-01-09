import { useState } from "react";
import TopBar from "../components/TopBar";
import BottomNav from "../components/BottomNav";
import { Outlet } from "react-router-dom";

export default function AppLayout({ children }) {
  const [search, setSearch] = useState("");

  return (
    <div className="min-h-screen bg-bg text-white">
      <TopBar search={search} onSearchChange={setSearch} />

      {/* 👇 CONTAINER CHỐNG TRÀN */}
      <main className="mx-auto w-full max-w-3xl px-4 py-4">
        <Outlet context={{ search }} />
      </main>

      <BottomNav />
    </div>
  );
}
