export interface PhotoEditRequest {
  photoId: string;
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
  edgeDetectionType?: 'canny' | 'sobel';

  // Morphological operations
  morphologicalOperation?: 'open' | 'close';
  morphologicalKernelSize?: number;
  morphologicalIterations?: number;

  // Noise reduction
  noiseReduction?: 'bilateral' | 'median';

  // Thresholding
  thresholdValue?: number;
  thresholdType?: string;
  autoThreshold?: boolean;

  // HSV conversion
  hsvConversion?: boolean;

  // Combined processing flag
  combinedProcessing?: boolean;
}
