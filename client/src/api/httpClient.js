import { getCurrentUserId } from "./authStorage";

async function request(url, options = {}) {
  const {
    method = "GET",
    body,
    headers = {},
  } = options;

  const finalHeaders = {
    ...headers,
  };

  let finalBody = body;

  // ===== AUTO JSON =====
  if (body && !(body instanceof FormData)) {
    finalHeaders["Content-Type"] = "application/json";
    finalBody = JSON.stringify(body);
  }

  // ===== AUTO USER HEADER =====
  const userId = getCurrentUserId();
  if (userId) {
    finalHeaders["X-User-Id"] = String(userId);
  }

  try {
    const response = await fetch(url, {
      method,
      headers: finalHeaders,
      body: finalBody,
    });

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
        `Request failed (${response.status})`;

      throw new Error(message);
    }

    return data;
  } catch (error) {
    console.error("HTTP ERROR:", error);

    throw new Error(
      error.message || "Cannot connect to server"
    );
  }
}

// ===== METHODS =====
export const httpClient = {
  get: (url) => request(url),

  post: (url, body) =>
    request(url, {
      method: "POST",
      body,
    }),

  put: (url, body) =>
    request(url, {
      method: "PUT",
      body,
    }),

  delete: (url) =>
    request(url, {
      method: "DELETE",
    }),
};