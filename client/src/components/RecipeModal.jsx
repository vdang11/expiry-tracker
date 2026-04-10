import { X, ChefHat, ListChecks } from "lucide-react";

function normalizeIngredient(value) {
  return (value || "").trim().toLowerCase();
}

function isExpiringIngredient(ingredient, expiringIngredients = []) {
  const ing = (ingredient || "").trim().toLowerCase();

  const expSet = new Set(
    (expiringIngredients || []).map((e) =>
      (e || "").trim().toLowerCase()
    )
  );

  return expSet.has(ing);
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
            <h3 className="text-lg font-semibold text-slate-100 sm:text-xl">
              {recipe.title}
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
              {recipe.ingredients?.map((ingredient, index) => {
                const expiring = isExpiringIngredient(
                  ingredient,
                  recipe.expiringIngredients
                );

                return (
                  <span
                    key={`${ingredient}-${index}`}
                    className={
                      expiring
                        ? "rounded-full border border-yellow-400/40 bg-yellow-400/10 px-3 py-1.5 text-sm text-yellow-200"
                        : "rounded-full border border-slate-700 bg-slate-800 px-3 py-1.5 text-sm text-slate-200"
                    }
                  >
                    {ingredient}
                  </span>
                );
              })}
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

          <section className="rounded-2xl border border-slate-800 bg-slate-950/40 p-4">
            <div className="text-sm font-medium text-slate-200">
              Highlight guide
            </div>

            <div className="mt-3 flex flex-wrap gap-3 text-xs text-slate-400">
              <div className="flex items-center gap-2">
                <span className="inline-block h-3 w-3 rounded-full border border-yellow-400/40 bg-yellow-400/20" />
                <span>Expiring soon / expired ingredient</span>
              </div>

              <div className="flex items-center gap-2">
                <span className="inline-block h-3 w-3 rounded-full border border-slate-700 bg-slate-800" />
                <span>Other recipe ingredients</span>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}