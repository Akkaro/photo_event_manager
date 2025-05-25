package photo_mgmt_backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "album_share")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumShareEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "album_share_id")
    private UUID albumShareId;

    @Column(name = "album_id")
    private UUID albumId;

    @Column(name = "shared_with_user_id")
    private UUID sharedWithUserId;

    @Column(name = "shared_by_user_id")
    private UUID sharedByUserId;

    @Column(name = "shared_at")
    private ZonedDateTime sharedAt;

    @ManyToOne
    @JoinColumn(name = "album_id", insertable = false, updatable = false)
    private AlbumEntity album;

    @ManyToOne
    @JoinColumn(name = "shared_with_user_id", insertable = false, updatable = false)
    private UserEntity sharedWithUser;

    @ManyToOne
    @JoinColumn(name = "shared_by_user_id", insertable = false, updatable = false)
    private UserEntity sharedByUser;
}
