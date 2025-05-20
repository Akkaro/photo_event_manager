-- Insert users
INSERT INTO "user" (user_id, user_name, email, password_hash, role)
VALUES ('00000000-0000-0000-0000-000000000000', 'ADMIN USER', 'admin_user@mail.com', '$2a$10$97EBsv3ZT1gK9UyiH3PhqO9iHk1WjGwsMZn.lZNpKBwSVRo8Pa9tC', 'ADMIN');

INSERT INTO "user" (user_id, user_name, email, password_hash, role)
VALUES ('00000000-0000-0000-0000-000000000001', 'MODERATOR USER', 'moderator_user@mail.com', '$2a$10$eKKuuqBdky8SGEsda55Hu.lfAfMlOtqsAlpjme9jKkit5bX0zhtNK', 'MODERATOR');

INSERT INTO "user" (user_id, user_name, email, password_hash, role)
VALUES ('00000000-0000-0000-0000-000000000002', 'USER USER', 'user_user@mail.com', '$2a$10$eKKuuqBdky8SGEsda55Hu.lfAfMlOtqsAlpjme9jKkit5bX0zhtNK', 'USER');

-- Insert albums (using the user IDs from above)
INSERT INTO album (album_id, album_name, owner_id, qr_code)
VALUES ('86cc6c51-855d-48d4-aa3e-80d7d06c955b', 'Black and White', '00000000-0000-0000-0000-000000000000', 'QR_CODE_1'),
       ('04240c28-714a-4361-9d6d-80cac897a875', 'Portraits', '00000000-0000-0000-0000-000000000001', 'QR_CODE_2');

-- Insert photos (using the user IDs from above)
INSERT INTO photo (photo_id, photo_name, album_id, owner_id, path, is_edited)
VALUES ('d6399dfc-e3d7-4e75-98f2-c57c6aa67e45', 'afmsurf', '86cc6c51-855d-48d4-aa3e-80d7d06c955b',
        '00000000-0000-0000-0000-000000000000', 'https://res.cloudinary.com/debuvrlfm/image/upload/v1747774775/photos/00000000-0000-0000-0000-000000000000/sf5rp1var9pxlkec96uu.bmp', FALSE),
       ('3711c64b-674b-46be-8cae-df00359359ee', 'alumgrns', '86cc6c51-855d-48d4-aa3e-80d7d06c955b',
        '00000000-0000-0000-0000-000000000000', 'https://res.cloudinary.com/debuvrlfm/image/upload/v1747774799/photos/00000000-0000-0000-0000-000000000000/l4prxkgkpkmm6hn88bye.bmp', FALSE),
       ('eea2c8d3-b4a7-455a-b121-1b2e406ce074', 'cameraman', '86cc6c51-855d-48d4-aa3e-80d7d06c955b',
        '00000000-0000-0000-0000-000000000000', 'https://res.cloudinary.com/debuvrlfm/image/upload/v1747774821/photos/00000000-0000-0000-0000-000000000000/z2csjdyozaur2mqxroan.bmp', FALSE),
       ('8bf73141-eea6-45b1-90b2-44a1e5ce1ea4', 'moon', '86cc6c51-855d-48d4-aa3e-80d7d06c955b',
        '00000000-0000-0000-0000-000000000000', 'https://res.cloudinary.com/debuvrlfm/image/upload/v1747774853/photos/00000000-0000-0000-0000-000000000000/ptus0x5obsivhwtasqma.bmp', FALSE),
       ('9bfea051-7202-4f74-bf6b-b8305abc914b', 'flowers', '04240c28-714a-4361-9d6d-80cac897a875',
        '00000000-0000-0000-0000-000000000001', 'https://res.cloudinary.com/debuvrlfm/image/upload/v1747774900/photos/00000000-0000-0000-0000-000000000000/ql01mb4oasdrpofbyqai.bmp', FALSE),
       ('1493d2c0-3c6f-4ab4-abfe-bae1fa400caa', 'kids', '04240c28-714a-4361-9d6d-80cac897a875',
        '00000000-0000-0000-0000-000000000001', 'https://res.cloudinary.com/debuvrlfm/image/upload/v1747774919/photos/00000000-0000-0000-0000-000000000000/aciskeylmzoowxmfwh8q.bmp', FALSE),
       ('15d9b5f2-66e8-46fe-95a0-bfe554d9eb5f', 'Lena', '04240c28-714a-4361-9d6d-80cac897a875',
        '00000000-0000-0000-0000-000000000001', 'https://res.cloudinary.com/debuvrlfm/image/upload/v1747774938/photos/00000000-0000-0000-0000-000000000000/wluw3o5xftr3gpbwce6r.bmp', FALSE);

-- Insert photo edits
INSERT INTO photo_edit (edit_id, photo_id, owner_id, brightness, contrast)
VALUES ('30000000-0000-0000-0000-000000000000', '3711c64b-674b-46be-8cae-df00359359ee',
        '00000000-0000-0000-0000-000000000000', 1.20, 1.10),
       ('30000000-0000-0000-0000-000000000001', '9bfea051-7202-4f74-bf6b-b8305abc914b',
        '00000000-0000-0000-0000-000000000001', 0.95, 1.25);