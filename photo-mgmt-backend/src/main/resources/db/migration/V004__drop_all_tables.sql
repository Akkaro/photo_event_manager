-- Drop tables in order to respect foreign key constraints
-- We need to drop tables with foreign keys first before dropping tables they reference

-- Drop photo_edit table
DROP TABLE IF EXISTS photo_edit;

-- Drop photo table
DROP TABLE IF EXISTS photo;

-- Drop album table
DROP TABLE IF EXISTS album;

-- Drop user_session table
DROP TABLE IF EXISTS user_session;

-- Drop user table
DROP TABLE IF EXISTS "user";