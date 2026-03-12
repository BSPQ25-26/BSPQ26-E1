INSERT INTO users (username, email, password_hash, created_at) 
VALUES ('usuario1', 'user1@email.com', '1234', CURRENT_TIMESTAMP) 
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (username, email, password_hash, created_at) 
VALUES ('usuario2', 'user2@email.com', '1234', CURRENT_TIMESTAMP) 
ON CONFLICT (email) DO NOTHING;