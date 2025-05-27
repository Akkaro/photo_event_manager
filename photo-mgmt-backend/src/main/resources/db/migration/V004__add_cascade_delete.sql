-- V004__add_cascade_delete.sql
-- Add CASCADE DELETE to photo_edit foreign key constraint

-- First, drop the existing foreign key constraint
ALTER TABLE photo_edit DROP CONSTRAINT IF EXISTS photo_edit_photo_id_fkey;

-- Add the foreign key constraint with CASCADE DELETE
ALTER TABLE photo_edit
    ADD CONSTRAINT photo_edit_photo_id_fkey
        FOREIGN KEY (photo_id) REFERENCES photo (photo_id) ON DELETE CASCADE;

-- Also add CASCADE DELETE for owner_id constraint if it doesn't exist
ALTER TABLE photo_edit DROP CONSTRAINT IF EXISTS photo_edit_owner_id_fkey;
ALTER TABLE photo_edit
    ADD CONSTRAINT photo_edit_owner_id_fkey
        FOREIGN KEY (owner_id) REFERENCES "user" (user_id) ON DELETE CASCADE;