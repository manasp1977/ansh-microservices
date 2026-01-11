-- Add latitude and longitude columns to users table
-- Using simple DOUBLE PRECISION columns for latitude/longitude
-- Distance calculations will be done in the application layer

ALTER TABLE users ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE users ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

-- Create index on location columns for efficient queries
CREATE INDEX IF NOT EXISTS idx_users_location ON users(latitude, longitude)
WHERE latitude IS NOT NULL AND longitude IS NOT NULL;

-- Note: For production, consider using PostGIS for more efficient spatial queries
-- To enable PostGIS in the future:
-- 1. Use postgis/postgis Docker image instead of postgres:alpine
-- 2. Add: CREATE EXTENSION IF NOT EXISTS postgis;
-- 3. Add geography column: ALTER TABLE users ADD COLUMN location geography(Point, 4326);
-- 4. Create spatial index: CREATE INDEX idx_users_location ON users USING GIST (location);
