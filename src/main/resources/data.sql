-- Добавить пользователя с паролем "password" (зашифруй через BCrypt)
INSERT INTO users (id, username, password) VALUES (1, 'admin', '$2a$10$xyz...');
