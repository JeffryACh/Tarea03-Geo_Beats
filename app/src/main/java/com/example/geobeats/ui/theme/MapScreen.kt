package com.example.geobeats.ui.theme

// Import importantísimo: Asegura que es el de Android y no el de Java
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@Composable
fun MapScreen() {
    // El estado ahora funcionará correctamente gracias a los imports de getValue y setValue
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Especificamos explícitamente el tipo Map<String, Boolean> para evitar el error de inferencia
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
                // Activar controles de zoom
                googleMap.uiSettings.isZoomControlsEnabled = true

                // Coordenadas del punto de interés (Ejemplo: cerca del TEC)
                val puntoInteres = LatLng(9.8563, -83.9127)

                // Colocar el marcador
                googleMap.addMarker(
                    MarkerOptions()
                        .position(puntoInteres)
                        .title("Tienda Urbana Principal")
                        .snippet("Toca aquí para iniciar la música")
                )

                // Mover la cámara a ese punto
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntoInteres, 15f))

                // Evento de clic en el marcador
                googleMap.setOnMarkerClickListener { marker ->
                    Log.d("MapScreen", "Marcador tocado: ${marker.title}")
                    false
                }
            }
        )
    } else {
        Text("La aplicación requiere acceso a la ubicación para funcionar.")
    }
}