package com.example.mediaplayerdroid.shared

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import classes.MainStruct
import classes.Music
import classes.MusicRecent
import classes.Playlist


/**
 * This class provides a MediaBrowser through a service. It exposes the media library to a browsing
 * client, through the onGetRoot and onLoadChildren methods. It also creates a MediaSession and
 * exposes it through its MediaSession.Token, which allows the client to create a MediaController
 * that connects to and send control commands to the MediaSession remotely. This is useful for
 * user interfaces that need to interact with your media session, like Android Auto. You can
 * (should) also use the same service from your app's UI, which gives a seamless playback
 * experience to the user.
 *
 *
 * To implement a MediaBrowserService, you need to:
 *
 *  *  Extend [MediaBrowserServiceCompat], implementing the media browsing
 * related methods [MediaBrowserServiceCompat.onGetRoot] and
 * [MediaBrowserServiceCompat.onLoadChildren];
 *
 *  *  In onCreate, start a new [MediaSessionCompat] and notify its parent
 * with the session"s token [MediaBrowserServiceCompat.setSessionToken];
 *
 *  *  Set a callback on the [MediaSessionCompat.setCallback].
 * The callback will receive all the user"s actions, like play, pause, etc;
 *
 *  *  Handle all the actual music playing using any method your app prefers (for example,
 * [android.media.MediaPlayer])
 *
 *  *  Update playbackState, "now playing" metadata and queue, using MediaSession proper methods
 * [MediaSessionCompat.setPlaybackState]
 * [MediaSessionCompat.setMetadata] and
 * [MediaSessionCompat.setQueue])
 *
 *  *  Declare and export the service in AndroidManifest with an intent receiver for the action
 * android.media.browse.MediaBrowserService
 *
 * To make your app compatible with Android Auto, you also need to:
 *
 *  *  Declare a meta-data tag in AndroidManifest.xml linking to a xml resource
 * with a &lt;automotiveApp&gt; root element. For a media app, this must include
 * an &lt;uses name="media"/&gt; element as a child.
 * For example, in AndroidManifest.xml:
 * &lt;meta-data android:name="com.google.android.gms.car.application"
 * android:resource="@xml/automotive_app_desc"/&gt;
 * And in res/values/automotive_app_desc.xml:
 * &lt;automotiveApp&gt;
 * &lt;uses name="media"/&gt;
 * &lt;/automotiveApp&gt;
 *
 */
