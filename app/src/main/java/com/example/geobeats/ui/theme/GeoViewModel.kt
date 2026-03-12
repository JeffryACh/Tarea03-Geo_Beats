package com.example.geobeats.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.geobeats.location.DistanceCalculator
import com.example.geobeats.location.LocationClient
import com.example.geobeats.model.PointOfInterest
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GeoViewModel(application: Application) : AndroidViewModel(application) {

    private val locationClient = LocationClient(application)

    // Usamos un Set para recordar en qué geocercas ya entramos y no repetir la música
    private val triggeredGeofences = mutableSetOf<String>()

    fun startLocationTracking(
        points: List<PointOfInterest>,
        onEnterGeofence: (String) -> Unit
    ) {
        locationClient.getLocationUpdates()
            .catch { e -> e.printStackTrace() } // Captura errores silenciosamente
            .onEach { location ->

                // Evaluamos la distancia contra TODOS los puntos de la lista
                points.forEach { point ->
                    val distance = DistanceCalculator.haversine(
                        location.latitude, location.longitude,
                        point.latitude, point.longitude
                    )

                    // 1. Si entramos al radio y la música de este punto NO había sonado
                    if (distance <= point.triggerRadiusMeters && !triggeredGeofences.contains(point.id)) {

                        triggeredGeofences.add(point.id) // Ponemos el seguro
                        onEnterGeofence(point.spotifyUri) // Disparamos Spotify

                        // 2. Si salimos del radio, quitamos el seguro
                    } else if (distance > point.triggerRadiusMeters && triggeredGeofences.contains(point.id)) {

                        triggeredGeofences.remove(point.id) // Listo para volver a sonar si regresamos

                    }
                }
            }
            .launchIn(viewModelScope)
    }
}