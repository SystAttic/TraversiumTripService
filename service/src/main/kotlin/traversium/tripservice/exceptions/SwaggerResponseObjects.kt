package traversium.tripservice.exceptions

const val NOW = "2026-01-01T08:00:00.00Z"

object SwaggerRequestObjects {

    const val CREATE_TRIP = """
        {
            "title": "Trip example",
            "description": "Example of a new trip.",
            "visibility": "PRIVATE",
            "coverPhotoUrl": "url/of/coverPhoto.jpg"
        }
    """

    const val UPDATE_TRIP = """
        {
            "tripId": 42,
            "title": "Trip update example",
            "description": "Example of an updated trip.",
            "visibility": "PUBLIC",
            "coverPhotoUrl": "url/of/newCoverPhoto.jpg"
        }
    """

    const val ADD_ALBUM = """
        {
            "title": "New album example",
            "description": "Example of a new album."
        }
    """

    const val UPDATE_ALBUM = """
        {
            "title": "Album example with new title",
            "description": "Example of an updated album."
        }
    """

    const val AUTOSORT_TRIP = """
        {   
            "title": "Sortable trip example",
            "description": "Example of a trip with media to be sorted.",
            "visibility": "PUBLIC",
            "coverPhotoUrl": "url/of/coverPhoto.jpg",
            "defaultAlbum": 1,
            "albums": [
                {
                  "albumId": 1,
                  "title": "Default Album",
                  "description": "Media awaiting processing or missing metadata.",
                  "media": [
                    {
                      "pathUrl": "media/raw-upload-001.jpg",
                      "uploader": "user_123",
                      "fileType": "image",
                      "fileFormat": "jpg",
                      "fileSize": 2048576,
                      "createdAt": "2024-06-05T10:00:00Z",
                      "geoLocation": { "latitude": 41.8902, "longitude": 12.4922 } 
                    },
                    {
                      "pathUrl": "media/no-metadata-found.jpg",
                      "uploader": "user_123",
                      "fileType": "image",
                      "fileFormat": "jpg",
                      "fileSize": 1524000,
                      "createdAt": "1970-01-01T00:00:00Z",
                      "geoLocation": { "latitude": 0.0, "longitude": 0.0 }
                    },
                    {
                      "pathUrl": "media/rome-colosseum-3.jpg",
                      "uploader": "user_123",
                      "fileType": "image",
                      "fileFormat": "jpg",
                      "fileSize": 3600000,
                      "createdAt": "2024-06-01T14:26:00Z",
                      "geoLocation": { "latitude": 41.8911, "longitude": 12.4924 }
                    }
                  ]
                },
                {
                  "title": "Rome: The Colosseum",
                  "description": "Correctly sorted photos from the Roman Forum and Colosseum.",
                  "media": [
                    {
                      "pathUrl": "media/rome-colosseum-1.jpg",
                      "uploader": "user_123",
                      "fileType": "image",
                      "fileFormat": "jpg",
                      "fileSize": 3500000,
                      "createdAt": "2024-06-01T14:22:00Z",
                      "geoLocation": { "latitude": 41.8902, "longitude": 12.4922 }
                    },
                    {
                      "pathUrl": "media/rome-arch-of-constantine.jpg",
                      "uploader": "user_123",
                      "fileType": "image",
                      "fileFormat": "jpg",
                      "fileSize": 4100000,
                      "createdAt": "2024-06-01T15:10:00Z",
                      "geoLocation": { "latitude": 41.8928, "longitude": 12.4905 }
                    }
                  ]
                },
                {
                  "title": "Venice Canals",
                  "description": "Photos from the Venice gondola ride and two accidentally added photos from Pisa and Rome.",
                  "media": [
                    {
                      "pathUrl": "media/venice-gondola.jpg",
                      "uploader": "user_123",
                      "fileType": "image",
                      "fileFormat": "jpg",
                      "fileSize": 2800000,
                      "createdAt": "2024-06-10T18:00:00Z",
                      "geoLocation": { "latitude": 45.4344, "longitude": 12.3394 }
                    },
                    {
                      "pathUrl": "media/venice-rialto-bridge.jpg",
                      "uploader": "user_123",
                      "fileType": "image",
                      "fileFormat": "jpg",
                      "fileSize": 3100000,
                      "createdAt": "2024-06-10T19:30:00Z",
                      "geoLocation": { "latitude": 45.4380, "longitude": 12.3359 }
                    },
                    {
                      "pathUrl": "media/pisa-leaning-tower.jpg",
                      "uploader": "user_123",
                      "fileType": "image",
                      "fileFormat": "jpg",
                      "fileSize": 2950000,
                      "createdAt": "2024-06-07T12:00:00Z",
                      "geoLocation": { "latitude": 43.7230, "longitude": 10.3966 }
                    },
                    {
                      "pathUrl": "media/rome-colosseum-2.jpg",
                      "uploader": "user_123",
                      "fileType": "image",
                      "fileFormat": "jpg",
                      "fileSize": 3500000,
                      "createdAt": "2024-06-01T14:24:00Z",
                      "geoLocation": { "latitude": 41.8909, "longitude": 12.4923 }
                    }
                  ]
                }
            ]
        }
    """

