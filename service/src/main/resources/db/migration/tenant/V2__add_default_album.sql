-- Add default_album column to trip table
ALTER TABLE trip
ADD COLUMN default_album BIGINT;

-- Add foreign key constraint to reference album table
ALTER TABLE trip
ADD CONSTRAINT fk_trip_default_album
FOREIGN KEY (default_album) REFERENCES album(album_id) ON DELETE SET NULL;

-- Add index for better query performance
CREATE INDEX idx_trip_default_album ON trip(default_album);
