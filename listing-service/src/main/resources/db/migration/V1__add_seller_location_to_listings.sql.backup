-- Add seller location columns to listings table for distance-based filtering
-- These columns store denormalized data from the User service for efficient queries

-- Add latitude and longitude columns
ALTER TABLE listings ADD COLUMN IF NOT EXISTS seller_latitude DOUBLE PRECISION;
ALTER TABLE listings ADD COLUMN IF NOT EXISTS seller_longitude DOUBLE PRECISION;

-- Create index on location columns for better query performance
CREATE INDEX IF NOT EXISTS idx_listings_seller_location
ON listings(seller_latitude, seller_longitude)
WHERE seller_latitude IS NOT NULL AND seller_longitude IS NOT NULL;

-- Note: PostGIS spatial index commented out - enable if PostGIS is installed
-- CREATE EXTENSION IF NOT EXISTS postgis;
-- CREATE INDEX IF NOT EXISTS idx_listings_seller_location_geog
-- ON listings USING GIST(ST_MakePoint(seller_longitude, seller_latitude)::geography)
-- WHERE seller_latitude IS NOT NULL AND seller_longitude IS NOT NULL;
