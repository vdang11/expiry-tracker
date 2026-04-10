package com.expiry.infrastructure.image;

import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

@Component
public class ImageOptimizer {

    private static final int MAX_DIMENSION = 1280;
    private static final float JPEG_QUALITY = 0.80f;

    public OptimizedImage optimize(byte[] originalBytes, String originalContentType) {

        if (originalBytes == null || originalBytes.length == 0) {
            return new OptimizedImage(originalBytes, safeMime(originalContentType));
        }

        BufferedImage src;

        try (ByteArrayInputStream in = new ByteArrayInputStream(originalBytes)) {
            src = ImageIO.read(in);
        } catch (IOException e) {
            return new OptimizedImage(originalBytes, safeMime(originalContentType));
        }

        if (src == null) {
            return new OptimizedImage(originalBytes, safeMime(originalContentType));
        }

        BufferedImage scaled = scaleDownIfNeeded(src, MAX_DIMENSION);

        byte[] jpegBytes = encodeJpeg(scaled, JPEG_QUALITY, originalBytes);

        return new OptimizedImage(jpegBytes, "image/jpeg");
    }

    private BufferedImage scaleDownIfNeeded(BufferedImage src, int maxDim) {

        int w = src.getWidth();
        int h = src.getHeight();

        int max = Math.max(w, h);

        if (max <= maxDim) {
            return toRgb(src);
        }

        double scale = (double) maxDim / (double) max;

        int nw = (int) Math.round(w * scale);
        int nh = (int) Math.round(h * scale);

        BufferedImage rgb = toRgb(src);
        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = out.createGraphics();

        try {
            g.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            g.drawImage(rgb, 0, 0, nw, nh, null);

        } finally {
            g.dispose();
        }

        return out;
    }

    private BufferedImage toRgb(BufferedImage src) {

        if (src.getType() == BufferedImage.TYPE_INT_RGB) {
            return src;
        }

        BufferedImage rgb =
                new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g = rgb.createGraphics();

        try {

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, src.getWidth(), src.getHeight());

            g.drawImage(src, 0, 0, null);

        } finally {

            g.dispose();

        }

        return rgb;
    }

    /**
     * Encode JPEG with fallback to original bytes if compression fails
     */
    private byte[] encodeJpeg(BufferedImage img, float quality, byte[] originalBytes) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");

        if (!writers.hasNext()) {

            try {
                ImageIO.write(img, "jpg", baos);
                return baos.toByteArray();
            } catch (IOException e) {

                // fallback to original image
                return originalBytes;
            }
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {

            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            param.setCompressionQuality(
                    Math.max(0.1f, Math.min(quality, 1.0f))
            );
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {

            writer.setOutput(ios);

            writer.write(null, new IIOImage(img, null, null), param);

            return baos.toByteArray();

        } catch (IOException e) {

            // fallback to original image
            return originalBytes;

        } finally {

            writer.dispose();

        }
    }

    private String safeMime(String contentType) {

        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }

        return contentType;
    }

    public record OptimizedImage(byte[] bytes, String mimeType) {}
}