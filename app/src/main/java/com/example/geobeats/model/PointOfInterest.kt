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
                )
            )
        }
    }
}