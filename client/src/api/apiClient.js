const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

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

export const api = {
  async getItems(userId) {
    const response = await fetch(`${API_BASE}/api/products?userId=${userId}`);
    return handleResponse(response);
  },

  async getSummary(userId) {
    const response = await fetch(
      `${API_BASE}/api/products/summary?userId=${userId}`,
    );
    return handleResponse(response);
  },

  async getItemById(id) {
    const response = await fetch(`${API_BASE}/api/products/${id}`);
    return handleResponse(response);
  },

  async saveItem(payload) {
    const response = await fetch(`${API_BASE}/api/products`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });

    return handleResponse(response);
  },

  async deleteItem(id) {
    const response = await fetch(`${API_BASE}/api/products/${id}`, {
      method: "DELETE",
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
  },

  consumeItem: async (id) => {
    const res = await fetch(`${API_BASE}/api/products/${id}/consume`, {
      method: "PUT",
    });

    if (!res.ok) {
      throw new Error("Failed to consume item");
    }

    return res.json();
  },
};
