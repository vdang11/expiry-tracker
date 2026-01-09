/* =========================
   LocalStorage helpers
========================= */

const STORAGE_KEY = "expiry-tracker-items";

function loadItems() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch (e) {
    console.error("Failed to load items", e);
  }

  // seed data (chỉ dùng khi CHƯA có gì trong storage)
  const initial = [
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
      alertDismissedAt: null,
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
      alertDismissedAt: null,
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
      alertDismissedAt: null,
    },
  ];

  localStorage.setItem(STORAGE_KEY, JSON.stringify(initial));
  return initial;
}

function saveItems(items) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
}

/* =========================
   Date helpers
========================= */

function todayISO() {
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

function daysBetween(fromISO, toISO) {
  const a = new Date(fromISO + "T00:00:00");
  const b = new Date(toISO + "T00:00:00");
  return Math.round((b - a) / (1000 * 60 * 60 * 24));
}

function getEffectiveExpiry(item) {
  return item.expiryDate || item.estimatedExpiryDate;
}

/* =========================
   Derived fields
========================= */

function computeDerived(item) {
  if (item.status === "CONSUMED") {
    return {
      ...item,
      effectiveExpiry: null,
      daysLeft: null,
      expiryStatus: "CONSUMED",
    };
  }

  const expiry = getEffectiveExpiry(item);
  const today = todayISO();

  let daysLeft = null;
  let expiryStatus = "OK";

  if (expiry) {
    daysLeft = daysBetween(today, expiry);
    if (daysLeft < 0) expiryStatus = "EXPIRED";
    else if (daysLeft <= 3) expiryStatus = "EXPIRING_SOON";
  }

  return {
    ...item,
    effectiveExpiry: expiry,
    daysLeft,
    expiryStatus,
  };
}

/* =========================
   API (mock backend – stateless)
========================= */

export const api = {
  async getItems() {
    const items = loadItems();

    return items
      .map(computeDerived)
      .sort((a, b) => {
        const ea = a.effectiveExpiry || "9999-12-31";
        const eb = b.effectiveExpiry || "9999-12-31";
        return ea.localeCompare(eb);
      });
  },

  async getItemById(id) {
    const items = loadItems();
    const found = items.find((x) => x.id === Number(id));
    if (!found) throw new Error("Item not found");
    return computeDerived(found);
  },

  async createItem(payload) {
    const items = loadItems();

    const nextId =
      items.length === 0
        ? 1
        : Math.max(...items.map((x) => x.id)) + 1;

    const newItem = {
      id: nextId,
      status: "ACTIVE",
      source: payload.source || "manual",
      alertDismissedAt: null,
      ...payload,
    };

    const nextItems = [newItem, ...items];
    saveItems(nextItems);

    return computeDerived(newItem);
  },

  async updateItem(id, patch) {
    const items = loadItems();
    const idx = items.findIndex((x) => x.id === Number(id));
    if (idx === -1) throw new Error("Item not found");

    const updated = { ...items[idx], ...patch };
    const nextItems = [...items];
    nextItems[idx] = updated;

    saveItems(nextItems);
    return computeDerived(updated);
  },

  async deleteItem(id) {
    const items = loadItems();
    const nextItems = items.filter((x) => x.id !== Number(id));
    saveItems(nextItems);
    return { ok: true };
  },

  async getNotifications() {
    const items = loadItems();
    const today = todayISO();

    return items
      .map(computeDerived)
      .filter(
        (it) =>
          (it.expiryStatus === "EXPIRED" ||
            it.expiryStatus === "EXPIRING_SOON") &&
          !it.alertDismissedAt
      )
      .map((it) => ({
        id: it.id,
        itemId: it.id,
        type: it.expiryStatus,
        message:
          it.expiryStatus === "EXPIRED"
            ? `${it.name} đã hết hạn`
            : `${it.name} sắp hết hạn`,
        date: today,
      }))
      .sort((a, b) => b.date.localeCompare(a.date));
  },

  async clearNotifications() {
    const items = loadItems();
    const now = new Date().toISOString();

    const nextItems = items.map((it) => {
      const d = computeDerived(it);
      if (
        (d.expiryStatus === "EXPIRED" ||
          d.expiryStatus === "EXPIRING_SOON") &&
        !it.alertDismissedAt
      ) {
        return { ...it, alertDismissedAt: now };
      }
      return it;
    });

    saveItems(nextItems);
    return { ok: true };
  },

  async getMenuSuggestions() {
    const items = (await this.getItems()).filter(
      (x) => x.expiryStatus !== "OK" && x.expiryStatus !== "CONSUMED"
    );

    const names = items.map((x) => x.name.toLowerCase());
    const suggestions = [];

    const add = (title, uses, timeMin) =>
      suggestions.push({ id: title, title, uses, timeMin });

    if (names.some((n) => n.includes("trứng")) && names.some((n) => n.includes("rau"))) {
      add("Canh rau nấu trứng", ["Trứng gà", "Rau cải"], 20);
      add("Salad rau + trứng luộc", ["Trứng gà", "Rau cải"], 10);
    }

    if (names.some((n) => n.includes("thịt")) && names.some((n) => n.includes("rau"))) {
      add("Thịt xào rau", ["Thịt bò", "Rau cải"], 15);
    }

    if (suggestions.length === 0) {
      add("Món gợi ý đơn giản", items.map((x) => x.name), 15);
    }

    return { basedOn: items.map((x) => x.name), suggestions };
  },
};

/* =========================
   Enums
========================= */

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
