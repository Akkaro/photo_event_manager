package photo_mgmt_backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "photo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhotoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "photo_id")
    private UUID photoId;

    @Column(name = "photo_name")
    private String photoName;

    @Column(name = "album_id")
    private UUID albumId;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "path")
    private String path;

    @Column(name = "original_path")
    private String originalPath;

    @Column(name = "uploaded_at")
    private ZonedDateTime uploadedAt;

    @Column(name = "is_edited")
    private Boolean isEdited;

    @ManyToOne
    @JoinColumn(name = "album_id", insertable = false, updatable = false)
    private AlbumEntity album;

    @ManyToOne
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private UserEntity owner;
}