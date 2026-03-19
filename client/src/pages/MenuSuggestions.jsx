import { useEffect, useState } from "react";
import { api } from "../api/apiClient";

export default function MenuSuggestions() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let alive = true;
    (async () => {
      setLoading(true);
      const res = await api.getMenuSuggestions();
      if (alive) {
        setData(res);
        setLoading(false);
      }
    })();
    return () => (alive = false);
  }, []);

  if (loading) {
    return (
      <div className="rounded-2xl border border-line bg-card p-4 text-muted">
        Đang gợi ý menu...
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold">Menu Suggestions</h2>

      <div className="text-sm text-muted">
        Dựa trên: {data.basedOn.join(", ")}
      </div>

      <div className="space-y-3">
        {data.suggestions.map((s) => (
          <div key={s.id} className="rounded-2xl border border-line bg-card p-4">
            <div className="font-semibold">{s.title}</div>
            <div className="mt-1 text-sm text-muted">
              Uses: {s.uses.join(", ")} · {s.timeMin} min
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
