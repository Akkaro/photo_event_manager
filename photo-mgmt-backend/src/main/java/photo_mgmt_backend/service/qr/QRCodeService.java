package photo_mgmt_backend.service.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Service
public class QRCodeService {

    @Value("${app.base-url:http://localhost:4200}")
    private String baseUrl;

    private static final int QR_CODE_SIZE = 300;

    /**
     * Generate QR code as base64 string for a public album
     */
    public String generateQRCodeForAlbum(String publicToken) {
        try {
            String publicUrl = generatePublicAlbumUrl(publicToken);
            log.info("Generating QR code for URL: {}", publicUrl);

            return generateQRCodeBase64(publicUrl);
        } catch (Exception e) {
            log.error("Failed to generate QR code for token: {}", publicToken, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    /**
     * Generate QR code as base64 string for any URL
     */
    public String generateQRCodeBase64(String url) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        byte[] pngData = pngOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(pngData);
    }

    /**
     * Generate the public album URL
     */
    public String generatePublicAlbumUrl(String publicToken) {
        return baseUrl + "/public/album/" + publicToken;
    }

    /**
     * Generate QR code as byte array (for direct download)
     */
    public byte[] generateQRCodeBytes(String url) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        return pngOutputStream.toByteArray();
    }
}