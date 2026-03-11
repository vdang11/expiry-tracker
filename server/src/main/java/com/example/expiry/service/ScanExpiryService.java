package com.example.expiry.service;

import com.example.expiry.domain.InvalidImageException;
import com.example.expiry.dto.ExpiryResult;
import com.example.expiry.infrastructure.ai.OpenAIClient;
import com.example.expiry.infrastructure.ai.VisionImage;
import com.example.expiry.infrastructure.image.ImageOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
public class ScanExpiryService {

    private static final Logger log = LoggerFactory.getLogger(ScanExpiryService.class);

    private final OpenAIClient openAIClient;
    private final ExpiryDecisionEngine decisionEngine;
    private final ImageValidator imageValidator;
    private final ImageOptimizer imageOptimizer;

    public ScanExpiryService(
            OpenAIClient openAIClient,
            ExpiryDecisionEngine decisionEngine,
            ImageValidator imageValidator,
            ImageOptimizer imageOptimizer
    ) {
        this.openAIClient = openAIClient;
        this.decisionEngine = decisionEngine;
        this.imageValidator = imageValidator;
        this.imageOptimizer = imageOptimizer;
    }

    public ExpiryResult scan(List<MultipartFile> images) {

        imageValidator.validateAll(images);

        try {
            List<VisionImage> visionImages = new ArrayList<>();

            for (MultipartFile file : images) {
                byte[] originalBytes = file.getBytes();

                ImageOptimizer.OptimizedImage optimized =
                        imageOptimizer.optimize(originalBytes, file.getContentType());

                String base64 = Base64.getEncoder().encodeToString(optimized.bytes());

                visionImages.add(new VisionImage(optimized.mimeType(), base64));
            }

            ExpiryResult result = openAIClient.callVision(visionImages);

            applyNoPackageEstimation(result);

            String expiryDate = result.getExpiryDate();
            String dateType = result.getDateType();
            String imageQuality = result.getImageQuality();
            double confidence = result.getConfidence();

            boolean productAccepted =
                    result.getProductName() != null &&
                            result.getProductNameConfidence() >= 0.75;

            ExpiryDecisionEngine.Decision decision =
                    decisionEngine.decide(expiryDate, dateType, imageQuality, confidence);

            result.setStatus(decision.getStatus());
            result.setReason(decision.getReason());
            result.setSuggestedAction(decision.getSuggestedAction());
            result.setProductNameAccepted(productAccepted);

            // sanitize rejected results
            if ("REJECTED".equals(decision.getStatus())) {
                result.setExpiryDate("UNKNOWN");
                result.setDateType("UNKNOWN");
                result.setConfidence(0.0);
            }

            return result;

        } catch (InvalidImageException e) {
            throw e;

        } catch (Exception e) {
            log.warn("SCAN EXPIRY FAILED", e);

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

        if ("BLURRY".equals(quality)) return;
        if (!"NO".equals(pkg)) return;

        int aiDays = (result.getEstimatedShelfLifeDays() == null)
                ? 0
                : result.getEstimatedShelfLifeDays();

        int ruleDays =
                ShelfLifeRules.estimateDays(result.getItemCategory(), result.getFreshnessState());

        int days = (ruleDays > 0) ? ruleDays : aiDays;

        String cat = safeUpper(result.getItemCategory());
        int max = ("FROZEN".equals(cat)) ? 90 : 30;

        if (days <= 0 || days > max) {
            result.setExpiryDate("UNKNOWN");
            result.setDateType("UNKNOWN");
            result.setConfidence(0.0);
            return;
        }

        LocalDate estimated =
                LocalDate.now(ZoneId.systemDefault()).plusDays(days);

        result.setExpiryDate(estimated.toString());
        result.setDateType("ESTIMATED");
        result.setConfidence(Math.min(result.getConfidence(), 0.59));
        result.setEstimatedShelfLifeDays(days);
    }

    private String safeUpper(String s) {
        if (s == null) return "";
        return s.trim().toUpperCase(Locale.ROOT);
    }
}