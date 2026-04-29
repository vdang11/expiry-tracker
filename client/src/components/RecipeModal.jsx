import { X, ChefHat, ListChecks } from "lucide-react";
import { useEffect, useMemo } from "react";

function normalize(value) {
  return (value || "").trim().toLowerCase();
}

export default function RecipeModal({ recipe, onClose }) {
  if (!recipe) return null;

  /**
   * 🔥 FIX: chỉ highlight ingredient thật
   */
  const highlightSet = useMemo(() => {
    return new Set(
      (recipe.coveredIngredients || []).map(normalize)
    );
  }, [recipe.coveredIngredients]);

  useEffect(() => {
    function handleEsc(e) {
      if (e.key === "Escape") onClose();
    }

    window.addEventListener("keydown", handleEsc);

    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    return () => {
      window.removeEventListener("keydown", handleEsc);
      document.body.style.overflow = prev;
    };
  }, [onClose]);

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center sm:items-center">
      
      {/* overlay */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      <div className="relative z-10 w-full max-w-2xl rounded-t-3xl border border-slate-700 bg-slate-900 shadow-2xl sm:rounded-3xl">
        
        {/* HEADER */}
        <div className="flex items-start justify-between border-b border-slate-800 px-5 py-4">
          <h3 className="text-lg font-semibold text-slate-100 sm:text-xl">
            {recipe.title}
          </h3>

          <button
            onClick={onClose}
            className="rounded-full p-2 text-slate-300 hover:bg-slate-800"
          >
            <X size={18} />
          </button>
        </div>

        {/* CONTENT */}
        <div className="max-h-[75vh] overflow-y-auto px-5 py-5 space-y-5">
          
          {/* 🔥 EXPIRING INFO (nhẹ, không phá UX) */}
          {recipe.expiringIngredients?.length > 0 && (
            <div className="text-xs text-slate-400">
              Uses expiring items:{" "}
              <span className="text-yellow-300">
                {recipe.expiringIngredients.join(", ")}
              </span>
            </div>
          )}

          {/* INGREDIENTS */}
          <section className="rounded-2xl border border-slate-800 bg-slate-950/40 p-4">
            <div className="mb-3 flex items-center gap-2 text-sm font-medium text-slate-200">
              <ChefHat size={16} />
              Ingredients
            </div>

            <div className="flex flex-wrap gap-2">
              {recipe.ingredients?.map((ingredient, index) => {
                const isHighlighted = highlightSet.has(
                  normalize(ingredient)
                );

                return (
                  <span
                    key={index}
                    className={
                      isHighlighted
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

          {/* STEPS */}
          <section className="rounded-2xl border border-slate-800 bg-slate-950/40 p-4">
            <div className="mb-3 flex items-center gap-2 text-sm font-medium text-slate-200">
              <ListChecks size={16} />
              Steps
            </div>

            <div className="space-y-3">
              {recipe.steps?.map((step, index) => (
                <div
                  key={index}
                  className="flex gap-3 rounded-2xl bg-slate-800/70 p-3"
                >
                  <div className="flex h-7 w-7 items-center justify-center rounded-full bg-primary text-white">
                    {index + 1}
                  </div>
                  <p className="text-sm text-slate-200">{step}</p>
                </div>
              ))}
            </div>
          </section>

        </div>
      </div>
    </div>
  );
}