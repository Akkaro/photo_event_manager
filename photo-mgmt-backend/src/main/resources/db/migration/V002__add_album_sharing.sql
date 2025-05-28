CREATE TABLE album_share (
                             album_share_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             album_id UUID NOT NULL,
                             shared_with_user_id UUID NOT NULL,
                             shared_by_user_id UUID NOT NULL,
                             shared_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (album_id) REFERENCES album (album_id) ON DELETE CASCADE,
                             FOREIGN KEY (shared_with_user_id) REFERENCES "user" (user_id) ON DELETE CASCADE,
                             FOREIGN KEY (shared_by_user_id) REFERENCES "user" (user_id) ON DELETE CASCADE,
                             UNIQUE(album_id, shared_with_user_id)
);