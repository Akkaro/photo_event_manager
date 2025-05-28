package photo_mgmt_backend.model.dto.public_album;

public record PublicAlbumUrlResponseDTO(
        String publicUrl,
        String qrCodeBase64
) { }
