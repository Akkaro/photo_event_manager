package photo_mgmt_backend.service.cloudinary;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface CloudinaryService {

    /**
     * Uploads an image file to Cloudinary
     *
     * @param file The image file to upload
     * @param ownerId The ID of the owner of the image
     * @return The URL of the uploaded image
     */
    String uploadImage(MultipartFile file, UUID ownerId);

    /**
     * Deletes an image from Cloudinary
     *
     * @param publicId The public ID of the image to delete
     */
    void deleteImage(String publicId);

    /**
     * Extracts the public ID from a Cloudinary URL
     *
     * @param url The Cloudinary URL
     * @return The public ID
     */
    String extractPublicIdFromUrl(String url);
}