package com.example.geobeats.model

data class PointOfInterest(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val spotifyUri: String,
    val triggerRadiusMeters: Double = 50.0 // Radio en metros para que suene la música
) {
    // El companion object actúa como un proveedor de datos estáticos
    companion object {
        fun obtenerLugares(): List<PointOfInterest> {
            return listOf(
                PointOfInterest(
                    id = "tec_cartago",
                    name = "Campus TEC",
                    latitude = 9.8563,
                    longitude = -83.9127,
                    spotifyUri = "spotify:playlist:37i9dQZF1DXcBWIGOYBMm1",
                    triggerRadiusMeters = 50.0
                ),
                PointOfInterest(
                    id = "basilica_cartago",
                    name = "Basílica de los Ángeles",
                    latitude = 9.864444,
                    longitude = -83.912778,
                    // Playlist de música clásica / épica
                    spotifyUri = "spotify:playlist:37i9dQZF1DWWEJlAGA9gs0",
                    triggerRadiusMeters = 60.0
                ),
                PointOfInterest(
                    id = "paseo_metropoli",
                    name = "Paseo Metrópoli",
                    latitude = 9.8596,
                    longitude = -83.9355,
                    // Playlist de éxitos urbanos/pop para tiendas
                    spotifyUri = "spotify:playlist:37i9dQZF1DX10zKzsJ2jva",
                    triggerRadiusMeters = 50.0
                ),
                PointOfInterest(
                    id = "casa turrialba",
                    name = "casa turrialba",
                    latitude = 9.907827,
                    longitude = -83.677046,
                    // Playlist de linkin park
                    spotifyUri = "spotify:playlist:37i9dQZF1DZ06evO47cwRq",
                    triggerRadiusMeters = 50.0
                ),
                PointOfInterest(
                    id = "b6",
                    name = "b6",
                    latitude = 9.856686,
                    longitude = -83.911992,
                    // Playlist de the weekend
                    spotifyUri = "spotify:playlist:2c4NRJdLK7kgZ0tCVcwgOI",
                    triggerRadiusMeters = 50.0
                ),
                PointOfInterest(
                    id = "b3",
                    name = "b3",
                    latitude = 9.856425,
                    longitude = -83.912592,
                    // Playlist de linkin park
                    spotifyUri = "spotify:playlist:37i9dQZF1DZ06evO1SVXaM",
                    triggerRadiusMeters = 50.0
                )
            )
        }
    }
}