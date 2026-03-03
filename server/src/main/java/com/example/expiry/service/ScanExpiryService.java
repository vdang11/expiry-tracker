package com.example.expiry.service;

import com.example.expiry.infrastructure.ai.OpenAIClient;
import com.example.expiry.dto.ExpiryResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Locale;

@Service
public class ScanExpiryService {

    private final OpenAIClient openAIClient;

    public ScanExpiryService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    public ExpiryResult scan(MultipartFile image) {
        try {
            // 1. Đọc bytes từ file upload
            byte[] bytes = image.getBytes();

            // 2. Encode BASE64 TẠI ĐÂY (ĐÚNG CHỖ)
            String base64 = Base64.getEncoder().encodeToString(bytes);

            // 3. Gọi OpenAI
            ExpiryResult result= openAIClient.callVision(base64);

            // 3.1 If no package, estimate expiry using conservative rules (always ESTIMATED + needs confirmation)
            applyNoPackageEstimation(result);

            // 4. Gọi ExpiryDecisionHelper

            // lấy dữ liệu AI đã parse ra
            String expiryDate = result.getExpiryDate();
            String dateType = result.getDateType();
            String imageQuality = result.getImageQuality();
            double confidence = result.getConfidence();
            boolean productAccepted = result.getProductName() != null && result.getProductNameConfidence() >= 0.75;

            // đưa vào decision helper
            ExpiryDecisionHelper.Decision decision = ExpiryDecisionHelper.decide(expiryDate, dateType, imageQuality, confidence);

            // enrich lại result
            result.setStatus(decision.getStatus());
            result.setReason(decision.getReason());
            result.setSuggestedAction(decision.getSuggestedAction());
            result.setProductNameAccepted(productAccepted);

            return result;


        } catch (Exception e) {
            System.out.println(">>> SCAN EXPIRY FAILED");
            e.printStackTrace();

            // Sprint 2 requirement: safer fallback (do not crash request)
            ExpiryResult fallback = new ExpiryResult();
            fallback.setExpiryDate("UNKNOWN");
            fallback.setDateType("UNKNOWN");
            fallback.setImageQuality("UNKNOWN");
            fallback.setPackagePresent("UNKNOWN");
            fallback.setItemCategory("UNKNOWN");
            fallback.setFreshnessState("UNKNOWN");
            fallback.setEstimatedShelfLifeDays(0);
            fallback.setConfidence(0.0);
            fallback.setProductName(null);
            fallback.setProductNameConfidence(0.0);
            fallback.setStatus("REJECTED");
            fallback.setReason("AI_ERROR");
            fallback.setSuggestedAction("ASK_USER_RESCAN");
            fallback.setProductNameAccepted(false);
            return fallback;
        }
    }

    private void applyNoPackageEstimation(ExpiryResult result) {
        if (result == null) return;

        String pkg = safeUpper(result.getPackagePresent());
        String quality = safeUpper(result.getImageQuality());

        // If image is blurry, don't try to estimate from it.
        if ("BLURRY".equals(quality)) return;

        if (!"NO".equals(pkg)) return;

        int aiDays = (result.getEstimatedShelfLifeDays() == null) ? 0 : result.getEstimatedShelfLifeDays();
        int ruleDays = ShelfLifeRules.estimateDays(result.getItemCategory(), result.getFreshnessState());

        int days = (ruleDays > 0) ? ruleDays : aiDays;

        // Hard safety caps (avoid crazy estimates)
        String cat = safeUpper(result.getItemCategory());
        int max = ("FROZEN".equals(cat)) ? 90 : 30;
        if (days <= 0 || days > max) {
            // Can't estimate safely
            result.setExpiryDate("UNKNOWN");
            result.setDateType("UNKNOWN");
            result.setConfidence(0.0);
            return;
        }

        LocalDate estimated = LocalDate.now(ZoneId.systemDefault()).plusDays(days);
        result.setExpiryDate(estimated.toString());
        result.setDateType("ESTIMATED");

        // Force below 0.6 per Sprint 2 threshold behavior (never auto-accept)
        result.setConfidence(Math.min(result.getConfidence(), 0.59));
        result.setEstimatedShelfLifeDays(days);
    }

    private String safeUpper(String s) {
        if (s == null) return "";
        return s.trim().toUpperCase(Locale.ROOT);
    }
}