CREATE TABLE IF NOT EXISTS wishes (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    quantity DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    claimer_id VARCHAR(50),
    claimed_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wishes_user_id ON wishes(user_id);
CREATE INDEX idx_wishes_status ON wishes(status);
CREATE INDEX idx_wishes_claimer_id ON wishes(claimer_id);
