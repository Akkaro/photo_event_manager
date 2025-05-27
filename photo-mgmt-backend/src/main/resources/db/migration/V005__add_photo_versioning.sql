-- V006__add_photo_versioning.sql
-- Add photo versioning support

-- Add original_path to photo table to store the original image
ALTER TABLE photo ADD COLUMN original_path VARCHAR(500);

-- Update existing photos to set original_path same as current path
UPDATE photo SET original_path = path WHERE original_path IS NULL;

-- Make original_path not null after setting default values
ALTER TABLE photo ALTER COLUMN original_path SET NOT NULL;

-- Add versioning fields to photo_edit table
ALTER TABLE photo_edit ADD COLUMN version_number INTEGER DEFAULT 1;
ALTER TABLE photo_edit ADD COLUMN previous_version_url VARCHAR(500);
ALTER TABLE photo_edit ADD COLUMN result_version_url VARCHAR(500);

-- Add index for version queries
CREATE INDEX idx_photo_edit_photo_version ON photo_edit (photo_id, version_number);

-- Add index for version number ordering
CREATE INDEX idx_photo_edit_version_number ON photo_edit (version_number DESC);