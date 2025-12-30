import { useState } from "react";
import TopBar from "../components/TopBar";
import BottomNav from "../components/BottomNav";

export default function AppLayout({ children }) {
  const [search, setSearch] = useState("");

  return (
    <div className="flex min-h-screen flex-col bg-bg text-white">
      <TopBar search={search} onSearchChange={setSearch} />

      <main className="flex-1 overflow-y-auto px-4 py-6 pb-24">
        <div className="mx-auto w-full max-w-3xl">
          {typeof children === "function"
            ? children(search)
            : children}
        </div>
      </main>

      <BottomNav />
    </div>
  );
}
