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

    // 🛡️ Protección contra Memory Leaks
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

                // 🛡️ Limpiar el mapa antes de dibujar
                googleMap.clear()

                // Definir tu punto de interés
                val puntoInteres = LatLng(9.8563, -83.9127)
                val playlistUri = "spotify:playlist:37i9dQZF1DXcBWIGOYBMm1"

                googleMap.addMarker(
                    MarkerOptions()
                        .position(puntoInteres)
                        .title("Campus TEC")
                )

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntoInteres, 15f))

                // Evento DIRECTO: Comportamiento al tocar el PIN ROJO
                googleMap.setOnMarkerClickListener { marker ->
                    Log.d("MapScreen", "Pin rojo tocado: ${marker.title}")

                    // Disparamos la música de una vez
                    spotifyManager.connectAndPlay(
                        playlistUri = playlistUri,
                        onConnected = { Log.d("MapScreen", "¡Música iniciada desde el pin!") },
                        onError = { error -> Log.e("MapScreen", "Problema al iniciar: $error") }
                    )

                    // Al retornar 'true' le decimos al mapa:
                    // "Ya me encargué del clic, no muestres el cuadro blanco"
                    true
                }
            }
        )
    } else {
        Text("La aplicación requiere acceso a la ubicación para funcionar.")
    }
}