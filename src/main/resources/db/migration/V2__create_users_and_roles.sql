-- Таблица ролей
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    member_id INT,
    trainer_id INT,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE SET NULL,
    FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE SET NULL
);

-- Таблица связи пользователей и ролей
CREATE TABLE IF NOT EXISTS user_roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Добавляем начальные роли (для H2 используем MERGE или INSERT IGNORE)
MERGE INTO roles (id, name) KEY(name) VALUES
    (1, 'ROLE_ADMIN'),
    (2, 'ROLE_MEMBER'),
    (3, 'ROLE_TRAINER');

-- Добавляем администратора по умолчанию
-- Пароль: admin123 (реальный BCrypt пароль)
MERGE INTO users (id, username, email, password) KEY(username) VALUES
    (1, 'admin', 'admin@fitnessclub.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1hBcZJH8p4p5pK1L5LpG9JtKQ2X7W3a');

-- Назначаем роль администратора
MERGE INTO user_roles (user_id, role_id) KEY(user_id, role_id) VALUES
    (1, 1);