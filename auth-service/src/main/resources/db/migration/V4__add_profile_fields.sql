-- V4: Standard MySQL syntax for adding profile fields
ALTER TABLE users ADD bio VARCHAR(250) AFTER avatar_url;
ALTER TABLE users MODIFY avatar_url LONGTEXT;
