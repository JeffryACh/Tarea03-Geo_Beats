package com.example.geobeats.spotify

import android.content.Context
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState

class SpotifyManager(private val context: Context) {

    private val CLIENT_ID = "d788f4c7ff9944af90d093bb347e413c"
    private val REDIRECT_URI = "https://GeoBeats.com/callback"

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
                        Log.d("SpotifyManager", "🎵 Reproduciendo: ${track.name} - ${track.artist.name}")
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