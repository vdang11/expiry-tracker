export function signup({ username, password }) {
  const users = JSON.parse(localStorage.getItem("users") || "[]");

  // check exists
  if (users.some(u => u.username === username)) {
    return { ok: false, error: "Username already exists" };
  }

  users.push({ username, password });
  localStorage.setItem("users", JSON.stringify(users));

  return { ok: true };
}

export function login({ username, password }) {
  const users = JSON.parse(localStorage.getItem("users") || "[]");

  const found = users.find(u => u.username === username && u.password === password);

  if (!found) {
    return { ok: false, error: "Invalid username or password" };
  }

  localStorage.setItem("currentUser", JSON.stringify(found));

  return { ok: true };
}

export function logout() {
  localStorage.removeItem("currentUser");
}
