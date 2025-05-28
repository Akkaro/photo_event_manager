-- V003__enhance_photo_edit_table.sql
-- Add new columns to support enhanced photo editing features

-- Add gamma correction
ALTER TABLE photo_edit ADD COLUMN gamma DECIMAL(5,2);

-- Add histogram equalization
ALTER TABLE photo_edit ADD COLUMN histogram_equalization BOOLEAN DEFAULT FALSE;

-- Add blur settings
ALTER TABLE photo_edit ADD COLUMN blur_kernel_size INTEGER;
ALTER TABLE photo_edit ADD COLUMN blur_sigma DECIMAL(5,2);

-- Add edge detection
ALTER TABLE photo_edit ADD COLUMN edge_detection_type VARCHAR(20);

-- Add morphological operations
ALTER TABLE photo_edit ADD COLUMN morphological_operation VARCHAR(20);
ALTER TABLE photo_edit ADD COLUMN morphological_kernel_size INTEGER;
ALTER TABLE photo_edit ADD COLUMN morphological_iterations INTEGER;

-- Add noise reduction
ALTER TABLE photo_edit ADD COLUMN noise_reduction VARCHAR(20);

-- Add thresholding
ALTER TABLE photo_edit ADD COLUMN threshold_value INTEGER;
ALTER TABLE photo_edit ADD COLUMN threshold_type VARCHAR(20);
ALTER TABLE photo_edit ADD COLUMN auto_threshold BOOLEAN DEFAULT FALSE;

-- Add HSV conversion
ALTER TABLE photo_edit ADD COLUMN hsv_conversion BOOLEAN DEFAULT FALSE;

-- Add combined processing flag
ALTER TABLE photo_edit ADD COLUMN combined_processing BOOLEAN DEFAULT FALSE;