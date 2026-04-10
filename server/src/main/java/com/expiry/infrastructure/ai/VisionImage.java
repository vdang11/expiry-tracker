package com.expiry.infrastructure.ai;

/**
 * One image payload for OpenAI Vision.
 * We keep mimeType so we can build: data:{mimeType};base64,{base64}
 */
public record VisionImage(String mimeType, String base64) {}