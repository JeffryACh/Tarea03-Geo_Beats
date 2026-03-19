package com.example.geobeats.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geobeats.spotify.SpotifyManager

@Composable
fun PlayerScreen(
    spotifyManager: SpotifyManager,
    modifier: Modifier = Modifier
) {
    // 1. OBSERVADOR MÁGICO: Escucha los cambios del StateFlow en tiempo real
    // (Esto soluciona los errores de "getValue" y "playerState")
    val trackState by spotifyManager.trackState.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 2. RENDERIZADO DE IMAGEN NATIVO
            // (Esto soluciona los errores de "Coil" y "AsyncImage")
            if (trackState.imageBitmap != null) {
                Image(
                    bitmap = trackState.imageBitmap!!.asImageBitmap(),
                    contentDescription = "Carátula del álbum",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder gris por si la imagen aún no ha cargado
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎵", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 3. TEXTOS DINÁMICOS
            // (Esto soluciona los errores de "track")
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = trackState.trackName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = trackState.artistName,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            // 4. CONTROLES DE REPRODUCCIÓN
            // (Esto soluciona los errores de iconos no resueltos)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { spotifyManager.skipPrevious() }) {
                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Anterior")
                }

                IconButton(onClick = {
                    if (trackState.isPaused) {
                        spotifyManager.resume()
                    } else {
                        spotifyManager.pause()
                    }
                }) {
                    Icon(
                        imageVector = if (trackState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (trackState.isPaused) "Reproducir" else "Pausar",
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = { spotifyManager.skipNext() }) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Siguiente")
                }
            }
        }
    }
}