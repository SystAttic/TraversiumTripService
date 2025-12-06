-- Create trip table
CREATE TABLE trip (
    trip_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id VARCHAR(255) NOT NULL,
    visibility INTEGER NOT NULL,
    cover_photo_url VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Create album table
CREATE TABLE album (
    album_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Create media table
CREATE TABLE media (
    media_id BIGSERIAL PRIMARY KEY,
    path_url VARCHAR(512),
    uploader VARCHAR(255),
    file_type VARCHAR(50),
    file_format VARCHAR(50),
    file_size BIGINT,
    geo_location VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Create trip_albums join table (Trip to Album relationship)
CREATE TABLE trip_albums (
    trip_id BIGINT NOT NULL,
    album_id BIGINT NOT NULL,
    PRIMARY KEY (trip_id, album_id),
    CONSTRAINT fk_trip_albums_trip FOREIGN KEY (trip_id) REFERENCES trip(trip_id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_albums_album FOREIGN KEY (album_id) REFERENCES album(album_id) ON DELETE CASCADE
);

-- Create album_media join table (Album to Media relationship)
CREATE TABLE album_media (
    album_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    PRIMARY KEY (album_id, media_id),
    CONSTRAINT fk_album_media_album FOREIGN KEY (album_id) REFERENCES album(album_id) ON DELETE CASCADE,
    CONSTRAINT fk_album_media_media FOREIGN KEY (media_id) REFERENCES media(media_id) ON DELETE CASCADE
);

-- Create trip_collaborators table (ElementCollection for collaborators)
CREATE TABLE trip_collaborators (
    trip_id BIGINT NOT NULL,
    collaborator_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (trip_id, collaborator_id),
    CONSTRAINT fk_trip_collaborators_trip FOREIGN KEY (trip_id) REFERENCES trip(trip_id) ON DELETE CASCADE
);

-- Create trip_viewers table (ElementCollection for viewers)
CREATE TABLE trip_viewers (
    trip_id BIGINT NOT NULL,
    viewer_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (trip_id, viewer_id),
    CONSTRAINT fk_trip_viewers_trip FOREIGN KEY (trip_id) REFERENCES trip(trip_id) ON DELETE CASCADE
);

-- Create indexes for better query performance

-- Trip table indexes
CREATE INDEX idx_trip_owner_id ON trip(owner_id);
CREATE INDEX idx_trip_visibility ON trip(visibility);
CREATE INDEX idx_trip_created_at ON trip(created_at);

-- Album table indexes
CREATE INDEX idx_album_created_at ON album(created_at);

-- Media table indexes
CREATE INDEX idx_media_uploader ON media(uploader);
CREATE INDEX idx_media_file_type ON media(file_type);
CREATE INDEX idx_media_created_at ON media(created_at);

-- Join table indexes (as specified in entity annotations)
CREATE INDEX idx_trip_albums_trip ON trip_albums(trip_id);
CREATE INDEX idx_trip_albums_album ON trip_albums(album_id);

CREATE INDEX idx_album_media_albums ON album_media(album_id);
CREATE INDEX idx_album_media_media ON album_media(media_id);

CREATE INDEX idx_trip_collaborators_trip ON trip_collaborators(trip_id);
CREATE INDEX idx_trip_collaborators_user ON trip_collaborators(collaborator_id);

CREATE INDEX idx_trip_viewers_trip ON trip_viewers(trip_id);
CREATE INDEX idx_trip_viewers_user ON trip_viewers(viewer_id);