class MediaService : MediaBrowserServiceCompat(),
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnPreparedListener,
    OnAudioFocusChangeListener{


    companion object {
        const val MEDIA_ID_ROOT = "ROOT"
        const val MEDIA_PLAYLISTS = "PLAYLISTS"
        const val MEDIA_RECENTS = "RECENTS"
        const val MEDIA_CONFIGURATIONS = "CONFIGURATIONS"
        const val MEDIA_PLAYBACK_MODE = "PLAYBACK_MODE"
        const val MEDIA_REPEAT_ALREADY_PLAYED_SONGS = "REPEAT_ALREADY_PLAYED_SONGS"
        const val MEDIA_REPEAT_ALREADY_VALUE = "REPEAT_ALREADY_VALUE"
        const val MEDIA_RECENTS_CLEAR = "RECENTS_CLEAR"
        const val MEDIA_LOG = "LOG"
        const val MEDIA_LOG_CLEAR = "LOG_CLEAR"
        const val MEDIA_LOG_ITEM = "LOG_ITEM"
        const val MEDIA_PLAYBACK_MODE_SELECTED = "PLAYBACK_MODE_SELECTED"
        const val MEDIA_PLAYLIST = "{PLAYLIST}"
        const val MEDIA_PAGE = "{PAGE}"
        const val MEDIA_ITEMS_PAGE = 30
        const val MEDIA_MUSIC = "{MUSIC}"
        const val MEDIA_PLAYLIST_ID = "PLAYLIST_ID"
        const val MEDIA_PLAYBACK_ISASC = "isAsc"
        const val MEDIA_PLAYBACK_ISDESC = "isDesc"
        const val MEDIA_PLAYBACK_ISREPEAT = "isRepeat"
        const val MEDIA_PLAYBACK_ISRANDOMALL = "isRandomAll"
        const val MEDIA_PLAYBACK_ISRANDOMPLAYLIST = "isRandomPlaylist"

        const val CHANNEL_ID = "CANAL_MUSICA"
        const val CHANNEL_NAME = "Canal de música"
        const val CHANNEL_DESCRIPTION = "Canal de música"
        const val NOTIFICATION_ID = 100
    }

    private lateinit var session: MediaSessionCompat
    private lateinit var mainStruct: MainStruct
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private var audioAttributes: AudioAttributes? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var notificationManager: NotificationManager? = null
    private lateinit var notification: NotificationCompat.Builder
    private var playbackStateActual: Int = 0
    private var playlistActual: Playlist? = null
    private var musicActual: Music? = null

    private val callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() { play() }

        override fun onSkipToQueueItem(queueId: Long) {}

        override fun onSeekTo(position: Long) { seekTo(position) }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            if(mediaId != null) {
                val musicName = mediaId.replace(MEDIA_MUSIC, "")
                if(extras != null){
                    val playlistId = extras.getInt(MEDIA_PLAYLIST_ID)
                    if(playlistId > -1){
                        for(playlist in mainStruct.playlists!!){
                            if(playlist.id == playlistId){
                                playlistActual = playlist
                                break
                            }
                        }
                    }
                }
                val i = mainStruct.musics!!.indexOf(Music("", musicName))
                if(i > -1){
                    start(mainStruct.musics!![i])
                }
            }
        }

        override fun onPause() { pause() }

        override fun onStop() { stop() }

        override fun onSkipToNext() { skipToNext() }

        override fun onSkipToPrevious() { skipToPrevious() }

        override fun onCustomAction(action: String?, extras: Bundle?) {}

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {}

        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
            if(mediaButtonEvent != null) {
                if (Intent.ACTION_MEDIA_BUTTON == mediaButtonEvent.action) {
                    val event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)

                    if(event?.action == KeyEvent.ACTION_UP) {
                        event.let {
                            when (it.keyCode) {
                                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> playPause()
                                KeyEvent.KEYCODE_MEDIA_PLAY -> play()
                                KeyEvent.KEYCODE_MEDIA_PAUSE -> pause()
                                KeyEvent.KEYCODE_MEDIA_NEXT -> onSkipToNext()
                                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> onSkipToPrevious()
                                else -> {}
                            }
                        }
                    }
                }
            }

            return true
        }
    }

    override fun onCreate() {
        super.onCreate()

        session = MediaSessionCompat(this, "MediaService")
        sessionToken = session.sessionToken
        session.setCallback(callback)
        //session.setFlags(
        //    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
        //            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        //)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        mainStruct = MainStruct.getUnique()
        // Carga inicial
        mainStruct.loadFromFile(dataDir.path)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null){
            if(intent.hasExtra("COMMAND")){
                when(intent.getStringExtra("COMMAND")){
                    "START" -> {
                        if(intent.hasExtra(MEDIA_PLAYLIST_ID)){
                            val playlistId = intent.getIntExtra(MEDIA_PLAYLIST_ID, -1)
                            if(playlistId > -1){
                                for(playlist in mainStruct.playlists!!){
                                    if(playlist.id == playlistId){
                                        playlistActual = playlist
                                        break
                                    }
                                }
                            }
                        }
                        if(intent.hasExtra("MUSIC")){
                            val musicName = intent.getStringExtra("MUSIC")!!
                            val i = mainStruct.musics!!.indexOf(Music("", musicName))
                            if(i > -1){
                                start(mainStruct.musics!![i])
                            }
                        }
                    }
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        if(mediaPlayer != null){
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
        }
        if(session.isActive){
            session.isActive = false
        }
        session.release()
        mainStruct.saveToFile(dataDir.path)
        stopSelf()
        stopForeground(STOP_FOREGROUND_REMOVE)
        if(notificationManager != null){
            notificationManager!!.cancelAll()
        }
        if(audioFocusRequest != null){
            audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
            audioFocusRequest = null
        }
        super.onDestroy()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        // Não tem esquema de permissões
        return BrowserRoot(MEDIA_ID_ROOT, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
        if (parentId == MEDIA_ID_ROOT) {
            val list = ArrayList<MediaItem>()

            list.add(
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(MEDIA_PLAYLISTS)
                        .setTitle("Playlists")
                        .setIconBitmap(
                            BitmapFactory.decodeResource(
                                resources,
                                R.drawable.android_auto_playlist_icon
                            )
                        )
                        .build(), MediaItem.FLAG_BROWSABLE
                )
            )

            list.add(
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId("${MEDIA_RECENTS}*&${MEDIA_PAGE}*&1")
                        .setTitle("Recentes")
                        .setIconBitmap(
                            BitmapFactory.decodeResource(
                                resources,
                                R.drawable.android_auto_recents_icon
                            )
                        )
                        .build(), MediaItem.FLAG_BROWSABLE
                )
            )

            list.add(
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(MEDIA_CONFIGURATIONS)
                        .setTitle("Configurações")
                        .setIconBitmap(
                            BitmapFactory.decodeResource(
                                resources,
                                R.drawable.android_auto_settings_icon
                            )
                        )
                        .build(), MediaItem.FLAG_BROWSABLE
                )
            )

            result.sendResult(list)
        } else if (parentId == MEDIA_PLAYLISTS) {
            result.detach()
            val list = ArrayList<MediaItem>()
            for (playlist in mainStruct.playlists!!) {
                list.add(
                    MediaItem(
                        MediaDescriptionCompat.Builder()
                            .setMediaId("${MEDIA_PLAYLIST}*&${playlist.name}*&${MEDIA_PAGE}*&1")
                            .setTitle(playlist.name)
                            .setSubtitle("Total de músicas: ${playlist.size}")
                            .build(), MediaItem.FLAG_BROWSABLE
                    )
                )
            }
            result.sendResult(list)
        } else if (parentId.contains(MEDIA_PLAYLIST)) {
            result.detach()
            val list = ArrayList<MediaItem>()
            val contents = parentId.split("*&")
            val playlistName = contents[1]
            val pageActual = contents[3].toInt()
            for (playlist in mainStruct.playlists!!) {
                if (playlist.name == playlistName) {
                    val start = (MEDIA_ITEMS_PAGE) * (pageActual - 1)
                    var total = MEDIA_ITEMS_PAGE * pageActual
                    val isLastPage = total > playlist.size
                    if (isLastPage) {
                        total = playlist.size
                    } else {
                        var totalNextPage = (pageActual + 1) * MEDIA_ITEMS_PAGE
                        if (totalNextPage > playlist.size) {
                            totalNextPage = playlist.size
                        }
                        list.add(
                            MediaItem(
                                MediaDescriptionCompat.Builder()
                                    .setMediaId("${MEDIA_PLAYLIST}*&${playlist.name}*&${MEDIA_PAGE}*&${pageActual + 1}")
                                    .setTitle("Página ${pageActual + 1}")
                                    .setSubtitle("Músicas de ${pageActual * MEDIA_ITEMS_PAGE} até $totalNextPage")
                                    .build(), MediaItem.FLAG_BROWSABLE
                            )
                        )
                    }
                    // Carrega as 30 músicas da página que o usuário clicou
                    for (i in (start..<total)) { // Paginação
                        val music = playlist[i]
                        mainStruct.loadInformations(music)
                        val extra = Bundle()
                        extra.putInt(MEDIA_PLAYLIST_ID, playlist.id)
                        val mdc = MediaDescriptionCompat.Builder()
                            .setMediaId("${MEDIA_MUSIC}${music.fileName}")
                            .setTitle(music.title)
                            .setSubtitle(music.artist)
                            .setExtras(extra)
                        if (music.art != null) {
                            mdc.setIconBitmap(
                                BitmapFactory.decodeByteArray(
                                    music.art,
                                    0,
                                    music.art!!.size
                                )
                            )
                        }
                        list.add(MediaItem(mdc.build(), MediaItem.FLAG_PLAYABLE))
                    }
                    if (!isLastPage) {
                        var totalNextPage = (pageActual + 1) * MEDIA_ITEMS_PAGE
                        if (totalNextPage > playlist.size) {
                            totalNextPage = playlist.size
                        }
                        list.add(
                            MediaItem(
                                MediaDescriptionCompat.Builder()
                                    .setMediaId("${MEDIA_PLAYLIST}*&${playlist.name}*&${MEDIA_PAGE}*&${pageActual + 1}")
                                    .setTitle("Página ${pageActual + 1}")
                                    .setSubtitle("Músicas de ${pageActual * MEDIA_ITEMS_PAGE} até $totalNextPage")
                                    .build(), MediaItem.FLAG_BROWSABLE
                            )
                        )
                    }
                }
            }
            result.sendResult(list)
        } else if (parentId.contains(MEDIA_RECENTS)) {
            result.detach()
            val list = ArrayList<MediaItem>()
            val contents = parentId.split("*&")
            val pageActual = contents[2].toInt()
            if (mainStruct.recents != null) {
                val start = (pageActual - 1) * MEDIA_ITEMS_PAGE
                var end = pageActual * MEDIA_ITEMS_PAGE
                val isLastPage = end > mainStruct.recents!!.size
                if (isLastPage) {
                    end = mainStruct.recents!!.size
                } else {
                    var totalNextPage = (pageActual + 1) * MEDIA_ITEMS_PAGE
                    if (totalNextPage > mainStruct.recents!!.size) {
                        totalNextPage = mainStruct.recents!!.size
                    }
                    list.add(
                        MediaItem(
                            MediaDescriptionCompat.Builder()
                                .setMediaId("${MEDIA_RECENTS}*&${MEDIA_PAGE}*&${pageActual + 1}")
                                .setTitle("Página ${pageActual + 1}")
                                .setSubtitle("Músicas de ${pageActual * MEDIA_ITEMS_PAGE} até $totalNextPage")
                                .build(), MediaItem.FLAG_BROWSABLE
                        )
                    )
                }
                // Carrega as 30 músicas da página que o usuário clicou
                for (i in (start..<end)) { // Paginação
                    val musicRecent = mainStruct.recents!![i]
                    mainStruct.loadInformations(musicRecent)
                    val extra = Bundle()
                    extra.putInt(MEDIA_PLAYLIST_ID, musicRecent.playlistId)
                    val mdc = MediaDescriptionCompat.Builder()
                        .setMediaId("${MEDIA_MUSIC}${musicRecent.fileName}")
                        .setTitle(musicRecent.title)
                        .setSubtitle(musicRecent.artist)
                        .setExtras(extra)
                    if (musicRecent.art != null) {
                        mdc.setIconBitmap(
                            BitmapFactory.decodeByteArray(
                                musicRecent.art,
                                0,
                                musicRecent.art!!.size
                            )
                        )
                    }
                    list.add(MediaItem(mdc.build(), MediaItem.FLAG_PLAYABLE))
                }
                if (!isLastPage) {
                    var totalNextPage = (pageActual + 1) * MEDIA_ITEMS_PAGE
                    if (totalNextPage > mainStruct.recents!!.size) {
                        totalNextPage = mainStruct.recents!!.size
                    }
                    list.add(
                        MediaItem(
                            MediaDescriptionCompat.Builder()
                                .setMediaId("${MEDIA_RECENTS}*&${MEDIA_PAGE}*&${pageActual + 1}")
                                .setTitle("Página ${pageActual + 1}")
                                .setSubtitle("Músicas de ${pageActual * MEDIA_ITEMS_PAGE} até $totalNextPage")
                                .build(), MediaItem.FLAG_BROWSABLE
                        )
                    )
                }
            }
            result.sendResult(list)
        } else if (parentId == MEDIA_CONFIGURATIONS) {
            result.detach()
            val list = ArrayList<MediaItem>()
            val subTitle = if (mainStruct.settings!!.isAsc) {
                "Ordem alfabética (A-Z)"
            } else if (mainStruct.settings!!.isDesc) {
                "Ordem alfabética invertida (Z-A)"
            } else if (mainStruct.settings!!.isRepeat) {
                "Repetir a mesma música"
            } else if (mainStruct.settings!!.isRandomAll) {
                "Aleatório geral"
            } else if (mainStruct.settings!!.isRandomPlaylist) {
                "Aleatório na playlist atual"
            } else {
                ""
            }
            list.add(
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(MEDIA_PLAYBACK_MODE)
                        .setTitle("Modo de reprodução")
                        .setSubtitle(subTitle)
                        .build(), MediaItem.FLAG_BROWSABLE
                )
            )
            val strRepeatAlreadyPlayedSongs = if (mainStruct.settings!!.repeatAlreadyPlayedSongs) {
                "Sim"
            } else {
                "Não"
            }
            list.add(
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(MEDIA_REPEAT_ALREADY_PLAYED_SONGS)
                        .setTitle("Repetir músicas já tocadas")
                        .setSubtitle(strRepeatAlreadyPlayedSongs)
                        .build(), MediaItem.FLAG_BROWSABLE
                )
            )
            list.add(
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(MEDIA_RECENTS_CLEAR)
                        .setTitle("Limpar lista de recentes")
                        .setSubtitle("Total de músicas: ${mainStruct.recents!!.size}")
                        .build(), MediaItem.FLAG_BROWSABLE
                )
            )
            list.add(
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(MEDIA_LOG)
                        .setTitle("Lista de erros")
                        .setSubtitle("Total de erros: ${mainStruct.logs!!.size}")
                        .build(), MediaItem.FLAG_BROWSABLE
                )
            )
            result.sendResult(list)
        } else if (parentId == MEDIA_PLAYBACK_MODE) {
            result.detach()
            val list = ArrayList<MediaItem>()
            if (!mainStruct.settings!!.isAsc) {
                list.add(
                    MediaItem(
                        MediaDescriptionCompat.Builder()
                            .setMediaId("${MEDIA_PLAYBACK_MODE_SELECTED}*&${MEDIA_PLAYBACK_ISASC}")
                            .setTitle("Ordem alfabética (A-Z)")
                            .build(), MediaItem.FLAG_BROWSABLE
                    )
                )
            }
            if (!mainStruct.settings!!.isDesc) {
                list.add(
                    MediaItem(
                        MediaDescriptionCompat.Builder()
                            .setMediaId("${MEDIA_PLAYBACK_MODE_SELECTED}*&${MEDIA_PLAYBACK_ISDESC}")
                            .setTitle("Ordem alfabética invertida (Z-A)")
                            .build(), MediaItem.FLAG_BROWSABLE
                    )
                )
            }
            if (!mainStruct.settings!!.isRepeat) {
                list.add(
                    MediaItem(
                        MediaDescriptionCompat.Builder()
                            .setMediaId("${MEDIA_PLAYBACK_MODE_SELECTED}*&${MEDIA_PLAYBACK_ISREPEAT}")
                            .setTitle("Repetir a mesma música")
                            .build(), MediaItem.FLAG_BROWSABLE
                    )
                )
            }
            if (!mainStruct.settings!!.isRandomAll) {
                list.add(
                    MediaItem(
                        MediaDescriptionCompat.Builder()
                            .setMediaId("${MEDIA_PLAYBACK_MODE_SELECTED}*&${MEDIA_PLAYBACK_ISRANDOMALL}")
                            .setTitle("Aleatório geral")
                            .build(), MediaItem.FLAG_BROWSABLE
                    )
                )
            }
            if (!mainStruct.settings!!.isRandomPlaylist) {
                list.add(
                    MediaItem(
                        MediaDescriptionCompat.Builder()
                            .setMediaId("${MEDIA_PLAYBACK_MODE_SELECTED}*&${MEDIA_PLAYBACK_ISRANDOMPLAYLIST}")
                            .setTitle("Aleatório na playlist atual")
                            .build(), MediaItem.FLAG_BROWSABLE
                    )
                )
            }
            result.sendResult(list)
        } else if (parentId.contains(MEDIA_PLAYBACK_MODE_SELECTED)) {
            result.detach()
            val contents = parentId.split("*&")
            mainStruct.settings!!.isAsc = contents[1] == MEDIA_PLAYBACK_ISASC
            mainStruct.settings!!.isDesc = contents[1] == MEDIA_PLAYBACK_ISDESC
            mainStruct.settings!!.isRepeat = contents[1] == MEDIA_PLAYBACK_ISREPEAT
            mainStruct.settings!!.isRandomAll = contents[1] == MEDIA_PLAYBACK_ISRANDOMALL
            mainStruct.settings!!.isRandomPlaylist = contents[1] == MEDIA_PLAYBACK_ISRANDOMPLAYLIST
            result.sendResult(ArrayList())
        } else if (parentId == MEDIA_REPEAT_ALREADY_PLAYED_SONGS) {
            result.detach()
            val list = ArrayList<MediaItem>()
            val option = if (mainStruct.settings!!.repeatAlreadyPlayedSongs) {
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId("${MEDIA_REPEAT_ALREADY_PLAYED_SONGS}*&${MEDIA_REPEAT_ALREADY_VALUE}*&FALSE")
                        .setTitle("Não")
                        .build(), MediaItem.FLAG_BROWSABLE
                )
            } else {
                MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId("${MEDIA_REPEAT_ALREADY_PLAYED_SONGS}*&${MEDIA_REPEAT_ALREADY_VALUE}*&TRUE")
                        .setTitle("Sim")
                        .build(), MediaItem.FLAG_BROWSABLE
                )
            }
            list.add(option)
            result.sendResult(list)
        } else if(parentId.contains(MEDIA_REPEAT_ALREADY_VALUE)){
            result.detach()
            val contents = parentId.split("*$")
            mainStruct.settings!!.repeatAlreadyPlayedSongs = contents[2] == "TRUE"
            result.sendResult(ArrayList())
        } else if (parentId == MEDIA_RECENTS_CLEAR) {
            result.detach()
            mainStruct.recents!!.clear()
            result.sendResult(ArrayList())
        } else if (parentId == MEDIA_LOG) {
            result.detach()
            val list = ArrayList<MediaItem>()
            val logs = mainStruct.logs!!
            if(logs.size > 0) {
                list.add(
                    MediaItem(
                        MediaDescriptionCompat.Builder()
                            .setMediaId(MEDIA_LOG_CLEAR)
                            .setTitle("Limpar lista de log")
                            .setSubtitle("Total de registros: ${logs.size}")
                            .build(), MediaItem.FLAG_BROWSABLE
                    )
                )
                var i = logs.size - 1
                while (i in (0..< logs.size)) {
                    // Exibir de trás para frente (Z > A)
                    list.add(
                        MediaItem(
                            MediaDescriptionCompat.Builder()
                                .setMediaId("${MEDIA_LOG}*&${MEDIA_LOG_ITEM}")
                                .setTitle(logs[i])
                                .build(), MediaItem.FLAG_BROWSABLE
                        )
                    )
                    i--
                }
            }
            result.sendResult(list)
        } else if(parentId == MEDIA_LOG_CLEAR){
            mainStruct.logs!!.clear()
            result.sendResult(ArrayList())
        } else if (parentId.contains(MEDIA_LOG_ITEM)) {
            result.sendResult(ArrayList())
        } else {
            result.sendResult(null)
        }
    }

    fun start(music: Music){
        if(mediaPlayer != null){
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
        }

        musicActual = null

        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer()
            audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
            mediaPlayer!!.setAudioAttributes(audioAttributes)
            mediaPlayer!!.setVolume(1.0F, 1.0F)
            mediaPlayer!!.setOnCompletionListener { skipToNext() }
            mediaPlayer!!.setOnPreparedListener(this)
            mediaPlayer!!.setOnErrorListener(this)
        }

        mediaPlayer!!.setDataSource(music.filePath)
        mediaPlayer!!.prepareAsync()

        musicActual = music
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> play()
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> pause()
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        play()

        val playlistId = if(playlistActual != null){
            playlistActual!!.id
        }else{
            -1
        }
        addRecent(playlistId, musicActual!!)

        createNotification(musicActual!!)
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mainStruct.logs!!.add("Erro: $what")
        Log.d("Exceção", "Erro ao executar o áudio")
        return true
    }

    private fun addRecent(playlistId: Int, music: Music){
        val musicRecent = MusicRecent(playlistId, music.filePath, music.fileName)
        val i = mainStruct.recents!!.indexOf(musicRecent)
        if(i in (0..< mainStruct.recents!!.size)){
            mainStruct.recents!!.removeAt(i)
        }
        mainStruct.recents!!.addFirst(musicRecent)
        mainStruct.saveRecentsToFile(dataDir.path)
    }

    fun play(){
        if(mediaPlayer != null){
            if(audioFocusRequest == null){
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(audioAttributes!!)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(this)
                    .build()
            }
            val mFocusLock = Any()
            // requesting audio focus
            val res: Int = audioManager.requestAudioFocus(audioFocusRequest!!)
            synchronized(mFocusLock) {
                when(res) {
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED,
                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                        mediaPlayer!!.start()
                        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                    }
                    else -> {
                        mainStruct.logs!!.add("Não foi possível requisitar o foco de áudio ${musicActual!!.fileName}")
                    }
                }
            }
        }
    }

    fun pause(){
        if(mediaPlayer != null){
            mediaPlayer!!.pause()
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    fun playPause(){
        if(mediaPlayer != null){
            if(mediaPlayer!!.isPlaying){
                pause()
            }else{
                play()
            }
        }
    }

    fun stop(){
        if(mediaPlayer != null){
            mediaPlayer!!.pause()
            mediaPlayer!!.seekTo(0)
            updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
        }
    }

    fun skipToNext(){
        updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)

        skipMusic(isNext = true)
    }

    fun skipToPrevious(){
        updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS)

        skipMusic(isPrevious = true)
    }

    private fun skipMusic(isNext: Boolean = false, isPrevious: Boolean = false){
        var music: Music? = null
        if(mainStruct.settings!!.isAsc){
            if(!playlistActual.isNullOrEmpty() && musicActual != null){
                val i = playlistActual!!.indexOf(musicActual)
                if(i in (0..< playlistActual!!.size)){
                    if(isNext){
                        music = playlistActual!![i+1]
                    }else if(isPrevious){
                        music = playlistActual!![i-1]
                    }
                }
            }
        }else if(mainStruct.settings!!.isDesc){
            if(!playlistActual.isNullOrEmpty() && musicActual != null){
                val i = playlistActual!!.indexOf(musicActual)
                if(i in (0..< playlistActual!!.size)){
                    if(isNext){
                        music = playlistActual!![i-1]
                    }else if(isPrevious){
                        music = playlistActual!![i+1]
                    }
                }
            }
        }else if(mainStruct.settings!!.isRepeat){
            music = musicActual
        }else if(mainStruct.settings!!.isRandomAll){
            var counter = 0
            music = mainStruct.musics!![(0..< mainStruct.musics!!.size).random()]
            while(music == musicActual ||
                (!mainStruct.settings!!.repeatAlreadyPlayedSongs || findMusicRecent(music!!))){
                counter++
                music = mainStruct.musics!![(0..< mainStruct.musics!!.size).random()]
                if(counter > 10){ // loop infinito
                    music = null
                    break
                }
            }
            playlistActual = null
        }else if(mainStruct.settings!!.isRandomPlaylist){
            if(!playlistActual.isNullOrEmpty()){
                var counter = 0
                music = playlistActual!![(0..< playlistActual!!.size).random()]
                while(musicActual!! == music ||
                    (!mainStruct.settings!!.repeatAlreadyPlayedSongs || findMusicRecent(music!!))){
                    counter++
                    music = playlistActual!![(0..< playlistActual!!.size).random()]
                    if(counter > 10){ // loop infinito
                        music = null
                        break
                    }
                }
            }
        }
        if(music != null){
            start(music)
        }
    }

    private fun findMusicRecent(music: Music): Boolean{
        for(musicRecent in mainStruct.recents!!){
            if(musicRecent.fileName == music.fileName){
                return true
            }
        }
        return false
    }

    fun seekTo(position: Long){
        if(mediaPlayer != null){
            mediaPlayer!!.seekTo(position.toInt())
            updatePlaybackState(PlaybackStateCompat.STATE_NONE)
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            this.description = CHANNEL_DESCRIPTION
        }
        // Register the channel with the system.
        if(notificationManager == null) {
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        notificationManager!!.createNotificationChannel(channel)
    }

    private fun createNotification(music: Music){
        createNotificationChannel()

        mainStruct.loadInformations(music)

        notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_small_icon)
            .setContentTitle(music.title)
            .setContentText(music.artist)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(sessionToken)
                .setShowActionsInCompactView(0)
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
            )
        if(music.art != null){
            notification.setLargeIcon(BitmapFactory.decodeByteArray(music.art, 0, music.art!!.size))
        }

        val metadataCompat = MediaMetadataCompat.Builder()
        metadataCompat.putString(MediaMetadataCompat.METADATA_KEY_TITLE, music.title)
        metadataCompat.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, music.artist)
        metadataCompat.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, music.album)
        metadataCompat.putString(MediaMetadataCompat.METADATA_KEY_GENRE, music.genre)
        if(music.year != null){
            metadataCompat.putLong(MediaMetadataCompat.METADATA_KEY_YEAR, music.year!!)
        }
        if(music.trackNumber != null){
            metadataCompat.putLong(MediaMetadataCompat.METADATA_KEY_YEAR, music.trackNumber!!)
        }
        metadataCompat.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, music.duration!!)
        if(music.art != null){
            metadataCompat.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeByteArray(music.art, 0, music.art!!.size))
            metadataCompat.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeByteArray(music.art, 0, music.art!!.size))
        }
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)

        session.setMetadata(metadataCompat.build())

        if(!session.isActive) {
            session.isActive = true
        }

        startForeground(NOTIFICATION_ID, notification.build())

        notificationManager!!.notify(NOTIFICATION_ID, notification.build())
    }

    private fun updatePlaybackState(state: Int){
        if(state != PlaybackStateCompat.STATE_NONE){
            playbackStateActual = state
        }
        session.setPlaybackState(PlaybackStateCompat.Builder()
            .setState(playbackStateActual, mediaPlayer!!.currentPosition.toLong(), 1f)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_SEEK_TO
            ).build())
    }


}