package photo_mgmt_backend.model.dto.photo_edit;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PhotoEditRequestDTO(
        @NotNull(message = "Photo ID is required.")
        UUID photoId,

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
        String edgeDetectionType, // "canny", "sobel", or null

        // Morphological operations
        String morphologicalOperation, // "open", "close", or null
        Integer morphologicalKernelSize,
        Integer morphologicalIterations,

        // Noise reduction
        String noiseReduction, // "bilateral", "median", or null

        // Thresholding
        Integer thresholdValue,
        String thresholdType, // "binary" or null
        Boolean autoThreshold,

        // HSV conversion
        Boolean hsvConversion,

        // Combined processing flag
        Boolean combinedProcessing
) { }