    const val ADD_MEDIA = """
        [{
          "pathUrl": "path/to/new/media.jpg",
          "uploader": "user_123",
          "fileType": "image",
          "fileFormat": "jpg",
          "fileSize": 240000,
          "createdAt": "$NOW",
          "geoLocation": { "latitude": 15.8072, "longitude": 23.9123 } 
        },
        {
          "pathUrl": "path/to/new/media2.jpg",
          "uploader": "user_123",
          "fileType": "image",
          "fileFormat": "jpg",
          "fileSize": 250000,
          "createdAt": "$NOW",
          "geoLocation": { "latitude": 15.9112, "longitude": 22.1325 } 
        }]
    """


}

object SwaggerResponseObjects {

// ------------------------------------
//      Trip
// ------------------------------------

    const val GET_ALL_TRIPS = """
        [
          {
            "tripId": 1,
            "title": "Trip example",
            "description": "Example of a new trip.",
            "ownerId": "user_345",
            "visibility": "PRIVATE",
            "coverPhotoUrl": "url/of/coverPhoto.jpg",
            "collaborators": [
              "user_345",
              "user_999"
            ],
            "viewers": [],
            "defaultAlbum": 1,
            "albums": [
              {
                "albumId": 1,
                "title": "Default moment",
                "description": "",
                "media": [],
                "createdAt": "$NOW"
              },
              {
                "albumId": 3,
                "title": "Album example",
                "description": "Example of a new album.",
                "media": [
                  {
                    "mediaId": 1,
                    "pathUrl": "path/to/new/media.jpg",
                    "uploader": "user_999",
                    "fileType": "image",
                    "fileFormat": "jpg",
                    "fileSize": 240000,
                    "geoLocation": {
                      "latitude": 15.8072,
                      "longitude": 23.9123
                    },
                    "createdAt": "$NOW",
                    "uploadedAt": "$NOW"
                  }
                ],
                "createdAt": "$NOW"
              }
            ],
            "createdAt": "$NOW"
          },
          {
            "tripId": 2,
            "title": "Trip example 2",
            "description": "Another example of a new trip.",
            "ownerId": "user_123",
            "visibility": "PUBLIC",
            "coverPhotoUrl": "url/of/coverPhoto2.jpg",
            "collaborators": [
              "user_123",
              "user_345"
            ],
            "viewers": [],
            "defaultAlbum": 2,
            "albums": [
              {
                "albumId": 2,
                "title": "Default moment",
                "description": "",
                "media": [],
                "createdAt": "$NOW"
              },
              {
                "albumId": 4,
                "title": "Album example 2",
                "description": "Example 2 of a new album.",
                "media": [
                  {
                    "mediaId": 3,
                    "pathUrl": "path/to/new/media.jpg",
                    "uploader": "user_123",
                    "fileType": "image",
                    "fileFormat": "jpg",
                    "fileSize": 240000,
                    "geoLocation": {
                      "latitude": 15.8072,
                      "longitude": 23.9123
                    },
                    "createdAt": "$NOW",
                    "uploadedAt": "$NOW"
                  }
                ],
                "createdAt": "$NOW"
              }
            ],
            "createdAt": "$NOW"
          }
        ]
    """

