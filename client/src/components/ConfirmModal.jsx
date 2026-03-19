export default function ConfirmModal({ open, onClose, onConfirm }) {

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">

      <div className="w-[320px] rounded-2xl bg-card border border-line p-5 space-y-4">

        <h3 className="text-lg font-semibold text-white">
          Delete item?
        </h3>

        <p className="text-sm text-muted">
          This action cannot be undone.
        </p>

        <div className="flex gap-2 pt-2">

          <button
            onClick={onClose}
            className="flex-1 rounded-xl border border-line px-3 py-2 text-sm text-muted hover:text-white"
          >
            Cancel
          </button>

          <button
            onClick={onConfirm}
            className="flex-1 rounded-xl bg-red-500 px-3 py-2 text-sm font-semibold text-white"
          >
            Delete
          </button>

        </div>

      </div>
    </div>
  );
}