package com.example.geobeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.geobeats.ui.theme.GeoBeatsTheme
import com.example.geobeats.ui.theme.MapScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Este es el tema por defecto que Android Studio generó para ti
            GeoBeatsTheme {
                // Surface es el contenedor principal que usa el color de fondo del tema
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Aquí llamamos a tu pantalla del mapa que ya está configurada
                    MapScreen()
                }
            }
        }
    }
}