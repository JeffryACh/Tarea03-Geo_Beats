package com.example.geobeats.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.geobeats.location.DistanceCalculator
import com.example.geobeats.location.LocationClient
import com.example.geobeats.model.PointOfInterest
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GeoViewModel(application: Application) : AndroidViewModel(application) {

    private val locationClient = LocationClient(application)
    private var trackingJob: Job? = null

    // Memoria del último punto visitado
    private var lastVisitedPointId: String? = null

    fun startLocationTracking(points: List<PointOfInterest>, onEnterGeofence: (String) -> Unit) {
        // Cancelamos el rastreo anterior si la lista de puntos cambió
        trackingJob?.cancel()

        // CORRECCIÓN 1: Se quitó el "5000L" porque la función no recibe parámetros
        trackingJob = locationClient.getLocationUpdates()
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                var insideAnyGeofence = false

                for (point in points) {
                    // CORRECCIÓN 2: Se cambió "calculateDistance" por "haversine"
                    val distance = DistanceCalculator.haversine(
                        location.latitude, location.longitude,
                        point.latitude, point.longitude
                    )

                    // Validamos si entramos al radio de 50 metros (usamos 50.0 porque haversine devuelve Double)
                    if (distance <= 50.0) {
                        insideAnyGeofence = true

                        // ¿Es un punto nuevo o seguimos en el mismo?
                        if (lastVisitedPointId != point.id) {
                            lastVisitedPointId = point.id
                            // Disparamos la señal hacia la interfaz gráfica (MapScreen)
                            onEnterGeofence(point.spotifyUri)
                        }

                        // Como ya encontramos el punto más cercano, dejamos de iterar la lista
                        break
                    }
                }

                // Mecanismo de "Enfriamiento" (Cooldown)
                // Si el GPS detecta que salimos del radio de TODOS los puntos, borramos la memoria.
                if (!insideAnyGeofence) {
                    lastVisitedPointId = null
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel()
    }
}