export interface PhotoEditResponse {
  editId: string;
  photoId: string;
  ownerId: string;
  ownerName: string;

  brightness?: number;
  contrast?: number;
  gamma?: number;

  histogramEqualization?: boolean;

  blurKernelSize?: number;
  blurSigma?: number;

  edgeDetectionType?: string;

  morphologicalOperation?: string;
  morphologicalKernelSize?: number;
  morphologicalIterations?: number;

  noiseReduction?: string;

  thresholdValue?: number;
  thresholdType?: string;
  autoThreshold?: boolean;

  hsvConversion?: boolean;

  combinedProcessing?: boolean;

  editedAt: string;
}
