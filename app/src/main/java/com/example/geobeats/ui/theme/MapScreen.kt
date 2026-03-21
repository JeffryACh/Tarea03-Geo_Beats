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
    geoViewModel: GeoViewModel = viewModel(),
    spotifyManager: SpotifyManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Estados de Permisos
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Estados de la interfaz para CRUD de puntos
    var showDialog by remember { mutableStateOf(false) }
    var editingPoint by remember { mutableStateOf<PointOfInterest?>(null) }
    var tempLatLng by remember { mutableStateOf<LatLng?>(null) }
    var placeName by remember { mutableStateOf("") }
    var playlistUri by remember { mutableStateOf("") }

    // NUEVO ESTADO: Para guardar la URI cuando entramos a una geocerca
    var pendingUriToPlay by remember { mutableStateOf<String?>(null) }

    // Referencias y Datos
    var googleMapInstance by remember { mutableStateOf<GoogleMap?>(null) }
    val pointsOfInterest = remember {
        mutableStateListOf<PointOfInterest>().apply {
            addAll(PointOfInterest.obtenerLugares())
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // para atajar error de no tener spotify
    val snackbarHostState = remember { SnackbarHostState() }
    var spotifyErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(spotifyErrorMessage) {
        spotifyErrorMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
            spotifyErrorMessage = null
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(hasLocationPermission, pointsOfInterest.size, googleMapInstance) {
        if (hasLocationPermission) {
            // CAMBIO AQUÍ: En lugar de reproducir directamente, guardamos la URI pendiente
            geoViewModel.startLocationTracking(pointsOfInterest) { uri ->
                pendingUriToPlay = uri
            }

            googleMapInstance?.let { map ->
                map.clear()
                pointsOfInterest.forEach { point ->
                    val isCustom = point.id.startsWith("custom")
                    val color =
                        if (isCustom) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_GREEN
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

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (hasLocationPermission) {
                MapContainer(
                    onMapReady = { googleMap ->
                        googleMapInstance = googleMap
                        googleMap.uiSettings.isZoomControlsEnabled = true

                        try {
                            googleMap.isMyLocationEnabled = true
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    val currentPos = LatLng(it.latitude, it.longitude)
                                    googleMap.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            currentPos,
                                            15f
                                        )
                                    )
                                }
                            }
                        } catch (e: SecurityException) {
                            Log.e("Map", "Error de seguridad en GPS")
                        }

                        googleMap.setOnMapLongClickListener { latLng ->
                            editingPoint = null
                            tempLatLng = latLng
                            placeName = ""
                            playlistUri = ""
                            showDialog = true
                        }

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
                                    spotifyManager.connectAndPlay(
                                        playlistUri = point.spotifyUri,
                                        onConnected = {

                                        },
                                        onError = { errorMsg ->
                                            Log.d("MapScreen-Spotify", "Error recibido: $errorMsg")

                                            spotifyErrorMessage = when {
                                                errorMsg.lowercase().contains("couldnotfindspotifyapp") ||
                                                        errorMsg.lowercase().contains("could not find spotify") ||
                                                        errorMsg.lowercase().contains("could not find app") ||
                                                        "couldnotfindspotifyapp" in errorMsg.lowercase() ||
                                                        "not installed" in errorMsg.lowercase() -> {

                                                    "Spotify no está instalado.\nInstálalo desde Play Store para reproducir música."
                                                }

                                                "not logged in" in errorMsg.lowercase() ||
                                                        "authorization" in errorMsg.lowercase() ->
                                                    "Necesitas iniciar sesión o autorizar Spotify en la app."

                                                else ->
                                                    "Error al conectar con Spotify:\n$errorMsg\n\n(Verifica que Spotify esté instalado y abierto)"
                                            }
                                        }
                                    )
                                }
                            }
                            true
                        }
                    }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Esperando permisos de ubicación...")
                    Button(onClick = {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                    }) {
                        Text("Conceder Permisos")
                    }
                }
            }

            // Diálogo para Crear o Editar un punto manual
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(if (editingPoint == null) "Nuevo Punto" else "Editar Punto") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = placeName,
                                onValueChange = { placeName = it },
                                label = { Text("Nombre") })
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = playlistUri,
                                onValueChange = { playlistUri = it },
                                label = { Text("Spotify URI") })
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (editingPoint == null) {
                                tempLatLng?.let {
                                    pointsOfInterest.add(
                                        PointOfInterest(
                                            id = "custom_${UUID.randomUUID()}",
                                            name = placeName.ifBlank { "Punto Manual" },
                                            latitude = it.latitude,
                                            longitude = it.longitude,
                                            spotifyUri = playlistUri.ifBlank { "spotify:playlist:37i9dQZF1DXcBWIGOYBMm1" }
                                        ))
                                }
                            } else {
                                val index =
                                    pointsOfInterest.indexOfFirst { it.id == editingPoint?.id }
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

            // NUEVO DIÁLOGO: Aparece cuando entras al radio de un punto
            pendingUriToPlay?.let { uri ->
                val puntoCercano = pointsOfInterest.find { it.spotifyUri == uri }
                val nombreLugar = puntoCercano?.name ?: "este Geo-Beat"

                AlertDialog(
                    onDismissRequest = { pendingUriToPlay = null },
                    title = { Text("🎵 ¡Música Detectada!") },
                    text = { Text("Has entrado al rango de $nombreLugar. ¿Quieres sintonizar su banda sonora?") },
                    confirmButton = {
                        Button(onClick = {
                            spotifyManager.connectAndPlay(
                                playlistUri = uri,
                                onConnected = { /* éxito opcional */ },
                                onError = { errorMsg ->
                                    Log.d("MapScreen-Spotify", "Error recibido: $errorMsg")  // para confirmar llegada

                                    spotifyErrorMessage = when {
                                        errorMsg.lowercase().contains("couldnotfindspotifyapp") ||
                                                errorMsg.lowercase().contains("could not find spotify") ||
                                                errorMsg.lowercase().contains("could not find app") ||
                                                "couldnotfindspotifyapp" in errorMsg.lowercase() ||
                                                "not installed" in errorMsg.lowercase() -> {

                                            "Spotify no está instalado.\nInstálalo desde Play Store para reproducir música."
                                        }

                                        "not logged in" in errorMsg.lowercase() ||
                                                "authorization" in errorMsg.lowercase() ->
                                            "Necesitas iniciar sesión o autorizar Spotify en la app."

                                        else ->
                                            "Error al conectar con Spotify:\n$errorMsg\n\n(Verifica que Spotify esté instalado y abierto)"
                                    }
                                }
                            )
                            pendingUriToPlay = null
                        }) {
                            Text("Reproducir")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingUriToPlay = null }) {
                            Text("Ignorar")
                        }
                    }
                )
            }
        }
    }
}