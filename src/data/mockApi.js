// Mock "DB" in memory (module-level). In real app -> backend API.
let items = [
  {
    id: 1,
    name: "Trứng gà",
    category: "EGG",
    purchaseDate: "2025-12-20",
    expiryDate: "2025-12-31",
    estimatedExpiryDate: null,
    quantity: 6,
    unit: "pcs",
    storageType: "FRIDGE",
    status: "ACTIVE",
    source: "manual",
  },
  {
    id: 2,
    name: "Rau cải",
    category: "VEGETABLE",
    purchaseDate: "2025-12-27",
    expiryDate: null,
    estimatedExpiryDate: "2025-12-30",
    quantity: 1,
    unit: "bunch",
    storageType: "FRIDGE",
    status: "ACTIVE",
    source: "ai_estimate",
  },
  {
    id: 3,
    name: "Thịt bò",
    category: "MEAT",
    purchaseDate: "2025-12-25",
    expiryDate: "2025-12-28",
    estimatedExpiryDate: null,
    quantity: 0.5,
    unit: "kg",
    storageType: "FRIDGE",
    status: "ACTIVE",
    source: "manual",
  },
];

let notifications = [
  { id: 101, type: "EXPIRY_SOON", itemId: 1, message: "Trứng gà sắp hết hạn", date: "2025-12-29" },
  { id: 102, type: "EXPIRY_SOON", itemId: 2, message: "Rau cải nên dùng sớm", date: "2025-12-29" },
  { id: 103, type: "EXPIRED", itemId: 3, message: "Thịt bò đã hết hạn", date: "2025-12-29" },
];

function todayISO() {
  // For demo: use current date
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

function daysBetween(fromISO, toISO) {
  const a = new Date(fromISO + "T00:00:00");
  const b = new Date(toISO + "T00:00:00");
  const ms = b - a;
  return Math.round(ms / (1000 * 60 * 60 * 24));
}

function getEffectiveExpiry(item) {
  return item.expiryDate || item.estimatedExpiryDate;
}

function computeDerived(item) {
  const expiry = getEffectiveExpiry(item);
  const t = todayISO();
  let daysLeft = null;
  let expiryStatus = "OK";

  if (expiry) {
    daysLeft = daysBetween(t, expiry);
    if (daysLeft < 0) expiryStatus = "EXPIRED";
    else if (daysLeft <= 3) expiryStatus = "EXPIRING_SOON";
  }

  return { ...item, effectiveExpiry: expiry, daysLeft, expiryStatus };
}

export const api = {
  async getItems() {
    return items.map(computeDerived).sort((a, b) => {
      const ea = a.effectiveExpiry || "9999-12-31";
      const eb = b.effectiveExpiry || "9999-12-31";
      return ea.localeCompare(eb);
    });
  },

  async getItemById(id) {
    const found = items.find((x) => x.id === Number(id));
    if (!found) throw new Error("Item not found");
    return computeDerived(found);
  },

  async createItem(payload) {
    const nextId = Math.max(...items.map((x) => x.id)) + 1;
    const newItem = { id: nextId, status: "ACTIVE", source: payload.source || "manual", ...payload };
    items = [newItem, ...items];
    return computeDerived(newItem);
  },

  async updateItem(id, patch) {
    const idx = items.findIndex((x) => x.id === Number(id));
    if (idx === -1) throw new Error("Item not found");
    items[idx] = { ...items[idx], ...patch };
    return computeDerived(items[idx]);
  },

  async deleteItem(id) {
    items = items.filter((x) => x.id !== Number(id));
    notifications = notifications.filter((n) => n.itemId !== Number(id));
    return { ok: true };
  },

  async getNotifications() {
    return notifications.slice().sort((a, b) => b.date.localeCompare(a.date));
  },

  async clearNotifications() {
    notifications = [];
    return { ok: true };
  },

  async getMenuSuggestions() {
    // Simple rule-based suggestions from expiring items
    const list = (await this.getItems()).filter((x) => x.expiryStatus !== "OK");
    const names = list.map((x) => x.name.toLowerCase());

    const suggestions = [];
    const add = (title, uses, timeMin) => suggestions.push({ id: title, title, uses, timeMin });

    if (names.some((n) => n.includes("trứng")) && names.some((n) => n.includes("rau"))) {
      add("Canh rau nấu trứng", ["Trứng gà", "Rau cải"], 20);
      add("Salad rau + trứng luộc", ["Trứng gà", "Rau cải"], 10);
    }
    if (names.some((n) => n.includes("thịt")) && names.some((n) => n.includes("rau"))) {
      add("Thịt xào rau", ["Thịt bò", "Rau cải"], 15);
    }
    if (suggestions.length === 0) {
      add("Món gợi ý đơn giản", list.map((x) => x.name), 15);
    }

    return { basedOn: list.map((x) => x.name), suggestions };
  },
};

export const enums = {
  category: [
    ["VEGETABLE", "Vegetable"],
    ["FRUIT", "Fruit"],
    ["MEAT", "Meat"],
    ["FISH", "Fish"],
    ["DAIRY", "Dairy"],
    ["EGG", "Egg"],
    ["DRY", "Dry goods"],
  ],
  storageType: [
    ["FRIDGE", "Fridge"],
    ["FREEZER", "Freezer"],
    ["PANTRY", "Pantry"],
  ],
  unit: [
    ["pcs", "pcs"],
    ["kg", "kg"],
    ["g", "g"],
    ["L", "L"],
    ["bunch", "bunch"],
    ["pack", "pack"],
  ],
};
