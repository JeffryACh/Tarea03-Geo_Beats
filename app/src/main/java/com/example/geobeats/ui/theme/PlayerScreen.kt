package com.example.geobeats.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.geobeats.spotify.SpotifyManager

@Composable
fun PlayerScreen(
    spotifyManager: SpotifyManager,
    onClose: () -> Unit
) {
    val playerState by spotifyManager.playerState.collectAsState()
    val track = playerState?.track
    val isPaused = playerState?.isPaused ?: true

    // URL de la carátula real de Spotify (versión limpia y sin errores)
    val albumArtUrl: String = track?.imageUri?.let { imageUri ->
        val imageUrl = imageUri.toString()
        if (imageUrl.startsWith("spotify:image:")) {
            "https://i.scdn.co/image/" + imageUrl.substringAfter("spotify:image:")
        } else {
            imageUrl
        }
    } ?: ""

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF000000)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Decoración superior GeoBeats (solo visual)
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .background(Color(0xFF0066FF), shape = RoundedCornerShape(50))
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "GeoBeats",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Reproduciendo Música",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Recuadro del álbum con foto real
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (albumArtUrl.isNotEmpty()) {
                    AsyncImage(
                        model = albumArtUrl,
                        contentDescription = "Carátula del álbum",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "Album",
                        fontSize = 28.sp,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = track?.name ?: "Nombre de la canción",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track?.artist?.name ?: "Autor",
                fontSize = 16.sp,
                color = Color(0xFFB3B3B3),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Slider(
                value = 0.45f,
                onValueChange = {},
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFF0066FF),
                    inactiveTrackColor = Color(0xFF444444)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { spotifyManager.skipPrevious() }) {
                    Icon(Icons.Filled.SkipPrevious, "Anterior", tint = Color.White, modifier = Modifier.size(36.dp))
                }

                IconButton(
                    onClick = { if (isPaused) spotifyManager.resume() else spotifyManager.pause() },
                    modifier = Modifier.size(72.dp).background(Color(0xFF0066FF), CircleShape)
                ) {
                    Icon(
                        if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = if (isPaused) "Play" else "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(onClick = { spotifyManager.skipNext() }) {
                    Icon(Icons.Filled.SkipNext, "Siguiente", tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    spotifyManager.disconnect()
                    onClose()
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp)
            ) {
                Text("Volver", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}