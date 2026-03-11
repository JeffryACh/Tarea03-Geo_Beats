package com.example.geobeats.ui.theme

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.geobeats.spotify.SpotifyManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@Composable
fun MapScreen() {
    var hasLocationPermission by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val spotifyManager = remember { SpotifyManager(context) }

    // ¡NUEVO! 🛡️ Protección contra Memory Leaks de Spotify
    // Esto asegura que la conexión se cierre si la app pasa a segundo plano o se destruye la vista
    DisposableEffect(Unit) {
        onDispose {
            spotifyManager.disconnect()
            Log.d("MapScreen", "Limpieza de memoria: Spotify desconectado.")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    if (hasLocationPermission) {
        MapContainer(
            onMapReady = { googleMap ->
                googleMap.uiSettings.isZoomControlsEnabled = true

                try {
                    googleMap.isMyLocationEnabled = true
                } catch (e: SecurityException) {
                    Log.e("MapScreen", "Permisos no concedidos")
                }

                // ¡NUEVO! 🛡️ Limpiar el mapa para evitar sobrecarga de RAM por recomposiciones
                googleMap.clear()

                // Definir tu punto de interés
                val puntoInteres = LatLng(9.8563, -83.9127)

                googleMap.addMarker(
                    MarkerOptions()
                        .position(puntoInteres)
                        .title("Campus TEC")
                        .snippet("Toca aquí para iniciar la banda sonora")
                )

                // Solo movemos la cámara la primera vez, puedes evitar que se mueva constantemente
                // si lo extraes a un LaunchedEffect, pero por ahora esto evitará el cierre abrupto.
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntoInteres, 15f))

                // Evento de clic en el marcador
                googleMap.setOnMarkerClickListener { marker ->
                    Log.d("MapScreen", "Marcador tocado: ${marker.title}")

                    val playlistUri = "spotify:playlist:37i9dQZF1DXcBWIGOYBMm1"

                    spotifyManager.connectAndPlay(
                        playlistUri = playlistUri,
                        onConnected = { Log.d("MapScreen", "¡Música iniciada desde el mapa!") },
                        onError = { error -> Log.e("MapScreen", "Problema al iniciar: $error") }
                    )

                    false
                }
            }
        )
    } else {
        Text("La aplicación requiere acceso a la ubicación para funcionar.")
    }
}