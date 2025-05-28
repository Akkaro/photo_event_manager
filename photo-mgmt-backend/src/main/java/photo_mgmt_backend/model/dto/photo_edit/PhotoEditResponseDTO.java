package photo_mgmt_backend.model.dto.photo_edit;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.ZonedDateTime;

public record PhotoEditResponseDTO(
        UUID editId,
        UUID photoId,
        UUID ownerId,
        String ownerName,

        // Basic adjustments
        BigDecimal brightness,
        BigDecimal contrast,
        BigDecimal gamma,

        // Histogram equalization
        Boolean histogramEqualization,

        // Blur settings
        Integer blurKernelSize,
        BigDecimal blurSigma,

        // Edge detection
        String edgeDetectionType,

        // Morphological operations
        String morphologicalOperation,
        Integer morphologicalKernelSize,
        Integer morphologicalIterations,

        // Noise reduction
        String noiseReduction,

        // Thresholding
        Integer thresholdValue,
        String thresholdType,
        Boolean autoThreshold,

        // HSV conversion
        Boolean hsvConversion,

        // Combined processing flag
        Boolean combinedProcessing,

        ZonedDateTime editedAt,

        Integer versionNumber,
        String previousVersionUrl,
        String resultVersionUrl
) { }