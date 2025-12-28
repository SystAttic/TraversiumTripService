-- Add columns (if not already existing)
ALTER TABLE media ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE media ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

-- Migration logic
UPDATE media
SET
    latitude = CAST(TRIM(SUBSTRING(geo_location FROM 1 FOR POSITION(',' IN geo_location) - 1)) AS DOUBLE PRECISION),
    longitude = CAST(TRIM(SUBSTRING(geo_location FROM POSITION(',' IN geo_location) + 1)) AS DOUBLE PRECISION)
WHERE geo_location IS NOT NULL
  AND geo_location LIKE '%,%'
  AND latitude IS NULL
  AND longitude IS NULL;

-- Drop column (if it exists)
ALTER TABLE media DROP COLUMN IF EXISTS geo_location;