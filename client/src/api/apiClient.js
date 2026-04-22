import { httpClient } from "./httpClient";

// ===== NORMALIZE =====
function normalize(value) {
  return (value || "").trim().toLowerCase();
}

// ===== RECIPE NORMALIZER =====
function normalizeRecipe(recipe) {
  return {
    ...recipe,
    expiringIngredients: (recipe.expiringIngredients || []).map(normalize),
    ingredients: (recipe.ingredients || []).map((i) => i.trim()),
    title: recipe.title || recipe.name || "Untitled Recipe",
  };
}

// ===== API =====
export const api = {
  // ===== AUTH =====
  signup: (payload) => httpClient.post("/api/users/signup", payload),
  login: (payload) => httpClient.post("/api/users/login", payload),

  // ===== VISION =====
  scanImages: (formData) => httpClient.post("/api/vision/scan", formData),

  // ===== ITEMS =====
  getItems: (
    page = 0,
    size = 20,
    search = "",
    filter = "all",
    sortBy = "expiryDate",
    direction = "asc"
  ) =>
    httpClient.get(
      `/api/products?page=${page}&size=${size}&search=${encodeURIComponent(
        search
      )}&filter=${encodeURIComponent(filter)}&sortBy=${encodeURIComponent(
        sortBy
      )}&direction=${encodeURIComponent(direction)}`
    ),

  getSummary: () => httpClient.get("/api/products/summary"),

  getItemById: (id) => httpClient.get(`/api/products/${id}`),

  saveItem: (payload) => httpClient.post("/api/products", payload),

  deleteItem: (id) => httpClient.delete(`/api/products/${id}`),

  consumeItem: (id) => httpClient.put(`/api/products/${id}/consume`),

  // ===== RECIPES =====
  async generateRecipes(excludeRecipeIds = []) {
    const data = await httpClient.post("/api/recipes/generate", {
      excludeRecipeIds,
    });

    const list = Array.isArray(data) ? data : [];
    return list.map(normalizeRecipe);
  },

  async getRecipes() {
    const data = await httpClient.get("/api/recipes");
    return (Array.isArray(data) ? data : []).map(normalizeRecipe);
  },

  async getRecipeById(id) {
    const data = await httpClient.get(`/api/recipes/${id}`);
    return normalizeRecipe(data || {});
  },

// ===== NOTIFICATIONS =====
getNotifications: () => httpClient.get("/api/notifications"),

markNotificationRead: (id) =>
  httpClient.put(`/api/notifications/${id}/read`),

deleteNotification: (id) =>
  httpClient.delete(`/api/notifications/${id}`),

clearAllNotifications: () =>
  httpClient.delete("/api/notifications"),

  // ===== PROFILE =====
  getProfile: () => httpClient.get("/api/profile"),

  updateEmailReminder: (enabled) =>
    httpClient.put("/api/profile/email-reminder", { enabled }),
};