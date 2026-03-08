package com.example.geobeats.ui.theme

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView

@Composable
fun MapContainer(
    onMapReady: (GoogleMap) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    // En versiones recientes de Compose, usamos esta línea para el ciclo de vida
    val lifecycleOwner = LocalLifecycleOwner.current

    // Gestión del ciclo de vida del mapa
    DisposableEffect(lifecycleOwner) {
        // Aquí especificamos explícitamente los tipos (LifecycleOwner y Lifecycle.Event)
        // para solucionar el error "Cannot infer type"
        val observer = LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Renderizar la vista nativa de Google Maps dentro de Jetpack Compose
    AndroidView(
        factory = {
            mapView.apply {
                getMapAsync { googleMap ->
                    onMapReady(googleMap)
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}