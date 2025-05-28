-- Increase the size of qr_code column to accommodate base64 encoded QR codes

-- Base64 encoded QR codes can be quite large (typically 10KB+ for a 300x300 PNG)
-- We'll use TEXT type which can handle much larger strings
ALTER TABLE album ALTER COLUMN qr_code TYPE TEXT;

-- Also ensure public_token has adequate size (though 255 should be enough)
-- But let's be safe and make it a bit larger
ALTER TABLE album ALTER COLUMN public_token TYPE VARCHAR(500);