const API_BASE =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

// ===== USER HELPER =====
function getCurrentUser() {
  try {
    const raw = localStorage.getItem("currentUser");
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

// ===== NORMALIZE (🔥 NEW) =====
function normalize(value) {
  return (value || "").trim().toLowerCase();
}

// ===== HEADER BUILDER =====
function buildHeaders(extra = {}) {
  const currentUser = getCurrentUser();

  return {
    "Content-Type": "application/json",
    ...(currentUser?.id && { "X-User-Id": String(currentUser.id) }),
    ...extra,
  };
}

// ===== RESPONSE HANDLER =====
async function handleResponse(response) {
  let data = null;

  try {
    data = await response.json();
  } catch {
    data = null;
  }

  if (!response.ok) {
    const message =
      data?.message ||
      data?.error ||
      `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return data;
}

// ===== RECIPE NORMALIZER (🔥 NEW) =====
function normalizeRecipe(recipe) {
  return {
    ...recipe,

    // đảm bảo luôn tồn tại
    expiringIngredients: (recipe.expiringIngredients || []).map(normalize),

    // normalize để tránh mismatch UI
    ingredients: (recipe.ingredients || []).map((i) => i.trim()),

    title: recipe.title || recipe.name || "Untitled Recipe",
  };
}

// ===== API =====
export const api = {
  // ===== ITEMS =====
  async getItems() {
    const response = await fetch(`${API_BASE}/api/products`, {
      headers: buildHeaders(),
    });
    return handleResponse(response);
  },

  async getSummary() {
    const response = await fetch(`${API_BASE}/api/products/summary`, {
      headers: buildHeaders(),
    });
    return handleResponse(response);
  },

  async getItemById(id) {
    const response = await fetch(`${API_BASE}/api/products/${id}`, {
      headers: buildHeaders(),
    });
    return handleResponse(response);
  },

  async saveItem(payload) {
    const response = await fetch(`${API_BASE}/api/products`, {
      method: "POST",
      headers: buildHeaders(),
      body: JSON.stringify(payload),
    });

    return handleResponse(response);
  },

  async deleteItem(id) {
    const response = await fetch(`${API_BASE}/api/products/${id}`, {
      method: "DELETE",
      headers: buildHeaders(),
    });

    if (!response.ok) {
      let data = null;
      try {
        data = await response.json();
      } catch {
        data = null;
      }

      const message =
        data?.message ||
        data?.error ||
        `Delete failed with status ${response.status}`;
      throw new Error(message);
    }

    return true;
  },

  async consumeItem(id) {
    const response = await fetch(`${API_BASE}/api/products/${id}/consume`, {
      method: "PUT",
      headers: buildHeaders(),
    });

    return handleResponse(response);
  },

  // ===== RECIPES =====
  async generateRecipes(excludeRecipeIds = []) {
    const response = await fetch(`${API_BASE}/api/recipes/generate`, {
      method: "POST",
      headers: buildHeaders(),
      body: JSON.stringify({
        excludeRecipeIds,
      }),
    });

    const data = await handleResponse(response);

    // 🔥 CRITICAL FIX: đảm bảo FE luôn có expiringIngredients
    const list = Array.isArray(data) ? data : [];

    return list.map(normalizeRecipe);
  },

  async getRecipes() {
    const response = await fetch(`${API_BASE}/api/recipes`, {
      headers: buildHeaders(),
    });

    const data = await handleResponse(response);

    return (Array.isArray(data) ? data : []).map(normalizeRecipe);
  },

  async getRecipeById(id) {
    const response = await fetch(`${API_BASE}/api/recipes/${id}`, {
      headers: buildHeaders(),
    });

    const data = await handleResponse(response);

    return normalizeRecipe(data || {});
  },
};