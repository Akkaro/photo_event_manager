import { EditOperation, EditCategory } from '../models/edit-operation.model';

export const EDIT_OPERATIONS: EditOperation[] = [
  // Basic Adjustments
  {
    id: 'brightness-contrast',
    name: 'Brightness & Contrast',
    description: 'Adjust image brightness and contrast levels',
    category: EditCategory.BASIC,
    icon: 'fas fa-sun',
    requiresParameters: true,
    parameters: [
      {
        name: 'brightness',
        type: 'number',
        label: 'Brightness',
        min: -100,
        max: 100,
        step: 1,
        defaultValue: 0,
        required: false
      },
      {
        name: 'contrast',
        type: 'number',
        label: 'Contrast',
        min: 0.1,
        max: 3.0,
        step: 0.1,
        defaultValue: 1.0,
        required: false
      }
    ]
  },
  {
    id: 'gamma',
    name: 'Gamma Correction',
    description: 'Adjust image gamma for exposure correction',
    category: EditCategory.BASIC,
    icon: 'fas fa-adjust',
    requiresParameters: true,
    parameters: [
      {
        name: 'gamma',
        type: 'number',
        label: 'Gamma',
        min: 0.1,
        max: 3.0,
        step: 0.1,
        defaultValue: 1.0,
        required: true
      }
    ]
  },

  // Image Enhancement
  {
    id: 'histogram-equalization',
    name: 'Histogram Equalization',
    description: 'Enhance image contrast using histogram equalization',
    category: EditCategory.ENHANCEMENT,
    icon: 'fas fa-chart-area',
    requiresParameters: false
  },
  {
    id: 'denoise',
    name: 'Noise Reduction',
    description: 'Remove noise from the image',
    category: EditCategory.ENHANCEMENT,
    icon: 'fas fa-broom',
    requiresParameters: true,
    parameters: [
      {
        name: 'type',
        type: 'select',
        label: 'Filter Type',
        defaultValue: 'bilateral',
        required: true,
        options: [
          { value: 'bilateral', label: 'Bilateral Filter' },
          { value: 'median', label: 'Median Filter' }
        ]
      }
    ]
  },

  // Effects & Filters
  {
    id: 'blur',
    name: 'Gaussian Blur',
    description: 'Apply Gaussian blur effect',
    category: EditCategory.EFFECTS,
    icon: 'fas fa-circle-notch',
    requiresParameters: true,
    parameters: [
      {
        name: 'kernelSize',
        type: 'number',
        label: 'Blur Intensity',
        min: 3,
        max: 51,
        step: 2,
        defaultValue: 15,
        required: true
      },
      {
        name: 'sigma',
        type: 'number',
        label: 'Sigma',
        min: 0.1,
        max: 10.0,
        step: 0.1,
        defaultValue: 2.0,
        required: false
      }
    ]
  },
  {
    id: 'edge-detection',
    name: 'Edge Detection',
    description: 'Detect and highlight edges in the image',
    category: EditCategory.EFFECTS,
    icon: 'fas fa-vector-square',
    requiresParameters: true,
    parameters: [
      {
        name: 'type',
        type: 'select',
        label: 'Algorithm',
        defaultValue: 'canny',
        required: true,
        options: [
          { value: 'canny', label: 'Canny Edge Detection' },
          { value: 'sobel', label: 'Sobel Edge Detection' }
        ]
      }
    ]
  },
  {
    id: 'hsv-convert',
    name: 'HSV Color Space',
    description: 'Convert image to HSV color representation',
    category: EditCategory.EFFECTS,
    icon: 'fas fa-palette',
    requiresParameters: false
  },

  // Advanced Processing
  {
    id: 'threshold',
    name: 'Thresholding',
    description: 'Apply binary thresholding to create black and white image',
    category: EditCategory.ADVANCED,
    icon: 'fas fa-divide',
    requiresParameters: true,
    parameters: [
      {
        name: 'threshold',
        type: 'number',
        label: 'Threshold Value',
        min: 0,
        max: 255,
        step: 1,
        defaultValue: 128,
        required: true
      },
      {
        name: 'type',
        type: 'select',
        label: 'Threshold Type',
        defaultValue: 'binary',
        required: false,
        options: [
          { value: 'binary', label: 'Binary' }
        ]
      }
    ]
  },
  {
    id: 'auto-threshold',
    name: 'Auto Thresholding',
    description: 'Automatic thresholding using Otsu\'s method',
    category: EditCategory.ADVANCED,
    icon: 'fas fa-magic',
    requiresParameters: false
  },
  {
    id: 'morphological',
    name: 'Morphological Operations',
    description: 'Apply morphological opening or closing operations',
    category: EditCategory.ADVANCED,
    icon: 'fas fa-shapes',
    requiresParameters: true,
    parameters: [
      {
        name: 'operation',
        type: 'select',
        label: 'Operation',
        defaultValue: 'open',
        required: true,
        options: [
          { value: 'open', label: 'Opening' },
          { value: 'close', label: 'Closing' }
        ]
      },
      {
        name: 'kernelSize',
        type: 'number',
        label: 'Kernel Size',
        min: 3,
        max: 15,
        step: 2,
        defaultValue: 5,
        required: true
      },
      {
        name: 'iterations',
        type: 'number',
        label: 'Iterations',
        min: 1,
        max: 10,
        step: 1,
        defaultValue: 1,
        required: false
      }
    ]
  }
];
