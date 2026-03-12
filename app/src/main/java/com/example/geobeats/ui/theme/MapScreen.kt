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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geobeats.model.PointOfInterest
import com.example.geobeats.spotify.SpotifyManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@Composable
fun MapScreen(
    geoViewModel: GeoViewModel = viewModel()
) {
    var hasLocationPermission by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val spotifyManager = remember { SpotifyManager(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // 1. Cargamos el catálogo completo de lugares
    val pointsOfInterest = remember { PointOfInterest.obtenerLugares() }

    DisposableEffect(Unit) {
        onDispose {
            spotifyManager.disconnect()
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

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            // 2. Le enviamos la LISTA al ViewModel (esto arregla el error)
            geoViewModel.startLocationTracking(pointsOfInterest) { playlistUri ->
                spotifyManager.connectAndPlay(
                    playlistUri = playlistUri,
                    onConnected = { Log.d("MapScreen", "Música iniciada por Geofencing") },
                    onError = { error -> Log.e("MapScreen", "Error: $error") }
                )
            }
        }
    }

    if (hasLocationPermission) {
        MapContainer(
            onMapReady = { googleMap ->
                googleMap.uiSettings.isZoomControlsEnabled = true
                googleMap.clear()

                try {
                    googleMap.isMyLocationEnabled = true

                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val userLatLng = LatLng(location.latitude, location.longitude)
                            // Hacemos el zoom un poco más lejos (14f) para que se vean varios puntos
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e("MapScreen", "Permisos denegados")
                }

                // 3. Dibujamos TODOS los marcadores usando un ciclo forEach
                pointsOfInterest.forEach { point ->
                    val positionLatLng = LatLng(point.latitude, point.longitude)

                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(positionLatLng)
                            .title(point.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )

                    // Guardamos la URI de Spotify "escondida" dentro del marcador (tag)
                    // para saber qué canción poner cuando lo toquen
                    marker?.tag = point.spotifyUri
                }

                // 4. El evento de toque ahora es dinámico y sirve para cualquier marcador
                googleMap.setOnMarkerClickListener { marker ->
                    val playlistUri = marker.tag as? String

                    if (playlistUri != null) {
                        spotifyManager.connectAndPlay(
                            playlistUri = playlistUri,
                            onConnected = { Log.d("MapScreen", "Música iniciada por tap en ${marker.title}") },
                            onError = { error -> Log.e("MapScreen", "Error: $error") }
                        )
                    }
                    true
                }
            }
        )
    } else {
        Text("La aplicación requiere acceso a la ubicación para funcionar.")
    }
}