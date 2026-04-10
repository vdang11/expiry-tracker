import { useMemo, useState } from "react";
import { Sparkles, RefreshCcw, ChevronRight, Utensils } from "lucide-react";
import toast from "react-hot-toast";
import { api } from "../api/apiClient";
import RecipeModal from "../components/RecipeModal";

// ===== NORMALIZE =====
function normalizeIngredient(value) {
  return (value || "").trim().toLowerCase();
}

// ===== FIX: STRICT MATCH (NO includes) =====
function isExpiringIngredient(ingredient, expiringIngredients = []) {
  const ing = normalizeIngredient(ingredient);

  const expSet = new Set(
    (expiringIngredients || []).map(normalizeIngredient)
  );

  return expSet.has(ing);
}

export default function MenuSuggestions() {
  const [recipes, setRecipes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedRecipe, setSelectedRecipe] = useState(null);
  const [error, setError] = useState("");
  const [hasGenerated, setHasGenerated] = useState(false);
  const [seenRecipeIds, setSeenRecipeIds] = useState([]);

  const currentUser = useMemo(() => {
    try {
      return JSON.parse(localStorage.getItem("currentUser"));
    } catch {
      return null;
    }
  }, []);

async function handleGenerateRecipes() {
  if (loading) return; // 🔥 anti spam

  if (!currentUser?.id) {
    setError("Please log in again.");
    return;
  }

  try {
    setLoading(true);
    setError("");
    setHasGenerated(true);

    const data = await api.generateRecipes(seenRecipeIds);
    const list = Array.isArray(data) ? data : [];

    setRecipes(list);

    setSeenRecipeIds((prev) => {
      const newIds = list.map((r) => r.id).filter(Boolean);
      const merged = [...new Set([...prev, ...newIds])];
      return merged.slice(-9);
    });

    setSelectedRecipe(null);

    const fromAI = list.some((r) => r.fromAI);

    // 🔥 FIX TOAST
    toast.success(
      fromAI ? "✨ Fresh recipes from AI" : "⚡ Loaded from database",
      { id: "recipe-toast" }
    );

  } catch (err) {
    setError(err.message || "Failed to generate recipes");

    toast.error("Failed to generate recipes", {
      id: "recipe-error"
    });

  } finally {
    setLoading(false);
  }
}

  return (
    <>
      <div className="space-y-5">
        {/* HEADER */}
        <div className="rounded-3xl border border-slate-700 bg-slate-900 p-5">
          <h2 className="text-xl font-semibold text-white">Quick Recipes</h2>
          <p className="mt-1 text-sm text-slate-400">
            Cook fast with what you already have
          </p>

          <button
            onClick={handleGenerateRecipes}
            disabled={loading}
            className="mt-4 flex w-full items-center justify-center gap-2 rounded-2xl bg-slate-800 px-4 py-3 text-white hover:bg-slate-700 disabled:opacity-50"
          >
            {loading ? (
              <>
                <RefreshCcw className="animate-spin" size={16} />
                Generating...
              </>
            ) : (
              <>
                <Sparkles size={16} />
                {recipes.length ? "Generate Again" : "Generate Recipes"}
              </>
            )}
          </button>

          {error && (
            <p className="mt-3 text-sm text-red-300">{error}</p>
          )}
        </div>

        {/* EMPTY */}
        {!loading && recipes.length === 0 && (
          <div className="py-10 text-center text-slate-400">
            <Utensils className="mx-auto mb-3" />
            {hasGenerated
              ? "No usable ingredients found"
              : "Generate recipes to start"}
          </div>
        )}

        {/* BADGE */}
        {recipes.length > 0 && (
          <div>
            {recipes.some((r) => r.fromAI) ? (
              <span className="text-xs text-purple-400">✨ Fresh AI</span>
            ) : (
              <span className="text-xs text-green-400">⚡ From database</span>
            )}
          </div>
        )}

        {/* LIST */}
        {recipes.length > 0 && (
          <div className="space-y-4">
            {recipes.map((recipe, index) => {
              // 🔥 FIX: dedupe ingredient
              const uniqueIngredients = [
                ...new Set(recipe.ingredients || []),
              ];

              return (
                <button
                  key={recipe.id ?? index}
                  onClick={() => setSelectedRecipe(recipe)}
                  className="w-full rounded-3xl border border-slate-700 bg-slate-900 p-5 text-left transition hover:border-slate-600"
                >
                  <div className="flex items-start justify-between">
                    <h3 className="text-lg font-semibold text-white">
                      {recipe.title}
                    </h3>

                    <ChevronRight className="mt-1 text-slate-400" />
                  </div>

                  <div className="mt-3 flex flex-wrap gap-2">
                    {uniqueIngredients.slice(0, 5).map((ing, i) => {
                      const expiring = isExpiringIngredient(
                        ing,
                        recipe.expiringIngredients
                      );

                      return (
                        <span
                          key={i}
                          className={
                            expiring
                              ? "rounded-full border border-yellow-400/40 bg-yellow-400/10 px-2 py-1 text-xs text-yellow-200"
                              : "rounded-full border border-slate-700 bg-slate-800 px-2 py-1 text-xs text-slate-200"
                          }
                        >
                          {ing}
                        </span>
                      );
                    })}
                  </div>

                  <p className="mt-3 text-sm text-slate-400">
                    Simple steps, ready in minutes
                  </p>
                </button>
              );
            })}
          </div>
        )}

        {/* LEGEND */}
        {recipes.length > 0 && (
          <div className="rounded-2xl border border-slate-800 bg-slate-900/60 p-4">
            <div className="text-sm font-medium text-slate-200">
              Ingredient highlight
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
          </div>
        )}
      </div>

      <RecipeModal
        recipe={selectedRecipe}
        onClose={() => setSelectedRecipe(null)}
      />
    </>
  );
}