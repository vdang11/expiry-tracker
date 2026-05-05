import { useEffect, useState } from "react";
import { api } from "../api/apiClient";

export default function Profile() {
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    load();
  }, []);

  async function load() {
    const data = await api.getProfile();
    setProfile(data);
  }

  async function toggleEmail() {
    const next = !profile.emailReminderEnabled;
    await api.updateEmailReminder(next);

    setProfile({
      ...profile,
      emailReminderEnabled: next,
    });
  }

  if (!profile) return null;

  return (
    <div className="space-y-6 pb-6">

      {/* ===== HEADER ===== */}
      <div className="flex items-center gap-3">
        <div className="w-12 h-12 rounded-full bg-gradient-to-br from-indigo-500 to-purple-500 flex items-center justify-center text-white font-bold">
          {profile.email?.[0]?.toUpperCase()}
        </div>

        <div>
          <div className="text-sm text-muted">Logged in as</div>
          <div className="font-semibold">{profile.email}</div>
        </div>
      </div>

      {/* ===== SETTINGS CARD ===== */}
      <Card title="Settings">

        <ToggleRow
          label="Email Reminder"
          description="Receive daily expiry alerts"
          enabled={profile.emailReminderEnabled}
          onToggle={toggleEmail}
        />

      </Card>

      {/* ===== SECURITY CARD ===== */}
      <Card title="Security">

        <div className="space-y-3">
          <Row label="Password" value="••••••••" />

          <button
            disabled
            className="w-full rounded-xl border border-line bg-card px-4 py-2 text-sm text-muted opacity-60 cursor-not-allowed"
          >
            Change Password (Coming soon)
          </button>
        </div>

      </Card>

    </div>
  );
}

// ===== COMPONENTS =====

function Card({ title, children }) {
  return (
    <div className="rounded-2xl border border-line bg-card p-4 space-y-4 shadow-sm">
      <div className="text-sm font-semibold text-muted">{title}</div>
      {children}
    </div>
  );
}

function Row({ label, value }) {
  return (
    <div className="flex justify-between items-center">
      <span className="text-sm text-muted">{label}</span>
      <span className="text-sm font-medium">{value}</span>
    </div>
  );
}

function ToggleRow({ label, description, enabled, onToggle }) {
  return (
    <div className="flex items-center justify-between">

      <div>
        <div className="text-sm font-medium">{label}</div>
        <div className="text-xs text-muted">{description}</div>
      </div>

      <button
        onClick={onToggle}
        className={`relative w-12 h-6 rounded-full transition ${
          enabled ? "bg-green-500" : "bg-gray-300"
        }`}
      >
        <span
          className={`absolute top-1 left-1 w-4 h-4 bg-white rounded-full transition ${
            enabled ? "translate-x-6" : ""
          }`}
        />
      </button>

    </div>
  );
}