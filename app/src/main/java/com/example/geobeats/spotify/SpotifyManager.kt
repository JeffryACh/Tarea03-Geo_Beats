package com.example.geobeats.spotify

import android.content.Context
import android.util.Log
import com.example.geobeats.BuildConfig // Importamos las variables seguras
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState

class SpotifyManager(private val context: Context) {

    // Ahora las llaves se leen de forma segura y no están expuestas en el código fuente
    private val CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
    private val REDIRECT_URI = BuildConfig.SPOTIFY_REDIRECT_URI

    private var spotifyAppRemote: SpotifyAppRemote? = null

    fun connectAndPlay(
        playlistUri: String,
        onConnected: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        // 1. ESCUDO: Si ya estamos conectados, solo reproducimos y evitamos reconectar
        if (spotifyAppRemote?.isConnected == true) {
            Log.d("SpotifyManager", "⚡ Ya hay conexión. Reproduciendo...")
            spotifyAppRemote?.playerApi?.play(playlistUri)
            onConnected()
            return
        }

        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        // 2. ESCUDO: Envolver el intento de conexión en un try-catch
        try {
            SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    spotifyAppRemote = appRemote
                    Log.d("SpotifyManager", "✅ Conectado a Spotify App Remote!")
                    appRemote.playerApi.play(playlistUri)

                    appRemote.playerApi.subscribeToPlayerState()
                        .setEventCallback { playerState: PlayerState ->
                            val track = playerState.track
                            if (track != null) {
                                Log.d("SpotifyManager", "🎵 Sonando: ${track.name} - ${track.artist.name}")
                            }
                        }
                    onConnected()
                }

                override fun onFailure(throwable: Throwable) {
                    val errorMsg = "❌ Error de conexión: ${throwable.message}"
                    Log.e("SpotifyManager", errorMsg, throwable)
                    onError(errorMsg)
                }
            })
        } catch (e: Exception) {
            // Si la app de Spotify no existe o hay un error fatal, lo atrapamos aquí sin que la app se cierre
            val errorFatal = "CRÍTICO al conectar: ${e.message}"
            Log.e("SpotifyManager", errorFatal, e)
            onError(errorFatal)
        }
    }

    fun disconnect() {
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
            spotifyAppRemote = null
            Log.d("SpotifyManager", "🔌 Desconectado de Spotify")
        }
    }

    fun pause() {
        spotifyAppRemote?.playerApi?.pause()
    }
}