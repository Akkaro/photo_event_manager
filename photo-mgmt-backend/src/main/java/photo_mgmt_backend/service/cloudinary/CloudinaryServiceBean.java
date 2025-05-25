package photo_mgmt_backend.service.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceBean implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, UUID ownerId) {
        try {
            // Create a folder structure based on owner ID
            String folder = "photos/" + ownerId;

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto"
                    )
            );

            // Return the secure URL of the uploaded image
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new RuntimeException("Image upload failed", e);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary", e);
            throw new RuntimeException("Image deletion failed", e);
        }
    }

    @Override
    public String extractPublicIdFromUrl(String url) {
        // Example URL: https://res.cloudinary.com/your-cloud-name/image/upload/v1234567890/photos/user-id/image.jpg
        // We need to extract: photos/user-id/image
        if (url == null || url.isEmpty()) {
            return null;
        }

        String[] parts = url.split("/upload/");
        if (parts.length < 2) {
            return null;
        }

        String part = parts[1];
        // Remove version number (v1234567890/) if present
        if (part.contains("/")) {
            String[] versionSplit = part.split("/", 2);
            part = versionSplit.length > 1 ? versionSplit[1] : part;
        }

        // Remove file extension
        int extensionIndex = part.lastIndexOf('.');
        if (extensionIndex > 0) {
            part = part.substring(0, extensionIndex);
        }

        return part;
    }

    @Override
    public String uploadEditedImage(byte[] imageBytes, UUID ownerId) {
        try {
            String folder = "photos/" + ownerId + "/edited";

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    imageBytes,
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto"
                    )
            );

            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Failed to upload edited image to Cloudinary", e);
            throw new RuntimeException("Image upload failed", e);
        }
    }
}