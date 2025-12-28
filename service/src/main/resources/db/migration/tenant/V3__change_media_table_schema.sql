-- Add the new columns (using DOUBLE PRECISION for coordinates)
ALTER TABLE media ADD COLUMN latitude DOUBLE PRECISION;
ALTER TABLE media ADD COLUMN longitude DOUBLE PRECISION;

-- Migrate existing data
-- This splits the string by the comma, removes whitespace, and casts to double
UPDATE media
SET
    latitude = CAST(split_part(geo_location, ',', 1) AS DOUBLE PRECISION),
    longitude = CAST(split_part(geo_location, ',', 2) AS DOUBLE PRECISION)
WHERE geo_location IS NOT NULL AND geo_location LIKE '%,%';

-- Remove the old column
ALTER TABLE media DROP COLUMN geo_location;