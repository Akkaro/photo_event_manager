-- Add public sharing functionality to albums

-- Add columns for public sharing
ALTER TABLE album ADD COLUMN is_public BOOLEAN DEFAULT FALSE;
ALTER TABLE album ADD COLUMN public_token VARCHAR(255);

-- Add unique constraint on public_token
ALTER TABLE album ADD CONSTRAINT uk_album_public_token UNIQUE (public_token);

-- Add index for faster lookups
CREATE INDEX idx_album_public_token ON album (public_token) WHERE public_token IS NOT NULL;
CREATE INDEX idx_album_is_public ON album (is_public) WHERE is_public = TRUE;