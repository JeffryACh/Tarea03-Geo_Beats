package com.example.geobeats.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationClient(context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        // Configuración de alta precisión:
        // Intervalo de 5 segundos para ahorrar batería, pero permite ráfagas de 2 segundos.
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.lastOrNull()?.let { location ->
                    // Enviamos la ubicación al flujo (Flow)
                    trySend(location)
                }
            }
        }

        // Iniciamos el rastreo
        client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            .addOnFailureListener { e ->
                Log.e("LocationClient", "Error al solicitar actualizaciones de ubicación: ${e.message}")
                close(e) // Cierra el flujo si hay un error fatal
            }

        // Esta parte es CRUCIAL: Detiene el GPS automáticamente cuando
        // la aplicación se cierra o cambias de pantalla.
        awaitClose {
            client.removeLocationUpdates(locationCallback)
        }
    }
}