package com.example.geobeats.ui.theme

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext // Importante para el contexto
import com.example.geobeats.spotify.SpotifyManager // Importante para enlazar Spotify
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@Composable
fun MapScreen() {
    var hasLocationPermission by remember { mutableStateOf(false) }

    // 1. Extraemos el contexto correctamente
    val context = LocalContext.current

    // 2. Instanciamos la clase SpotifyManager usando remember para que sobreviva a las recomposiciones de la UI
    val spotifyManager = remember { SpotifyManager(context) }

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

                // Activar el punto azul de ubicación en tiempo real
                try {
                    googleMap.isMyLocationEnabled = true
                } catch (e: SecurityException) {
                    Log.e("MapScreen", "Permisos no concedidos")
                }

                // Definir tu punto de interés (Ej: TEC en Cartago)
                val puntoInteres = LatLng(9.8563, -83.9127)

                googleMap.addMarker(
                    MarkerOptions()
                        .position(puntoInteres)
                        .title("Campus TEC")
                        .snippet("Toca aquí para iniciar la banda sonora")
                )

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntoInteres, 15f))

                // Evento de clic en el marcador
                googleMap.setOnMarkerClickListener { marker ->
                    Log.d("MapScreen", "Marcador tocado: ${marker.title}")

                    val playlistUri = "spotify:playlist:37i9dQZF1DXcBWIGOYBMm1"

                    // 3. Usamos la instancia de spotifyManager y la nueva función connectAndPlay
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