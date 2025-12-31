package org.yann.integerasiorderkuota.orderkuota.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;
import org.yann.integerasiorderkuota.orderkuota.exception.GenerateImageFailedException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class QrGenerator {

    private static final String LOGO_PATH = "logo/qris.png";

    private String generateQrString(String baseQrString, Long amount) {
        String qrCode = baseQrString.trim();
        String amountString = String.valueOf(amount);
        qrCode = qrCode.substring(0, qrCode.length() - 4);
        String step1 = qrCode.replace("010211", "010212");
        String[] step2 = step1.split("5802ID");
        String amountPart = "54" + String.format("%02d", amountString.length()) + amountString;
        amountPart += "5802ID";

        String fix = step2[0].trim() + amountPart + step2[1];
        fix += crcCalculator(fix);
        return fix;
    }

    private byte[] generateImage(String qr, int width, int height) throws WriterException, IOException {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(qr, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        
        File logoFile = new File(LOGO_PATH);
        if (logoFile.exists()) {
            qrImage = addLogoToQr(qrImage, logoFile);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(qrImage, "PNG", baos);
            return baos.toByteArray();
        }
    }

    private BufferedImage addLogoToQr(BufferedImage qrImage, File logoFile) throws IOException {
        BufferedImage logo = ImageIO.read(logoFile);
        Graphics2D g = qrImage.createGraphics();

        int maxWidth = qrImage.getWidth() / 5;
        int maxHeight = qrImage.getHeight() / 5;
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();
        
        if (logoWidth > maxWidth || logoHeight > maxHeight) {
            float ratio = Math.min((float) maxWidth / logoWidth, (float) maxHeight / logoHeight);
            logoWidth = Math.round(logoWidth * ratio);
            logoHeight = Math.round(logoHeight * ratio);
        }
        int x = (qrImage.getWidth() - logoWidth) / 2;
        int y = (qrImage.getHeight() - logoHeight) / 2;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.drawImage(logo, x, y, logoWidth, logoHeight, null);

        g.setStroke(new BasicStroke(2));
        g.setColor(Color.WHITE);
        g.drawRect(x, y, logoWidth, logoHeight);

        g.dispose();
        return qrImage;
    }

    private String crcCalculator(String qrString) {
        int crc = 0xFFFF;
        int stringLength = qrString.length();

        for (int c = 0; c < stringLength; c++) {
            crc ^= qrString.charAt(c) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc = crc << 1;
                }
            }
        }
        int hex = crc & 0xFFFF;
        String hexString = Integer.toHexString(hex).toUpperCase();
        if (hexString.length() == 3) {
            hexString = "0" + hexString;
        }
        return hexString;
    }

    public String generateQr(String baseQr, Long amount) {
        return generateQrString(baseQr, amount);
    }

    public byte[] generateQrImage(String baseAmount, Long amount, int width, int height) {
        try {
            String qrString = generateQrString(baseAmount, amount);
            return generateImage(qrString, width, height);
        } catch (WriterException | IOException e) {
            throw new GenerateImageFailedException("Gagal melakukan generate QRIS dengan logo: " + e.getMessage());
        }
    }
}