    const val GET_TRIP_BY_ID = """
        {
          "tripId": 1,
          "title": "Trip example",
          "description": "Example of a trip.",
          "ownerId": "user123",
          "visibility": "PRIVATE",
          "coverPhotoUrl": "url/of/coverPhoto.jpg",
          "collaborators": ["user123"],
          "viewers": [],
          "defaultAlbum": 1,
          "albums": [
            {
              "albumId": 1,
              "title": "Default moment",
              "description": "",
              "media": [
                {
                    "mediaId": 1,
                    "pathUrl": "path/to/media.jpg",
                    "uploader": "user_123",
                    "fileType": "image",
                    "fileFormat": "jpg",
                    "fileSize": 240000,
                    "geoLocation": {
                      "latitude": 15.8072,
                      "longitude": 23.9123
                    },
                    "createdAt": "$NOW",
                    "uploadedAt": "$NOW"
                }
              ],
              "createdAt": "$NOW"
            }
          ],
          "createdAt": "$NOW"
        }
    """

    const val GET_TRIP_BY_OWNER = """
        {
          "tripId": 1,
          "title": "Trip by owner example",
          "description": "Example of a trip searched by owner.",
          "ownerId": "user123",
          "visibility": "PRIVATE",
          "coverPhotoUrl": "url/of/coverPhoto.jpg",
          "collaborators": ["user123"],
          "viewers": [],
          "defaultAlbum": 1,
          "albums": [
            {
              "albumId": 1,
              "title": "Default moment",
              "description": "",
              "media": [
                {
                    "mediaId": 1,
                    "pathUrl": "path/to/media.jpg",
                    "uploader": "user_123",
                    "fileType": "image",
                    "fileFormat": "jpg",
                    "fileSize": 240000,
                    "geoLocation": {
                      "latitude": 15.8072,
                      "longitude": 23.9123
                    },
                    "createdAt": "$NOW",
                    "uploadedAt": "$NOW"
                }
              ],
              "createdAt": "$NOW"
            }
          ],
          "createdAt": "$NOW"
        }
    """

    const val CREATED_TRIP = """
        {
          "tripId": 1,
          "title": "Trip example",
          "description": "Example of a new trip.",
          "ownerId": "user123",
          "visibility": "PRIVATE",
          "coverPhotoUrl": "url/of/coverPhoto.jpg",
          "collaborators": ["user123"],
          "viewers": [],
          "defaultAlbum": 1,
          "albums": [
            {
              "albumId": 1,
              "title": "Default moment",
              "description": "",
              "media": [],
              "createdAt": "$NOW"
            }
          ],
          "createdAt": "$NOW"
        }
    """

    const val UPDATED_TRIP = """
        {
          "tripId": 1,
          "title": "Updated example",
          "description": "Example of an updated trip.",
          "ownerId": "user123",
          "visibility": "PUBLIC",
          "coverPhotoUrl": "url/of/newCoverPhoto.jpg",
          "collaborators": ["user123"],
          "viewers": [],
          "defaultAlbum": 1,
          "albums": [
            {
              "albumId": 1,
              "title": "Default moment",
              "description": "",
              "media": [],
              "createdAt": "$NOW"
            }
          ],
          "createdAt": "$NOW"
        }
    """

    const val FOUND_TRIP_BY_TITLE = """
        {
          "tripId": 1,
          "title": "Searched trip",
          "description": "Example of a searched trip.",
          "ownerId": "user123",
          "visibility": "PUBLIC",
          "coverPhotoUrl": "url/of/coverPhoto.jpg",
          "collaborators": ["user123"],
          "viewers": [],
          "defaultAlbum": 1,
          "albums": [
            {
              "albumId": 1,
              "title": "Default moment",
              "description": "",
              "media": [],
              "createdAt": "$NOW"
            }
          ],
          "createdAt": "$NOW"
        }
    """

