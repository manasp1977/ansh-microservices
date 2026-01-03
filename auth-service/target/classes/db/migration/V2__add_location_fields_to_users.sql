-- Add latitude and longitude columns to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE users ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;
ALTER TABLE users ADD COLUMN IF NOT EXISTS location geography(Point, 4326);

-- Create spatial index on location column for efficient distance queries
CREATE INDEX IF NOT EXISTS idx_users_location ON users USING GIST (location);

-- Function to update location column when latitude/longitude change
CREATE OR REPLACE FUNCTION update_user_location()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.latitude IS NOT NULL AND NEW.longitude IS NOT NULL THEN
        NEW.location = ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326)::geography;
    ELSE
        NEW.location = NULL;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update location column
DROP TRIGGER IF EXISTS trg_update_user_location ON users;
CREATE TRIGGER trg_update_user_location
    BEFORE INSERT OR UPDATE OF latitude, longitude ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_user_location();

-- Update existing users' location column if they have lat/lon
UPDATE users
SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
WHERE latitude IS NOT NULL AND longitude IS NOT NULL AND location IS NULL;
