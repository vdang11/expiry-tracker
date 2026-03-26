import { X, ChefHat, ListChecks } from "lucide-react";

function getCuisineStyle(cuisine) {
  switch (cuisine) {
    case "ASIAN":
      return "bg-emerald-500/15 text-emerald-300 border border-emerald-500/30";
    case "EUROPEAN":
      return "bg-sky-500/15 text-sky-300 border border-sky-500/30";
    case "AMERICAN":
      return "bg-amber-500/15 text-amber-300 border border-amber-500/30";
    default:
      return "bg-slate-500/15 text-slate-300 border border-slate-500/30";
  }
}

export default function RecipeModal({ recipe, onClose }) {
  if (!recipe) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center sm:items-center">
      <button
        type="button"
        aria-label="Close modal backdrop"
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      <div className="relative z-10 w-full max-w-2xl rounded-t-3xl border border-slate-700 bg-slate-900 shadow-2xl sm:rounded-3xl">
        <div className="flex items-start justify-between border-b border-slate-800 px-5 py-4">
          <div className="pr-4">
            <div className="mb-2">
              <span
                className={`inline-flex rounded-full px-2.5 py-1 text-[11px] font-medium ${getCuisineStyle(
                  recipe.cuisine
                )}`}
              >
                {recipe.cuisine}
              </span>
            </div>
            <h3 className="text-lg font-semibold text-slate-100 sm:text-xl">
              {recipe.name}
            </h3>
          </div>

          <button
            onClick={onClose}
            className="rounded-full p-2 text-slate-300 transition hover:bg-slate-800 hover:text-white"
          >
            <X size={18} />
          </button>
        </div>

        <div className="max-h-[75vh] space-y-5 overflow-y-auto px-5 py-5">
          <section className="rounded-2xl border border-slate-800 bg-slate-950/40 p-4">
            <div className="mb-3 flex items-center gap-2 text-sm font-medium text-slate-200">
              <ChefHat size={16} />
              Ingredients
            </div>

            <div className="flex flex-wrap gap-2">
              {recipe.ingredients?.map((ingredient, index) => (
                <span
                  key={`${ingredient}-${index}`}
                  className="rounded-full border border-slate-700 bg-slate-800 px-3 py-1.5 text-sm text-slate-200"
                >
                  {ingredient}
                </span>
              ))}
            </div>
          </section>

          <section className="rounded-2xl border border-slate-800 bg-slate-950/40 p-4">
            <div className="mb-3 flex items-center gap-2 text-sm font-medium text-slate-200">
              <ListChecks size={16} />
              Steps
            </div>

            <div className="space-y-3">
              {recipe.steps?.map((step, index) => (
                <div
                  key={`${step}-${index}`}
                  className="flex gap-3 rounded-2xl bg-slate-800/70 p-3"
                >
                  <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-primary text-sm font-semibold text-white">
                    {index + 1}
                  </div>
                  <p className="text-sm leading-6 text-slate-200">{step}</p>
                </div>
              ))}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}