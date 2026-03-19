package com.example.geobeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.geobeats.spotify.SpotifyManager
import com.example.geobeats.ui.theme.GeoBeatsTheme
import com.example.geobeats.ui.theme.MapScreen
import com.example.geobeats.ui.theme.PlayerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoBeatsTheme {
                // Un Surface es el contenedor principal de fondo
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. CREAMOS LA INSTANCIA ÚNICA (EL "CEREBRO" COMPARTIDO)
                    val context = LocalContext.current
                    val spotifyManager = remember { SpotifyManager(context) }

                    // 2. ACOMODAMOS LAS PANTALLAS EN UNA COLUMNA
                    Column(modifier = Modifier.fillMaxSize()) {
                        // El Mapa arriba, tomando todo el espacio disponible (weight = 1f)
                        MapScreen(
                            spotifyManager = spotifyManager,
                            modifier = Modifier.weight(1f)
                        )

                        // El Reproductor abajo, usando EL MISMO spotifyManager
                        PlayerScreen(
                            spotifyManager = spotifyManager
                        )
                    }
                }
            }
        }
    }
}