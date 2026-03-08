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
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                Log.d("SpotifyManager", "✅ Conectado a Spotify App Remote!")

                // Reproduce la playlist del lugar automáticamente
                appRemote.playerApi.play(playlistUri)

                // Muestra en Log qué canción está sonando
                appRemote.playerApi.subscribeToPlayerState()
                    .setEventCallback { playerState: PlayerState ->
                        val track = playerState.track
                        if (track != null) {
                            Log.d("SpotifyManager", "🎵 Reproduciendo: ${track.name} - ${track.artist.name}")
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