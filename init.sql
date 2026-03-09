CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS tours (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    from_location   VARCHAR(255) NOT NULL,
    to_location     VARCHAR(255) NOT NULL,
    transport_type  VARCHAR(50) NOT NULL,
    tour_distance   DOUBLE PRECISION,
    estimated_time  INTEGER,
    tour_image_path VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tour_logs (
    id              BIGSERIAL PRIMARY KEY,
    tour_id         BIGINT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    date_time       TIMESTAMP NOT NULL,
    comment         TEXT,
    difficulty      INTEGER NOT NULL CHECK (difficulty BETWEEN 1 AND 10),
    total_distance  DOUBLE PRECISION NOT NULL,
    total_time      INTEGER NOT NULL,
    rating          INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tour_logs_tour_id ON tour_logs(tour_id);