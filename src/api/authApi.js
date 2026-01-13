export async function signup({ username, password }) {
  await new Promise(r => setTimeout(r, 200));

  // remove old user + old items (per mock user)
  localStorage.removeItem("currentUser");
  localStorage.removeItem("expiry-tracker-items");

  const user = { username, password };
  localStorage.setItem("currentUser", JSON.stringify(user));

  return { ok: true, data: user };
}
