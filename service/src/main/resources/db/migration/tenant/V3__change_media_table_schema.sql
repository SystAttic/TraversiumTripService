-- Add the new columns
ALTER TABLE media ADD COLUMN latitude DOUBLE PRECISION;
ALTER TABLE media ADD COLUMN longitude DOUBLE PRECISION;

-- Migrate existing data (H2 + Postgres compatible)
UPDATE media
SET
    latitude = CAST(
            TRIM(SUBSTRING(geo_location FROM 1 FOR POSITION(',' IN geo_location) - 1))
        AS DOUBLE PRECISION
               ),
    longitude = CAST(
            TRIM(SUBSTRING(geo_location FROM POSITION(',' IN geo_location) + 1))
        AS DOUBLE PRECISION
                )
WHERE geo_location IS NOT NULL
  AND geo_location LIKE '%,%';

-- Remove the old column
ALTER TABLE media DROP COLUMN geo_location;