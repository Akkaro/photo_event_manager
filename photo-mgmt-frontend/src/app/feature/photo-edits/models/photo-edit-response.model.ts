export interface PhotoEditResponse {
  editId: string;
  photoId: string;
  ownerId: string;
  ownerName: string;

  // Basic adjustments
  brightness?: number;
  contrast?: number;
  gamma?: number;

  // Histogram equalization
  histogramEqualization?: boolean;

  // Blur settings
  blurKernelSize?: number;
  blurSigma?: number;

  // Edge detection
  edgeDetectionType?: string;

  // Morphological operations
  morphologicalOperation?: string;
  morphologicalKernelSize?: number;
  morphologicalIterations?: number;

  // Noise reduction
  noiseReduction?: string;

  // Thresholding
  thresholdValue?: number;
  thresholdType?: string;
  autoThreshold?: boolean;

  // HSV conversion
  hsvConversion?: boolean;

  // Combined processing flag
  combinedProcessing?: boolean;

  editedAt: string;
}