    const val AUTOSORTED_TRIP = """
        {
          "tripId": null,
          "title": "Sorted trip example",
          "description": "Example of a trip with autosorted media.",
          "ownerId": null,
          "visibility": "PUBLIC",
          "coverPhotoUrl": "url/of/coverPhoto.jpg",
          "collaborators": [],
          "viewers": [],
          "defaultAlbum": 1,
          "albums": [
            {
              "albumId": 1,
              "title": "Default Album",
              "description": "Media awaiting processing or missing metadata.",
              "media": [
                {
                  "mediaId": null,
                  "pathUrl": "media/no-metadata-found.jpg",
                  "uploader": "user_123",
                  "fileType": "image",
                  "fileFormat": "jpg",
                  "fileSize": 1524000,
                  "geoLocation": {
                    "latitude": 0,
                    "longitude": 0
                  },
                  "createdAt": "1970-01-01T00:00:00Z",
                  "uploadedAt": null
                },
                {
                  "mediaId": null,
                  "pathUrl": "media/raw-upload-001.jpg",
                  "uploader": "user_123",
                  "fileType": "image",
                  "fileFormat": "jpg",
                  "fileSize": 2048576,
                  "geoLocation": {
                    "latitude": 41.8902,
                    "longitude": 12.4922
                  },
                  "createdAt": "2024-06-05T10:00:00Z",
                  "uploadedAt": null
                }
              ],
              "createdAt": null
            },
            {
              "albumId": null,
              "title": "Rome: The Colosseum",
              "description": "Correctly sorted photos from the Roman Forum and Colosseum.",
              "media": [
                {
                  "mediaId": null,
                  "pathUrl": "media/rome-colosseum-1.jpg",
                  "uploader": "user_123",
                  "fileType": "image",
                  "fileFormat": "jpg",
                  "fileSize": 3500000,
                  "geoLocation": {
                    "latitude": 41.8902,
                    "longitude": 12.4922
                  },
                  "createdAt": "2024-06-01T14:22:00Z",
                  "uploadedAt": null
                },
                {
                  "mediaId": null,
                  "pathUrl": "media/rome-colosseum-2.jpg",
                  "uploader": "user_123",
                  "fileType": "image",
                  "fileFormat": "jpg",
                  "fileSize": 3500000,
                  "geoLocation": {
                    "latitude": 41.8909,
                    "longitude": 12.4923
                  },
                  "createdAt": "2024-06-01T14:24:00Z",
                  "uploadedAt": null
                },
                {
                  "mediaId": null,
                  "pathUrl": "media/rome-colosseum-3.jpg",
                  "uploader": "user_123",
                  "fileType": "image",
                  "fileFormat": "jpg",
                  "fileSize": 3600000,
                  "geoLocation": {
                    "latitude": 41.8911,
                    "longitude": 12.4924
                  },
                  "createdAt": "2024-06-01T14:26:00Z",
                  "uploadedAt": null
                },
                {
                  "mediaId": null,
                  "pathUrl": "media/rome-arch-of-constantine.jpg",
                  "uploader": "user_123",
                  "fileType": "image",
                  "fileFormat": "jpg",
                  "fileSize": 4100000,
                  "geoLocation": {
                    "latitude": 41.8928,
                    "longitude": 12.4905
                  },
                  "createdAt": "2024-06-01T15:10:00Z",
                  "uploadedAt": null
                }
              ],
              "createdAt": null
            },
            {
              "albumId": null,
              "title": "Location (45.4362, 12.3377) 2024-06-10",
              "description": null,
              "media": [
                {
                  "mediaId": null,
                  "pathUrl": "media/venice-gondola.jpg",
                  "uploader": "user_123",
                  "fileType": "image",
                  "fileFormat": "jpg",
                  "fileSize": 2800000,
                  "geoLocation": {
                    "latitude": 45.4344,
                    "longitude": 12.3394
                  },
                  "createdAt": "2024-06-10T18:00:00Z",
                  "uploadedAt": null
                },
                {
                  "mediaId": null,
                  "pathUrl": "media/venice-rialto-bridge.jpg",
                  "uploader": "user_123",
                  "fileType": "image",
                  "fileFormat": "jpg",
                  "fileSize": 3100000,
                  "geoLocation": {
                    "latitude": 45.438,
                    "longitude": 12.3359
                  },
                  "createdAt": "2024-06-10T19:30:00Z",
                  "uploadedAt": null
                }
              ],
              "createdAt": null
            },
            {
              "albumId": null,
              "title": "Location (43.7230, 10.3966) 2024-06-07",
              "description": null,
              "media": [
                {
                  "mediaId": null,
                  "pathUrl": "media/pisa-leaning-tower.jpg",
                  "uploader": "user_123",
                  "fileType": "image",
                  "fileFormat": "jpg",
                  "fileSize": 2950000,
                  "geoLocation": {
                    "latitude": 43.723,
                    "longitude": 10.3966
                  },
                  "createdAt": "2024-06-07T12:00:00Z",
                  "uploadedAt": null
                }
              ],
              "createdAt": null
            }
          ],
          "createdAt": null
        }
    """

// ------------------------------------
//      Collaborator
// ------------------------------------


