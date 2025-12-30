import { useEffect, useState } from "react";
import { api } from "../data/mockApi";

export default function Notifications() {
  const [list, setList] = useState([]);

  useEffect(() => {
    api.getNotifications().then(setList);
  }, []);

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
    </div>
  );
}
