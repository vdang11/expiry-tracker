package com.expiry.entity;

/**
 * What kind of thing an item is. Only INGREDIENT is used for recipes;
 * everything else is still tracked for expiry reminders.
 */
public enum ItemCategory {
    INGREDIENT,   // food used for cooking: vegetables, meat, eggs, dairy, rice, sauces...
    READY_FOOD,   // eaten as-is: instant noodles, snacks, soft drinks, supplements...
    MEDICINE,
    COSMETIC,     // cosmetics, skincare, toiletries
    OTHER         // anything else: names, gibberish, non-consumables
}
