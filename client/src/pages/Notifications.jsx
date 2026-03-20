import { useEffect, useState } from "react";
export default function Notifications() {
  const [list, setList] = useState([]);

  useEffect(() => {
    async function load() {
      try {
        const fake = [
          {
            id: 1,
            message: "Milk is expiring soon",
            type: "SOON",
            date: "2026-03-20"
          }
        ];

        setList(fake);
      } catch (e) {
        console.error(e);
        setList([]);
      }
    }

    load();
  }, []);

  function handleClear() {
    setList([]);
  }

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold">Notifications</h2>

      {list.map((n) => (
        <div key={n.id} className="rounded-2xl border border-line bg-card p-4">
          <div className="font-semibold">{n.message}</div>
          <div className="mt-1 text-sm text-muted">
            {n.type} · {n.date}
          </div>
        </div>
      ))}

      {list.length === 0 && (
        <div className="rounded-2xl border border-line bg-card p-4 text-muted">
          Không có thông báo
        </div>
      )}

      <div className="flex gap-2">
        <button
          onClick={handleClear}
          className="flex-1 rounded-xl border border-red-400/40 bg-card px-4 py-2 text-sm font-semibold text-red-300"
        >
          Clear
        </button>
      </div>
    </div>
  );
}