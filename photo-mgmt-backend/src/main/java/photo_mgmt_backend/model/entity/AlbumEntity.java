package photo_mgmt_backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "album")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "album_id")
    private UUID albumId;

    @Column(name = "album_name")
    private String albumName;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "public_token")
    private String publicToken;

    @ManyToOne
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private UserEntity owner;
}