    const val GET_TRIPS_BY_COLLABORATOR = """
        {
          "tripId": 1,
          "title": "Trip example with collaborator",
          "description": "Example of a trip with collaborator.",
          "ownerId": "user123",
          "visibility": "PRIVATE",
          "coverPhotoUrl": "url/of/coverPhoto.jpg",
          "collaborators": ["user123", "collaborator"],
          "viewers": [],
          "defaultAlbum": 1,
          "albums": [
            {
              "albumId": 1,
              "title": "Default moment",
              "description": "",
              "media": [
                {
                    "mediaId": 1,
                    "pathUrl": "path/to/media.jpg",
                    "uploader": "collaborator",
                    "fileType": "image",
                    "fileFormat": "jpg",
                    "fileSize": 240000,
                    "geoLocation": {
                      "latitude": 15.8072,
                      "longitude": 23.9123
                    },
                    "createdAt": "$NOW",
                    "uploadedAt": "$NOW"
                }
              ],
              "createdAt": "$NOW"
            }
          ],
          "createdAt": "$NOW"
        }
    """

    const val ADDED_COLLABORATOR = """
        {
          "tripId": 1,
          "title": "Trip example with new collaborator",
          "description": "Example of a trip with new collaborator.",
          "ownerId": "user123",
          "visibility": "PRIVATE",
          "coverPhotoUrl": "url/of/coverPhoto.jpg",
          "collaborators": ["user123", "collaborator"],
          "viewers": [],
          "defaultAlbum": 1,
          "albums": [
            {
              "albumId": 1,
              "title": "Default moment",
              "description": "",
              "media": [],
              "createdAt": "$NOW"
            }
          ],
          "createdAt": "$NOW"
        }
    """

    // If needed to return Trip without collaborator
    // const val REMOVE_COLLABORATOR = """"""

// ------------------------------------
//      Viewer
// ------------------------------------

    const val GET_TRIPS_BY_VIEWER = """
        {
          "tripId": 1,
          "title": "Trip example with viewer",
          "description": "Example of a trip with viewer.",
          "ownerId": "user123",
          "visibility": "PRIVATE",
          "coverPhotoUrl": "url/of/coverPhoto.jpg",
          "collaborators": ["user123"],
          "viewers": ["viewer"],
          "defaultAlbum": 1,
          "albums": [
            {
              "albumId": 1,
              "title": "Default moment",
              "description": "",
              "media": [],
              "createdAt": "$NOW"
            }
          ],
          "createdAt": "$NOW"
        }
    """

    const val ADDED_VIEWER = """
        {
          "tripId": 1,
          "title": "Trip example with new viewer",
          "description": "Example of a trip with new viewer.",
          "ownerId": "user123",
          "visibility": "PRIVATE",
          "coverPhotoUrl": "url/of/coverPhoto.jpg",
          "collaborators": ["user123"],
          "viewers": ["viewer"],
          "defaultAlbum": 1,
          "albums": [
            {
              "albumId": 1,
              "title": "Default moment",
              "description": "",
              "media": [],
              "createdAt": "$NOW"
            }
          ],
          "createdAt": "$NOW"
        }
    """

