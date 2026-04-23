import { httpClient } from "./httpClient";

// ===== BASE URL (MULTI-ENV FIX) =====
const BASE_URL =
  import.meta.env.MODE === "production"
    ? import.meta.env.VITE_API_URL
    : "";

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
  signup: (payload) =>
    httpClient.post(`${BASE_URL}/api/users/signup`, payload),

  login: (payload) =>
    httpClient.post(`${BASE_URL}/api/users/login`, payload),

  // ===== VISION =====
  scanImages: (formData) =>
    httpClient.post(`${BASE_URL}/api/vision/scan`, formData),

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
      `${BASE_URL}/api/products?page=${page}&size=${size}&search=${encodeURIComponent(
        search
      )}&filter=${encodeURIComponent(filter)}&sortBy=${encodeURIComponent(
        sortBy
      )}&direction=${encodeURIComponent(direction)}`
    ),

  getSummary: () =>
    httpClient.get(`${BASE_URL}/api/products/summary`),

  getItemById: (id) =>
    httpClient.get(`${BASE_URL}/api/products/${id}`),

  saveItem: (payload) =>
    httpClient.post(`${BASE_URL}/api/products`, payload),

  deleteItem: (id) =>
    httpClient.delete(`${BASE_URL}/api/products/${id}`),

  consumeItem: (id) =>
    httpClient.put(`${BASE_URL}/api/products/${id}/consume`),

  // ===== RECIPES =====
  async generateRecipes(excludeRecipeIds = []) {
    const data = await httpClient.post(
      `${BASE_URL}/api/recipes/generate`,
      { excludeRecipeIds }
    );

    const list = Array.isArray(data) ? data : [];
    return list.map(normalizeRecipe);
  },

  async getRecipes() {
    const data = await httpClient.get(`${BASE_URL}/api/recipes`);
    return (Array.isArray(data) ? data : []).map(normalizeRecipe);
  },

  async getRecipeById(id) {
    const data = await httpClient.get(
      `${BASE_URL}/api/recipes/${id}`
    );
    return normalizeRecipe(data || {});
  },

  // ===== NOTIFICATIONS =====
  getNotifications: () =>
    httpClient.get(`${BASE_URL}/api/notifications`),

  markNotificationRead: (id) =>
    httpClient.put(`${BASE_URL}/api/notifications/${id}/read`),

  deleteNotification: (id) =>
    httpClient.delete(`${BASE_URL}/api/notifications/${id}`),

  clearAllNotifications: () =>
    httpClient.delete(`${BASE_URL}/api/notifications`),

  // ===== PROFILE =====
  getProfile: () =>
    httpClient.get(`${BASE_URL}/api/profile`),

  updateEmailReminder: (enabled) =>
    httpClient.put(
      `${BASE_URL}/api/profile/email-reminder`,
      { enabled }
    ),
};