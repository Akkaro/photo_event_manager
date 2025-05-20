package photo_mgmt_backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "photo_edit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhotoEditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "edit_id")
    private UUID editId;

    @Column(name = "photo_id")
    private UUID photoId;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "brightness")
    private BigDecimal brightness;

    @Column(name = "contrast")
    private BigDecimal contrast;

    @Column(name = "edited_at")
    private ZonedDateTime editedAt;

    @ManyToOne
    @JoinColumn(name = "photo_id", insertable = false, updatable = false)
    private PhotoEntity photo;

    @ManyToOne
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private UserEntity owner;
}