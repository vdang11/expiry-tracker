package com.example.expiry.service;

import com.example.expiry.infrastructure.OpenAIClient;
import com.example.expiry.dto.ExpiryResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

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

            // 4. Gọi ExpiryDecisionHelper

            // lấy dữ liệu AI đã parse ra
            String expiryDate = result.getExpiryDate();
            double confidence = result.getConfidence();
            boolean productAccepted = result.getProductName() != null && result.getProductNameConfidence() >= 0.75;

            // đưa vào decision helper
            ExpiryDecisionHelper.Decision decision = ExpiryDecisionHelper.decide(expiryDate, confidence);

            // enrich lại result
            result.setStatus(decision.getStatus());
            result.setReason(decision.getReason());
            result.setSuggestedAction(decision.getSuggestedAction());
            result.setProductNameAccepted(productAccepted);

            return result;


        } catch (Exception e) {
            System.out.println(">>> SCAN EXPIRY FAILED");
            e.printStackTrace();
            throw new RuntimeException("Scan expiry failed", e);
        }
    }
}