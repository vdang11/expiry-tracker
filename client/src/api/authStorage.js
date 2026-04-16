const AUTH_CHANGED_EVENT = "authChanged";

function dispatchAuthChanged() {
  window.dispatchEvent(new Event(AUTH_CHANGED_EVENT));
}

export function getCurrentUser() {
  try {
    const raw = localStorage.getItem("currentUser");

    if (!raw) {
      return null;
    }

    return JSON.parse(raw);
  } catch (error) {
    console.error("Failed to parse currentUser from localStorage:", error);
    return null;
  }
}

export function getCurrentUserId() {
  const user = getCurrentUser();
  return user?.id ?? null;
}

export function getCurrentUserEmail() {
  const user = getCurrentUser();
  return user?.email ?? null;
}

export function saveCurrentUser(user) {
  localStorage.setItem("currentUser", JSON.stringify(user));
  dispatchAuthChanged();
}

export function clearCurrentUser() {
  localStorage.removeItem("currentUser");
  dispatchAuthChanged();
}

export function isLoggedIn() {
  return Boolean(getCurrentUser());
}

export function subscribeAuthChange(callback) {
  window.addEventListener(AUTH_CHANGED_EVENT, callback);

  return () => {
    window.removeEventListener(AUTH_CHANGED_EVENT, callback);
  };
}