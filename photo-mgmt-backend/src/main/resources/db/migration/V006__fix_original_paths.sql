-- Fix any photos that have null or empty original_path

-- Update photos where original_path is null or empty to use current path
UPDATE photo
SET original_path = path
WHERE original_path IS NULL OR original_path = '';

-- Ensure original_path is never null going forward
ALTER TABLE photo ALTER COLUMN original_path SET NOT NULL;