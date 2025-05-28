export interface PhotoEditRequest {
  photoId: string;
  brightness?: number;
  contrast?: number;
  gamma?: number;

  histogramEqualization?: boolean;

  blurKernelSize?: number;
  blurSigma?: number;

  edgeDetectionType?: 'canny' | 'sobel';

  morphologicalOperation?: 'open' | 'close';
  morphologicalKernelSize?: number;
  morphologicalIterations?: number;

  noiseReduction?: 'bilateral' | 'median';

  thresholdValue?: number;
  thresholdType?: string;
  autoThreshold?: boolean;

  hsvConversion?: boolean;

  combinedProcessing?: boolean;
}
