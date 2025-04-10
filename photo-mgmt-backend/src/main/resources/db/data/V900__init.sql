-- Insert users
INSERT INTO "user" (user_id, user_name, email, password_hash, role)
VALUES ('00000000-0000-0000-0000-000000000000', 'ADMIN USER', 'admin_user@mail.com', '$2a$10$97EBsv3ZT1gK9UyiH3PhqO9iHk1WjGwsMZn.lZNpKBwSVRo8Pa9tC', 'ADMIN');

INSERT INTO "user" (user_id, user_name, email, password_hash, role)
VALUES ('00000000-0000-0000-0000-000000000001', 'MODERATOR USER', 'moderator_user@mail.com', '$2a$10$eKKuuqBdky8SGEsda55Hu.lfAfMlOtqsAlpjme9jKkit5bX0zhtNK', 'MODERATOR');

INSERT INTO "user" (user_id, user_name, email, password_hash, role)
VALUES ('00000000-0000-0000-0000-000000000002', 'USER USER', 'user_user@mail.com', '$2a$10$eKKuuqBdky8SGEsda55Hu.lfAfMlOtqsAlpjme9jKkit5bX0zhtNK', 'USER');

-- Insert albums (using the user IDs from above)
INSERT INTO album (album_id, album_name, owner_id, qr_code)
VALUES ('10000000-0000-0000-0000-000000000000', 'Trip to Paris', '00000000-0000-0000-0000-000000000000', 'QR_CODE_1'),
       ('10000000-0000-0000-0000-000000000001', 'Birthday Bash', '00000000-0000-0000-0000-000000000001', 'QR_CODE_2');

-- Insert photos (using the user IDs from above)
INSERT INTO photo (photo_id, album_id, owner_id, path, is_edited)
VALUES ('20000000-0000-0000-0000-000000000000', '10000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000', '/photos/paris1.jpg', TRUE),
       ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000002', '/photos/paris2.jpg', FALSE),
       ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001', '/photos/birthday1.jpg', TRUE);

-- Insert photo edits
INSERT INTO photo_edit (edit_id, photo_id, owner_id, brightness, contrast)
VALUES ('30000000-0000-0000-0000-000000000000', '20000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000', 1.20, 1.10),
       ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002',
        '00000000-0000-0000-0000-000000000001', 0.95, 1.25);