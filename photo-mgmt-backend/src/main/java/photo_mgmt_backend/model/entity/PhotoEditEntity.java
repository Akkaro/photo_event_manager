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

    @Column(name = "gamma")
    private BigDecimal gamma;

    @Column(name = "histogram_equalization")
    private Boolean histogramEqualization;

    @Column(name = "blur_kernel_size")
    private Integer blurKernelSize;

    @Column(name = "blur_sigma")
    private BigDecimal blurSigma;

    @Column(name = "edge_detection_type")
    private String edgeDetectionType;

    @Column(name = "morphological_operation")
    private String morphologicalOperation;

    @Column(name = "morphological_kernel_size")
    private Integer morphologicalKernelSize;

    @Column(name = "morphological_iterations")
    private Integer morphologicalIterations;

    @Column(name = "noise_reduction")
    private String noiseReduction;

    @Column(name = "threshold_value")
    private Integer thresholdValue;

    @Column(name = "threshold_type")
    private String thresholdType;

    @Column(name = "auto_threshold")
    private Boolean autoThreshold;

    @Column(name = "hsv_conversion")
    private Boolean hsvConversion;

    @Column(name = "combined_processing")
    private Boolean combinedProcessing;

    @Column(name = "edited_at")
    private ZonedDateTime editedAt;

    @Column(name = "version_number")
    private Integer versionNumber;

    @Column(name = "previous_version_url")
    private String previousVersionUrl;

    @Column(name = "result_version_url")
    private String resultVersionUrl;

    @ManyToOne
    @JoinColumn(name = "photo_id", insertable = false, updatable = false)
    private PhotoEntity photo;

    @ManyToOne
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private UserEntity owner;
}