    // If needed to return Trip without viewer
    //const val REMOVE_VIEWER = """"""

// ------------------------------------
//      Album
// ------------------------------------

    const val GET_ALBUM_FROM_TRIP = """
        {
          "albumId": 15,
          "title": "Some album",
          "description": "This is some album",
          "media": [],
          "createdAt": "$NOW"
        }
    """

    const val ADDED_ALBUM_TO_TRIP = """
        {
          "tripId": 1,
          "title": "Trip example",
          "description": "Example of a trip.",
          "ownerId": "user_123",
          "visibility": "PRIVATE",
          "coverPhotoUrl": "url/of/coverPhoto.jpg",
          "collaborators": [
            "user_123"
          ],
          "viewers": [],
          "defaultAlbum": 1,
          "albums": [
            {
              "albumId": 1,
              "title": "Default moment",
              "description": "The default moment",
              "media": [],
              "createdAt": "$NOW"
            },
            {
              "albumId": 2,
              "title": "New album example",
              "description": "Example of a new album.",
              "media": [],
              "createdAt": "$NOW"
            }
          ],
          "createdAt": "$NOW"
        }
    """

    const val GET_ALL_ALBUMS = """
        [
            {   
                "albumId": 1,
                "title": "Default moment",
                "description": "A default moment",
                "media": [],
                "createdAt": "2025-12-30T09:11:47.504995Z"
            },
            {
                "albumId": 2,
                "title": "Album example",
                "description": "Example of a new album.",
                "media": [
                  {
                    "mediaId": 1,
                    "pathUrl": "path/to/new/media.jpg",
                    "uploader": "user_123",
                    "fileType": "image",
                    "fileFormat": "jpg",
                    "fileSize": 240000,
                    "geoLocation": {
                      "latitude": 15.8072,
                      "longitude": 23.9123
                    },
                    "createdAt": "$NOW",
                    "uploadedAt": "$NOW"
                  }
                ],
                "createdAt": "$NOW"
            }
        ]
    """

    const val GET_ALBUM_BY_ID = """
        {
            "albumId": 15,
            "title": "Album example",
            "description": "Example of a new album.",
            "media": [
              {
                "mediaId": 1,
                "pathUrl": "path/to/new/media.jpg",
                "uploader": "user_123",
                "fileType": "image",
                "fileFormat": "jpg",
                "fileSize": 240000,
                "geoLocation": {
                  "latitude": 15.8072,
                  "longitude": 23.9123
                },
                "createdAt": "$NOW",
                "uploadedAt": "$NOW"
              }
            ],
            "createdAt": "$NOW"
        }
    """

    const val UPDATED_ALBUM = """
        {
          "albumId": 32,
          "title": "Updated album example",
          "description": "Example of an updated album.",
          "media": [],
          "createdAt": "$NOW"
        }
    """

// ------------------------------------
//      Media
// ------------------------------------

    const val GET_ALL_MEDIA_FROM_TRIP = """
       [
          "path/to/media1.jpg"
          "path/to/media2.jpg",
          "path/to/media3.jpg",
        ] 
    """

    const val GET_MEDIA_FROM_ALBUM = """
        {
          "mediaId": 42,
          "pathUrl": "path/to/media.jpg",
          "uploader": "user_123",
          "fileType": "image",
          "fileFormat": "jpg",
          "fileSize": 240000,
          "geoLocation": {
            "latitude": 23.2235,
            "longitude": 12.6731
          },
          "createdAt": "$NOW",
          "uploadedAt": "$NOW"
        }
    """

