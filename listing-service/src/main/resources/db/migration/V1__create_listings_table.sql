-- Initial schema for listings table

CREATE TABLE IF NOT EXISTS listings (
    id VARCHAR(50) PRIMARY KEY,
    seller_id VARCHAR(50) NOT NULL,
    receipt_id VARCHAR(50) NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    quantity NUMERIC(10, 2) NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    total_price NUMERIC(10, 2) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    buyer_id VARCHAR(50),
    purchased_date TIMESTAMP,
    view_count INTEGER NOT NULL DEFAULT 0,
    cart_add_count INTEGER NOT NULL DEFAULT 0,
    last_viewed TIMESTAMP,
    receipt_item_id BIGINT,
    original_price NUMERIC(10, 2),
    seller_expense NUMERIC(10, 2),
    buyer_expense NUMERIC(10, 2),
    split_percentage NUMERIC(5, 2),
    seller_latitude DOUBLE PRECISION,
    seller_longitude DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on seller_id for efficient queries
CREATE INDEX IF NOT EXISTS idx_listings_seller_id ON listings(seller_id);

-- Create index on status for filtering available listings
CREATE INDEX IF NOT EXISTS idx_listings_status ON listings(status);

-- Create index on created_at for sorting
CREATE INDEX IF NOT EXISTS idx_listings_created_at ON listings(created_at DESC);

-- Create index on seller location for distance-based queries
CREATE INDEX IF NOT EXISTS idx_listings_seller_location
ON listings(seller_latitude, seller_longitude)
WHERE seller_latitude IS NOT NULL AND seller_longitude IS NOT NULL;
