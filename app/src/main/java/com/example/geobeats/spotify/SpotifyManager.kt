package com.example.geobeats.spotify

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.geobeats.BuildConfig
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TrackState(
    val trackName: String = "Buscando geocercas...",
    val artistName: String = "Camina para descubrir música",
    val isPaused: Boolean = true,
    val imageBitmap: Bitmap? = null
)

class SpotifyManager(private val context: Context) {

    private val CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
    private val REDIRECT_URI = BuildConfig.SPOTIFY_REDIRECT_URI

    private var spotifyAppRemote: SpotifyAppRemote? = null

    private val _trackState = MutableStateFlow(TrackState())
    val trackState: StateFlow<TrackState> = _trackState.asStateFlow()

    fun connectAndPlay(
        playlistUri: String,
        onConnected: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        Log.d("SpotifyManager", "========================================")
        Log.d("SpotifyManager", "PASO 1: Intentando reproducir URI: '$playlistUri'")

        // Validación básica de la URI
        if (playlistUri.isBlank() || !playlistUri.startsWith("spotify:")) {
            val errorMsg = "La URI proporcionada es inválida: '$playlistUri'. Debe empezar con 'spotify:'"
            Log.e("SpotifyManager", errorMsg)
            onError(errorMsg)
            return
        }

        if (spotifyAppRemote?.isConnected == true) {
            Log.d("SpotifyManager", "PASO 2: Ya estábamos conectados. Enviando comando de Play...")
            playUri(playlistUri, onError)
            onConnected()
            return
        }

        Log.d("SpotifyManager", "PASO 2: No estamos conectados. Iniciando conexión con App Remote...")
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        try {
            SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    Log.d("SpotifyManager", "PASO 3: ¡Conexión EXITOSA a Spotify!")
                    spotifyAppRemote = appRemote

                    // Enviamos la canción una vez conectados
                    playUri(playlistUri, onError)

                    // Suscripción al estado con reporte de errores
                    appRemote.playerApi.subscribeToPlayerState()
                        .setEventCallback { playerState: PlayerState ->
                            val track = playerState.track
                            if (track != null) {
                                Log.d("SpotifyManager", "INFO: Reproduciendo -> ${track.name} by ${track.artist.name} (Pausado: ${playerState.isPaused})")
                                _trackState.value = _trackState.value.copy(
                                    trackName = track.name,
                                    artistName = track.artist.name,
                                    isPaused = playerState.isPaused
                                )

                                appRemote.imagesApi.getImage(track.imageUri)
                                    .setResultCallback { bitmap ->
                                        _trackState.value = _trackState.value.copy(imageBitmap = bitmap)
                                    }
                                    .setErrorCallback { error ->
                                        Log.e("SpotifyManager", "ERROR AL DESCARGAR IMAGEN: ${error.message}")
                                    }
                            }
                        }
                        .setErrorCallback { error ->
                            Log.e("SpotifyManager", "ERROR DE SUSCRIPCIÓN AL REPRODUCTOR: ${error.message}", error)
                        }
                    onConnected()
                }

                override fun onFailure(throwable: Throwable) {
                    val errorMsg = "FALLO CRÍTICO DE CONEXIÓN: ${throwable.message}"
                    Log.e("SpotifyManager", errorMsg, throwable)
                    Log.e("SpotifyManager", "Causa detallada: ${throwable.cause}")
                    onError(errorMsg)
                }
            })
        } catch (e: Exception) {
            Log.e("SpotifyManager", "EXCEPCIÓN INTERNA AL CONECTAR: ${e.message}", e)
            onError(e.message ?: "Error desconocido")
        }
    }

    // Función auxiliar para solicitar reproducción con manejo estricto de errores
    private fun playUri(uri: String, onError: (String) -> Unit) {
        spotifyAppRemote?.playerApi?.play(uri)
            ?.setResultCallback {
                Log.d("SpotifyManager", "ÉXITO: El comando de reproducir '$uri' fue aceptado por Spotify.")
            }
            ?.setErrorCallback { error ->
                val errorMsg = "ERROR AL REPRODUCIR LA CANCIÓN '$uri': ${error.message}"
                Log.e("SpotifyManager", errorMsg, error)
                onError(errorMsg)
            }
    }

    // --- Controles con reportes de error ---

    fun resume() {
        spotifyAppRemote?.playerApi?.resume()?.setErrorCallback { Log.e("SpotifyManager", "ERROR AL REANUDAR: ${it.message}") }
    }

    fun pause() {
        spotifyAppRemote?.playerApi?.pause()?.setErrorCallback { Log.e("SpotifyManager", "ERROR AL PAUSAR: ${it.message}") }
    }

    fun skipNext() {
        spotifyAppRemote?.playerApi?.skipNext()?.setErrorCallback { Log.e("SpotifyManager", "ERROR EN SIGUIENTE: ${it.message}") }
    }

    fun skipPrevious() {
        spotifyAppRemote?.playerApi?.skipPrevious()?.setErrorCallback { Log.e("SpotifyManager", "ERROR EN ANTERIOR: ${it.message}") }
    }

    fun disconnect() {
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
            spotifyAppRemote = null
            Log.d("SpotifyManager", "Desconectado intencionalmente de Spotify.")
        }
    }
}