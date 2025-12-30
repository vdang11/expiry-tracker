export default function Profile() {
  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold">Profile</h2>

      <div className="rounded-2xl border border-line bg-card p-4 space-y-3">
        <Row label="Notification channel" value="In-app (MVP)" />
        <Row label="Default warn days" value="3 days" />
        <Row label="AI scan" value="Mock mode" />
      </div>

      <button className="w-full rounded-xl border border-line bg-card px-4 py-2 text-sm text-muted hover:text-white">
        Sign out (demo)
      </button>
    </div>
  );
}

function Row({ label, value }) {
  return (
    <div className="flex items-center justify-between gap-3">
      <div className="text-sm text-muted">{label}</div>
      <div className="text-sm">{value}</div>
    </div>
  );
}
