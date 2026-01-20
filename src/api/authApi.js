export async function signup({ username, password }) {
  await new Promise((r) => setTimeout(r, 200));

  // remove old user + old items (per mock user)
  localStorage.removeItem("currentUser");
  localStorage.removeItem("expiry-tracker-items");

  const user = { username, password };
  localStorage.setItem("currentUser", JSON.stringify(user));

  return { ok: true, data: user };
}

export async function login({ username, password }) {
  await new Promise((r) => setTimeout(r, 200));

  const raw = localStorage.getItem("currentUser");
  if (!raw) {
    return { ok: false, error: "No account found. Please sign up first." };
  }

  let user;
  try {
    user = JSON.parse(raw);
  } catch {
    localStorage.removeItem("currentUser");
    return { ok: false, error: "Stored user is corrupted. Please sign up again." };
  }

  if (user.username !== username || user.password !== password) {
    return { ok: false, error: "Invalid username or password." };
  }

  return { ok: true, data: user };
}