    const val ADDED_MEDIA = """
        {
          "albumId": 2,
          "title": "Album with new media",
          "description": "",
          "media": [
            {
                "pathUrl": "path/to/new/media.jpg",
                "uploader": "user_123",
                "fileType": "image",
                "fileFormat": "jpg",
                "fileSize": 240000,
                "geoLocation": {
                  "latitude": 15.8072,
                  "longitude": 23.9123
                }
                "createdAt": "2025-01-01T08:00:00.00Z",
                "uploadedAt": "$NOW"
            },
            {
                "pathUrl": "path/to/new/media2.jpg",
                "uploader": "user_123",
                "fileType": "image",
                "fileFormat": "jpg",
                "fileSize": 250000,
                "geoLocation": {
                  "latitude": 15.9112,
                  "longitude": 22.1325
                }
                "createdAt": "2025-01-01T08:00:00.00Z",
                "uploadedAt": "$NOW"
            }
          ],
          "createdAt": "$NOW"
        }
    """

    const val GET_ALL_MEDIA = """
        [
            {
                "pathUrl": "path/to/new/media.jpg",
                "uploader": "user_123",
                "fileType": "image",
                "fileFormat": "jpg",
                "fileSize": 240000,
                "geoLocation": {
                  "latitude": 15.8072,
                  "longitude": 23.9123
                }
                "createdAt": "2025-01-01T08:00:00.00Z",
                "uploadedAt": "$NOW"
            },
            {
                "pathUrl": "path/to/new/media2.jpg",
                "uploader": "user_123",
                "fileType": "image",
                "fileFormat": "jpg",
                "fileSize": 250000,
                "geoLocation": {
                  "latitude": 15.9112,
                  "longitude": 22.1325
                }
                "createdAt": "2025-01-01T08:00:00.00Z",
                "uploadedAt": "$NOW"
            }
        ]
    """

    const val GET_MEDIA_BY_ID = """
        {
          "mediaId": 42,
          "pathUrl": "path/to/mediaWithID.jpg",
          "uploader": "user_123",
          "fileType": "image",
          "fileFormat": "jpg",
          "fileSize": 240000,
          "geoLocation": {
            "latitude": 23.2235,
            "longitude": 12.6731
          },
          "createdAt": "$NOW",
          "uploadedAt": "$NOW"
        }
    """

    const val GET_MEDIA_BY_PATH = """
        {
          "mediaId": 16,
          "pathUrl": "path/to/mediaWithPath.jpg",
          "uploader": "user_123",
          "fileType": "image",
          "fileFormat": "jpg",
          "fileSize": 240000,
          "geoLocation": {
            "latitude": 23.2235,
            "longitude": 12.6731
          },
          "createdAt": "$NOW",
          "uploadedAt": "$NOW"
        }
    """

    const val GET_MEDIA_BY_UPLOADER = """
        {
          "mediaId": 99,
          "pathUrl": "path/to/mediaByUploader.jpg",
          "uploader": "user_999",
          "fileType": "image",
          "fileFormat": "jpg",
          "fileSize": 240000,
          "geoLocation": {
            "latitude": 23.2235,
            "longitude": 12.6731
          },
          "createdAt": "$NOW",
          "uploadedAt": "$NOW"
        }
    """

}

object SwaggerErrorObjects {

    const val INTERNAL_SERVER_ERROR = """
    {
      "message": "Internal Server Error",
      "status": 500,
      "timestamp": "$NOW",
      "path": "/rest/v1/somePath"
    }
    """

    const val BAD_REQUEST_ERROR = """
    {
      "message": "Invalid request body format",
      "status": 400,
      "timestamp": "$NOW",
      "path": "/rest/v1/somePath"
    }
    """

    const val NOT_FOUND_ERROR = """
    {
      "message": "Not found",
      "status": 404,
      "timestamp": "$NOW",
      "path": "/rest/v1/somePath"
    }
    """

    const val FORBIDDEN_ERROR = """
    {
      "message": "Access denied",
      "status": 403,
      "timestamp": "$NOW",
      "path": "/rest/v1/somePath"
    }
    """

    const val CONFLICT_ERROR = """
    {
      "message": "Conflict",
      "status": 409,
      "timestamp": "$NOW",
      "path": "/rest/v1/somePath"
    }
    """
}