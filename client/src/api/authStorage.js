const AUTH_CHANGED_EVENT = "authChanged";

const TOKEN_KEY = "token";
const USER_KEY = "currentUser";

// ===== EVENT =====
function dispatchAuthChanged() {
  window.dispatchEvent(new Event(AUTH_CHANGED_EVENT));
}

// ===== TOKEN =====
export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

// 🔥 FIX: save BOTH token + user
export function saveToken(token, user) {
  localStorage.setItem(TOKEN_KEY, token);

  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  dispatchAuthChanged();
}

// ===== CLEAR =====
export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  dispatchAuthChanged();
}

// ===== USER =====
export function getCurrentUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY));
  } catch {
    return null;
  }
}

// ===== AUTH STATE =====
export function isLoggedIn() {
  return Boolean(getToken());
}

// ===== SUBSCRIBE =====
export function subscribeAuthChange(callback) {
  window.addEventListener(AUTH_CHANGED_EVENT, callback);
  return () => window.removeEventListener(AUTH_CHANGED_EVENT, callback);
}