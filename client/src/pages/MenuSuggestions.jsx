import { useMemo, useState } from "react";
import { Sparkles, RefreshCcw, ChevronRight, Utensils } from "lucide-react";
import toast from "react-hot-toast";
import { api } from "../api/apiClient";
import RecipeModal from "../components/RecipeModal";

export default function MenuSuggestions() {
  const [recipes, setRecipes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedRecipe, setSelectedRecipe] = useState(null);
  const [error, setError] = useState("");
  const [fromCache, setFromCache] = useState(false);
  const [hasGenerated, setHasGenerated] = useState(false);

  const currentUser = useMemo(() => {
    try {
      return JSON.parse(localStorage.getItem("currentUser"));
    } catch {
      return null;
    }
  }, []);

  async function handleGenerateRecipes() {
    if (!currentUser?.id) {
      setError("Please log in again.");
      return;
    }

    try {
      setLoading(true);
      setError("");
      setHasGenerated(true);

      const data = await api.generateRecipes(currentUser.id);
      const list = Array.isArray(data) ? data : [];

      setRecipes(list);
      setSelectedRecipe(null);

      const cached = list[0]?.fromCache || false;
      setFromCache(cached);

      toast.success(
        cached ? "⚡ Loaded instantly" : "✨ Fresh recipes ready"
      );
    } catch (err) {
      setError(err.message || "Failed to generate recipes");
      toast.error("Failed to generate recipes");
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <div className="space-y-5">

  {/* HEADER */}
  <div className="rounded-3xl border border-slate-700 bg-slate-900 p-5">
    <h2 className="text-xl font-semibold text-white">
      Quick Recipes
    </h2>
    <p className="text-sm text-slate-400 mt-1">
      Cook fast with what you already have
    </p>

    <button
      onClick={handleGenerateRecipes}
      disabled={loading}
      className="mt-4 w-full flex items-center justify-center gap-2 rounded-2xl bg-slate-800 px-4 py-3 text-white hover:bg-slate-700 disabled:opacity-50"
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
  </div>

  {/* EMPTY */}
  {!loading && recipes.length === 0 && (
    <div className="text-center text-slate-400 py-10">
      <Utensils className="mx-auto mb-3" />
      {hasGenerated
        ? "No usable ingredients found"
        : "Generate recipes to start"}
    </div>
  )}

  {/* CACHE BADGE */}
  {recipes.length > 0 && (
    <div>
      {fromCache ? (
        <span className="text-xs text-green-400">
          ⚡ Instant result
        </span>
      ) : (
        <span className="text-xs text-purple-400">
          ✨ Newly generated
        </span>
      )}
    </div>
  )}

  {/* ✅ FULL WIDTH LIST */}
  {recipes.length > 0 && (
    <div className="space-y-4">
      {recipes.map((recipe, index) => (
        <button
          key={index}
          onClick={() => setSelectedRecipe(recipe)}
          className="w-full text-left rounded-3xl border border-slate-700 bg-slate-900 p-5 hover:border-slate-600 transition"
        >
          {/* HEADER ROW */}
          <div className="flex items-start justify-between">
            <h3 className="text-lg font-semibold text-white">
              {recipe.title}
            </h3>

            <ChevronRight className="text-slate-400 mt-1" />
          </div>

          {/* INGREDIENT TAGS */}
          <div className="mt-3 flex flex-wrap gap-2">
            {recipe.ingredients?.slice(0, 5).map((ing, i) => (
              <span
                key={i}
                className="px-2 py-1 rounded-full text-xs bg-blue-500/10 text-blue-300 border border-blue-500/20"
              >
                {ing}
              </span>
            ))}
          </div>

          {/* DESCRIPTION */}
          <p className="mt-3 text-sm text-slate-400">
            Simple steps, ready in minutes
          </p>
        </button>
      ))}
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