-- Create the user table
CREATE TABLE "user" (
                        user_id UUID PRIMARY KEY,
                        user_name VARCHAR(25) NOT NULL,
                        email VARCHAR(25) NOT NULL,
                        password_hash VARCHAR(255) NOT NULL,
                        role VARCHAR(25),
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create the user_session table
CREATE TABLE user_session (
                              session_id UUID PRIMARY KEY,
                              user_id UUID NOT NULL,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (user_id) REFERENCES "user" (user_id)
);

-- Create the album table
CREATE TABLE album (
                       album_id UUID PRIMARY KEY,
                       album_name VARCHAR(255) NOT NULL,
                       owner_id UUID NOT NULL,
                       qr_code VARCHAR(255),
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (owner_id) REFERENCES "user" (user_id)
);

-- Create the photo table
CREATE TABLE photo (
                       photo_id UUID PRIMARY KEY,
                       album_id UUID NOT NULL,
                       owner_id UUID NOT NULL,
                       path VARCHAR(255) NOT NULL,
                       uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       is_edited BOOLEAN NOT NULL DEFAULT FALSE,
                       FOREIGN KEY (album_id) REFERENCES album (album_id),
                       FOREIGN KEY (owner_id) REFERENCES "user" (user_id)
);

-- Create the photo_edit table
CREATE TABLE photo_edit (
                            edit_id UUID PRIMARY KEY,
                            photo_id UUID NOT NULL,
                            owner_id UUID NOT NULL,
                            brightness DECIMAL(5,2),
                            contrast DECIMAL(5,2),
                            edited_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (photo_id) REFERENCES photo (photo_id),
                            FOREIGN KEY (owner_id) REFERENCES "user" (user_id)
);