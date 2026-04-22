const AUTH_CHANGED_EVENT = "authChanged";

function dispatchAuthChanged() {
  window.dispatchEvent(new Event(AUTH_CHANGED_EVENT));
}

// ===== TOKEN =====
export function getToken() {
  return localStorage.getItem("token");
}

export function saveToken(token) {
  localStorage.setItem("token", token);
  dispatchAuthChanged();
}

export function clearAuth() {
  localStorage.removeItem("token");
  dispatchAuthChanged();
}

// ===== DECODE JWT =====
function parseJwt(token) {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload;
  } catch {
    return null;
  }
}

// ===== USER INFO =====
export function getCurrentUser() {
  const token = getToken();
  if (!token) return null;

  const payload = parseJwt(token);
  if (!payload) return null;

  return {
    id: payload.sub,
    email: payload.email,
  };
}

export function isLoggedIn() {
  return Boolean(getToken());
}

export function subscribeAuthChange(callback) {
  window.addEventListener(AUTH_CHANGED_EVENT, callback);
  return () => window.removeEventListener(AUTH_CHANGED_EVENT, callback);
}