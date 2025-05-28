export interface EditOperation {
  id: string;
  name: string;
  description: string;
  category: EditCategory;
  icon: string;
  requiresParameters: boolean;
  parameters?: EditParameter[];
}

export interface EditParameter {
  name: string;
  type: 'number' | 'boolean' | 'select';
  label: string;
  min?: number;
  max?: number;
  step?: number;
  defaultValue?: any;
  options?: { value: any; label: string }[];
  required?: boolean;
}

export enum EditCategory {
  BASIC = 'Basic Adjustments',
  ENHANCEMENT = 'Image Enhancement',
  EFFECTS = 'Effects & Filters',
  ADVANCED = 'Advanced Processing'
}
