CREATE database focusflow;
USE focusflow;
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(255)
);
CREATE TABLE songs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    song_number INT NOT NULL,
    song_name VARCHAR(255) NOT NULL,
    duration VARCHAR(20) NOT NULL
);
ALTER TABLE songs
ADD COLUMN file_path VARCHAR(500) NOT NULL;
CREATE TABLE user_songs (
    user_id INT NOT NULL,
    song_id INT NOT NULL,
    PRIMARY KEY (user_id, song_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE
);
CREATE TABLE videos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

ALTER TABLE songs ADD CONSTRAINT unique_song_path_per_user UNIQUE (file_path, id);
CREATE TABLE quotes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE task (
    task_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    title VARCHAR(100),
    date DATE,
    start_time TIME,
    end_time TIME,
    deadline DATETIME,
    is_completed BOOLEAN DEFAULT FALSE,
    reminded TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE note (
    note_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    content TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    task_id INT DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (task_id) REFERENCES task(task_id) ON DELETE SET NULL
);
ALTER TABLE task ADD COLUMN remind_at DATETIME NULL;
