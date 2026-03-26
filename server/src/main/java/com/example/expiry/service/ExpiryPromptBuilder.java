package com.example.expiry.service;

import org.springframework.stereotype.Service;

@Service
public class ExpiryPromptBuilder {

    public String buildPrompt() {

        return """
                You are extracting information from one or more images of the SAME food item package.
                Different images may show different sides of the package.

                IMPORTANT: Combine information across all images before answering.

                --------------------------------
                VISION ATTENTION STRATEGY
                --------------------------------

                STEP 1 — SEARCH FOR EXPIRY KEYWORDS FIRST

                Carefully scan the images for expiry-related keywords such as:

                EXP
                EXPIRY
                BEST BEFORE
                USE BY
                BBE
                DATE

                These keywords usually appear near the expiry date.

                STEP 2 — READ THE DATE NEAR THE KEYWORD

                If a keyword is found, read the nearby date.
                The date may appear above, below, or next to the keyword.
                
                Only extract a date if ALL conditions are true:
                
                1. A clear expiry keyword is visible.
                2. A readable date appears next to that keyword.
                3. The characters forming the date are visually distinguishable.
                
                Do NOT guess numbers.
                
                Do NOT invent missing digits.
                
                Do NOT attempt to "complete" blurry numbers.
                
                If the printed characters are dot-matrix and unclear,
                you must return UNKNOWN.
                
                --------------------------------
                DOT MATRIX PRINT RULE
                --------------------------------
                
                Many expiry dates are printed with dot-matrix ink.
                
                If the dot-matrix characters are:
                
                - incomplete
                - partially missing
                - merged together
                - visually ambiguous
                
                You must treat the date as unreadable.
                
                Return:
                
                expiryDate="UNKNOWN"
                imageQuality="BLURRY"
                confidence=0.0
                
                Do NOT estimate the numbers.
                
                --------------------------------
                VALID DATE PATTERNS
                --------------------------------
                
                Common formats used in Australian packaging include:
                
                DD/MM/YYYY
                DD/MM/YY
                YYYY-MM-DD
                DD MON YY
                DD MON YYYY
                DayMonthYear format such as:
                12JAN26
                27MAR2026
                
                Examples:
                
                USE BY 27 MAR 26
                BEST BEFORE 12/09/2025
                BBE 2026-03-27
                EXP 15 JAN 25
                
                STEP 3 — ONLY AFTER EXPIRY CHECK

                Identify the FOOD PRODUCT NAME on the packaging.

                --------------------------------
                IMPORTANT SAFETY RULES
                --------------------------------

                - Do NOT guess a random expiry date if there are many numbers in the photo.
                - If the expiry text is unreadable/blurry, return UNKNOWN and set imageQuality="BLURRY".

                --------------------------------
                RETURN JSON ONLY
                --------------------------------

                {
                  "expiryDate": "YYYY-MM-DD or UNKNOWN",
                  "dateType": "CONFIRMED|ESTIMATED|UNKNOWN",
                  "imageQuality": "OK|BLURRY|UNKNOWN",
                  "packagePresent": "YES|NO|UNKNOWN",
                  "itemCategory": "FRESH_PRODUCE|FRESH_MEAT|FRESH_SEAFOOD|DAIRY|BAKERY|READY_TO_EAT|PANTRY_PACKAGED|FROZEN|BEVERAGE|UNKNOWN",
                  "freshnessState": "FRESH|NEW|RIPE|OPENED|COOKED|LEFTOVER|UNKNOWN",
                  "estimatedShelfLifeDays": 0,
                  "confidence": 0.0,
                  "productName": "string or null",
                  "productNameConfidence": 0.0
                }
                
                --------------------------------
                EXPIRY RULES
                --------------------------------

                1) If you can see a full expiry date (DD/MM/YYYY, YYYY-MM-DD, MM/DD/YYYY):
                   convert it to ISO format YYYY-MM-DD,
                   set dateType="CONFIRMED".

                2) If only MONTH/YEAR is visible (e.g. 03/2026, MAR 2026):
                   set expiryDate to the LAST day of that month,
                   set dateType="ESTIMATED",
                   confidence MUST be < 0.6.

                3) If only YEAR or nothing is visible:
                   set expiryDate="UNKNOWN",
                   dateType="UNKNOWN",
                   confidence=0.0.

                --------------------------------
                PACKAGE RULES
                --------------------------------

                If a package or printed label is visible → packagePresent="YES".
                If food appears unpackaged (loose fruit, cooked meal, etc.) → packagePresent="NO".
                Otherwise → packagePresent="UNKNOWN".

                --------------------------------
                ESTIMATION MODE
                --------------------------------

                ONLY when packagePresent="NO":

                - Do NOT pretend you saw a printed expiry date.
                - Set dateType="ESTIMATED".
                - confidence < 0.6.
                - Choose itemCategory and freshnessState.
                - Provide conservative estimatedShelfLifeDays.

                --------------------------------
                PRODUCT NAME RULES
                --------------------------------

                - productName: short food item name only.
                - Ignore weight, nutrition, expiry text, slogans.
                - If not found → return null.
                - productNameConfidence between 0.0 and 1.0
                """;
    }
}