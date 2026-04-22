import { useEffect, useState } from "react";
import { api } from "../api/apiClient";

export default function Notifications() {
  const [list, setList] = useState([]);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    const data = await api.getNotifications();
    setList(Array.isArray(data) ? data : []);
  }

  async function handleRead(id) {
    await api.markNotificationRead(id);

    setList(prev =>
      prev.map(n =>
        n.id === id ? { ...n, isRead: true } : n
      )
    );
  }

  async function handleDelete(id) {
    await api.deleteNotification(id);

    setList(prev =>
      prev.filter(n => n.id !== id)
    );
  }

  async function handleClearAll() {
    await api.clearAllNotifications();
    setList([]);
  }

  return (
    <div className="space-y-3 px-3">

      {/* HEADER */}
      <div className="flex justify-between items-center">
        <h2 className="text-lg font-semibold text-white">
          Notifications
        </h2>

        {list.length > 0 && (
          <button
            onClick={handleClearAll}
            className="text-xs text-red-400 hover:text-red-300"
          >
            Clear all
          </button>
        )}
      </div>

      {/* LIST */}
      {list.map((n) => (
        <div
          key={n.id}
          onClick={() => handleRead(n.id)}
          className={`flex items-start gap-3 p-4 rounded-xl cursor-pointer
            border transition
            ${
              n.isRead
                ? "bg-card border-line"
                : "bg-[#1f2937] border-[#374151]"
            }`}
        >
          {/* DOT */}
          {!n.isRead && (
            <div className="w-2 h-2 bg-blue-400 rounded-full mt-2"></div>
          )}

          {/* CONTENT */}
          <div className="flex-1">
            <div className="text-sm font-semibold text-white">
              {n.title}
            </div>

            <div className="text-sm text-gray-400 mt-1">
              {n.message}
            </div>
          </div>

          {/* DELETE */}
          <button
            onClick={(e) => {
              e.stopPropagation();
              handleDelete(n.id);
            }}
            className="text-gray-500 hover:text-red-400 text-xs"
          >
            ✕
          </button>
        </div>
      ))}

      {/* EMPTY */}
      {list.length === 0 && (
        <div className="text-sm text-gray-500">
          No notifications
        </div>
      )}

    </div>
  );
}