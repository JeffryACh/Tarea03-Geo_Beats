package com.example.geobeats.spotify

import android.content.Context
import android.util.Log
import com.example.geobeats.BuildConfig
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpotifyManager(private val context: Context) {

    private val CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
    private val REDIRECT_URI = BuildConfig.SPOTIFY_REDIRECT_URI

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private val _playerState = MutableStateFlow<PlayerState?>(null)
    val playerState: StateFlow<PlayerState?> = _playerState.asStateFlow()
    fun connectAndPlay(
        playlistUri: String,
        onConnected: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        // ESCUDO 1: Si ya estamos conectados, solo cambiamos la playlist y evitamos reconectar
        if (spotifyAppRemote?.isConnected == true) {
            spotifyAppRemote?.playerApi?.play(playlistUri)
            onConnected()
            return
        }

        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        // ESCUDO 2: Envolver el intento de conexión para evitar cierres forzados de la app
        try {
            SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    spotifyAppRemote = appRemote
                    appRemote.playerApi.play(playlistUri)

                    // Suscripción al estado (nombre de canción, artista, pausa, etc.)
                    appRemote.playerApi.subscribeToPlayerState()
                        .setEventCallback { state: PlayerState ->
                            _playerState.value = state
                        }
                        .setErrorCallback { throwable ->
                            Log.e("SpotifyManager", "Error en player state", throwable)
                        }
                    onConnected()
                }

                override fun onFailure(throwable: Throwable) {
                    val errorMsg = "Error de conexión con Spotify App Remote: ${throwable.message}"
                    Log.e("SpotifyManager", errorMsg, throwable)
                    onError(errorMsg)
                }
            })
        } catch (e: Exception) {
            val errorFatal = "Error crítico al intentar conectar con Spotify: ${e.message}"
            Log.e("SpotifyManager", errorFatal, e)
            onError(errorFatal)
        }
    }

    fun resume() {
        spotifyAppRemote?.playerApi?.resume()
    }

    fun pause() {
        spotifyAppRemote?.playerApi?.pause()
    }

    fun disconnect() {
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
            spotifyAppRemote = null
        }
        _playerState.value = null
    }
    fun skipNext() {
        spotifyAppRemote?.playerApi?.skipNext()
    }

    fun skipPrevious() {
        spotifyAppRemote?.playerApi?.skipPrevious()
    }
}