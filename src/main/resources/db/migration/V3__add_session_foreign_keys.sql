-- Добавляем внешний ключ для user_sessions
ALTER TABLE user_sessions
ADD CONSTRAINT fk_user_sessions_user_id
FOREIGN KEY (user_id) REFERENCES users(id)
ON DELETE CASCADE;