DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS workspaces;
DROP TABLE IF EXISTS users;

-- пользователи
CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    full_name   VARCHAR(150)        NOT NULL,
    email       VARCHAR(150)        NOT NULL UNIQUE,
    phone       VARCHAR(30),
    role        VARCHAR(20)         NOT NULL DEFAULT 'CLIENT'
                    CHECK (role IN ('CLIENT', 'ADMIN')),
    created_at  TIMESTAMP           NOT NULL DEFAULT now()
);

-- рабочие места
CREATE TABLE workspaces (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL UNIQUE,
    type            VARCHAR(20)     NOT NULL
                        CHECK (type IN ('HOT_DESK', 'MEETING_ROOM', 'PRIVATE_OFFICE', 'CONFERENCE_HALL')),
    capacity        INT             NOT NULL CHECK (capacity > 0),
    price_per_hour  NUMERIC(10, 2)  NOT NULL CHECK (price_per_hour > 0),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE
);

-- бронирования, связывает users и workspaces
CREATE TABLE bookings (
    id            SERIAL PRIMARY KEY,
    user_id       INT             NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    workspace_id  INT             NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    start_time    TIMESTAMP       NOT NULL,
    end_time      TIMESTAMP       NOT NULL,
    status        VARCHAR(20)     NOT NULL DEFAULT 'CREATED'
                      CHECK (status IN ('CREATED', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    total_price   NUMERIC(10, 2)  NOT NULL CHECK (total_price >= 0),
    created_at    TIMESTAMP       NOT NULL DEFAULT now(),
    CHECK (end_time > start_time)
);

CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_workspace ON bookings(workspace_id);
CREATE INDEX idx_bookings_status ON bookings(status);

-- тестовые данные

INSERT INTO users (full_name, email, phone, role) VALUES
    ('Иванов Иван Иванович',    'ivanov@example.com',    '+7-900-100-00-01', 'CLIENT'),
    ('Петрова Анна Сергеевна',  'petrova@example.com',   '+7-900-100-00-02', 'CLIENT'),
    ('Сидоров Пётр Алексеевич', 'sidorov@example.com',   '+7-900-100-00-03', 'CLIENT'),
    ('Кузнецова Мария Олеговна','kuznecova@example.com', '+7-900-100-00-04', 'CLIENT'),
    ('Смирнов Артём Дмитриевич','smirnov@example.com',   '+7-900-100-00-05', 'CLIENT'),
    ('Администратор Системы',   'admin@example.com',     '+7-900-100-00-06', 'ADMIN');

INSERT INTO workspaces (name, type, capacity, price_per_hour, is_active) VALUES
    ('Стол A1',              'HOT_DESK',        1,  150.00, TRUE),
    ('Стол A2',              'HOT_DESK',        1,  150.00, TRUE),
    ('Стол A3',              'HOT_DESK',        1,  150.00, TRUE),
    ('Переговорная "Альфа"', 'MEETING_ROOM',    6,  800.00, TRUE),
    ('Переговорная "Бета"',  'MEETING_ROOM',    4,  600.00, TRUE),
    ('Кабинет "Люкс"',       'PRIVATE_OFFICE',  2, 1200.00, TRUE),
    ('Конференц-зал',        'CONFERENCE_HALL', 20, 2500.00, TRUE),
    ('Стол B1',              'HOT_DESK',        1,  150.00, FALSE);

INSERT INTO bookings (user_id, workspace_id, start_time, end_time, status, total_price) VALUES
    (1, 1, '2026-09-10 09:00', '2026-09-10 12:00', 'COMPLETED', 450.00),
    (2, 4, '2026-09-11 10:00', '2026-09-11 12:00', 'COMPLETED', 1600.00),
    (3, 6, '2026-09-12 09:00', '2026-09-12 18:00', 'CONFIRMED', 10800.00),
    (1, 2, '2026-09-15 13:00', '2026-09-15 17:00', 'CONFIRMED',  600.00),
    (4, 5, '2026-09-16 09:00', '2026-09-16 11:00', 'CREATED',   1200.00),
    (5, 3, '2026-09-16 14:00', '2026-09-16 16:00', 'CREATED',    300.00),
    (2, 7, '2026-09-17 08:00', '2026-09-17 20:00', 'CONFIRMED', 30000.00),
    (3, 1, '2026-09-18 09:00', '2026-09-18 10:00', 'CANCELLED',  150.00),
    (4, 4, '2026-09-19 15:00', '2026-09-19 18:00', 'CREATED',   2400.00),
    (5, 6, '2026-09-20 10:00', '2026-09-20 14:00', 'CREATED',   4800.00),
    (1, 5, '2026-09-05 09:00', '2026-09-05 10:00', 'COMPLETED',  600.00),
    (2, 2, '2026-09-06 11:00', '2026-09-06 13:00', 'CANCELLED',  300.00);
