package com.example.geobeats.ui.theme

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geobeats.model.PointOfInterest
import com.example.geobeats.spotify.SpotifyManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.UUID

@Composable
fun MapScreen(
    geoViewModel: GeoViewModel = viewModel()
) {
    val context = LocalContext.current
    val spotifyManager = remember { SpotifyManager(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    //Agregue esto
    var showPlayer by remember { mutableStateOf(false) }
    // Estados de Permisos
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Estados de la interfaz
    var showDialog by remember { mutableStateOf(false) }
    var editingPoint by remember { mutableStateOf<PointOfInterest?>(null) }
    var tempLatLng by remember { mutableStateOf<LatLng?>(null) }
    var placeName by remember { mutableStateOf("") }
    var playlistUri by remember { mutableStateOf("") }

    // Referencias y Datos
    var googleMapInstance by remember { mutableStateOf<GoogleMap?>(null) }
    val pointsOfInterest = remember {
        mutableStateListOf<PointOfInterest>().apply {
            addAll(PointOfInterest.obtenerLugares())
        }
    }

    // Gestor de permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Lanzar petición de permisos al iniciar
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Sincronización de Geocercas y Dibujo de Marcadores
    LaunchedEffect(hasLocationPermission, pointsOfInterest.size, googleMapInstance) {
        if (hasLocationPermission) {
            // Activar rastreo
            geoViewModel.startLocationTracking(pointsOfInterest) { uri ->
                spotifyManager.connectAndPlay(playlistUri = uri)
            }

            // Redibujar marcadores
            googleMapInstance?.let { map ->
                map.clear()
                pointsOfInterest.forEach { point ->
                    val isCustom = point.id.startsWith("custom")
                    val color = if (isCustom) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_GREEN
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(LatLng(point.latitude, point.longitude))
                            .title(point.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(color))
                    )
                    marker?.tag = point.id
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { spotifyManager.disconnect() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasLocationPermission) {
            if (showPlayer) {
                // Pantalla completa del reproductor
                PlayerScreen(
                    spotifyManager = spotifyManager,
                    onClose = { showPlayer = false }
                )
            } else {
                // El mapa normal
                MapContainer(
                    onMapReady = { googleMap ->
                        googleMapInstance = googleMap
                        googleMap.uiSettings.isZoomControlsEnabled = true
                        googleMap.uiSettings.isCompassEnabled = true

                        try {
                            googleMap.isMyLocationEnabled = true
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    val currentPos = LatLng(it.latitude, it.longitude)
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 16f))
                                }
                            }
                        } catch (e: SecurityException) {
                            Log.e("MapScreen", "Error de seguridad en GPS")
                        }

                        // Pulsación larga → nuevo punto
                        googleMap.setOnMapLongClickListener { latLng ->
                            editingPoint = null
                            tempLatLng = latLng
                            placeName = ""
                            playlistUri = ""
                            showDialog = true
                        }

                        // Pulsación normal en marcador → abre reproductor
                        googleMap.setOnMarkerClickListener { marker ->
                            val pointId = marker.tag as? String
                            val point = pointsOfInterest.find { it.id == pointId }

                            if (point != null) {
                                if (point.id.startsWith("custom")) {
                                    editingPoint = point
                                    placeName = point.name
                                    playlistUri = point.spotifyUri
                                    showDialog = true
                                } else {
                                    spotifyManager.connectAndPlay(point.spotifyUri)
                                    showPlayer = true          // ← Aquí abre el reproductor
                                }
                            }
                            true
                        }
                    }
                )
            }
        } else {
            // Pantalla de permisos (sin cambios)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Esperando permisos de ubicación...")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)) }) {
                    Text("Conceder Permisos")
                }
            }
        }

        // Diálogo de Crear/Editar (solo se muestra cuando NO está el reproductor)
        if (showDialog && !showPlayer) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (editingPoint == null) "Nuevo Punto de Interés" else "Editar Punto") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = placeName,
                            onValueChange = { placeName = it },
                            label = { Text("Nombre del lugar") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = playlistUri,
                            onValueChange = { playlistUri = it },
                            label = { Text("Spotify URI (playlist)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (editingPoint == null) {
                            tempLatLng?.let {
                                pointsOfInterest.add(
                                    PointOfInterest(
                                        id = "custom_${UUID.randomUUID()}",
                                        name = placeName.ifBlank { "Punto Personalizado" },
                                        latitude = it.latitude,
                                        longitude = it.longitude,
                                        spotifyUri = playlistUri.ifBlank { "spotify:playlist:37i9dQZF1DXcBWIGOYBMm1" }
                                    )
                                )
                            }
                        } else {
                            val index = pointsOfInterest.indexOfFirst { it.id == editingPoint?.id }
                            if (index != -1) {
                                pointsOfInterest[index] = editingPoint!!.copy(
                                    name = placeName,
                                    spotifyUri = playlistUri
                                )
                            }
                        }
                        showDialog = false
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    if (editingPoint != null) {
                        TextButton(onClick = {
                            pointsOfInterest.removeAll { it.id == editingPoint?.id }
                            showDialog = false
                        }) { Text("Borrar", color = MaterialTheme.colorScheme.error) }
                    } else {
                        TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
                    }
                }
            )
        }
    }
}