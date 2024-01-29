package com.automotivemusic.fragments

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.session.PlaybackState
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.view.isVisible
import classes.MainStruct
import classes.Music
import classes.PlaylistMusic
import com.example.mediaplayerdroid.R
import com.example.mediaplayerdroid.classes.Utils
import com.example.mediaplayerdroid.shared.MediaService

class HomeFragment : androidx.fragment.app.Fragment() {

    // Components
    private lateinit var toolbar: Toolbar
    private lateinit var ivBackToolbar: ImageView
    private lateinit var ivAlbumArt: ImageView

    private lateinit var tvPlaylistName: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var tvTitleAlbum: TextView
    private lateinit var tvGenre: TextView

    private lateinit var tvCurrentTime: TextView
    private lateinit var sbProgressMusic: SeekBar
    private lateinit var tvTotalTime: TextView

    private lateinit var ivSkipPrevious: ImageView
    private lateinit var ivStop: ImageView
    private lateinit var ivPlayPause: ImageView
    private lateinit var ivSkipNext: ImageView
    private lateinit var ivPlaybackMode: ImageView

    // Objects
    private lateinit var mainActivity: Activity
    private lateinit var mainContext: Context
    private var seekto: Boolean = false
    private lateinit var mainStruct: MainStruct
    private lateinit var handler: Handler
    private var oldMusic: Music? = null

    // Tasks
    private val taskRefreshViwer = Runnable { refreshViwer() }
    private val taskEnableSkipPreviousButton = Runnable { ivSkipPrevious.isEnabled = true }
    private val taskEnableStopButton = Runnable { ivStop.isEnabled = true }
    private val taskEnablePlayPauseButton = Runnable { ivPlayPause.isEnabled = true }
    private val taskEnableSkipNextButton = Runnable { ivSkipNext.isEnabled = true }
    private val taskEnablePlaybackModeButton = Runnable { ivPlaybackMode.isEnabled = true }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainStruct = MainStruct.getUnique()
        mainActivity = mainStruct.mainActivity!!

        handler = Handler(Looper.getMainLooper())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureVisualHome()
        configureVisualComponents(view)
        configureProgressMusic()
        configureSkipPrevious()
        configureStop()
        configurePlayPause()
        configureSkipNext()
        configurePlaybackMode()

        configureObjects()

        refreshViwer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private fun configureVisualHome(){
        mainActivity.title = getString(R.string.app_name)
        toolbar = mainActivity.findViewById(R.id.toolbar)
        ivBackToolbar = mainActivity.findViewById(R.id.ivBackToolbar)
        ivBackToolbar.isVisible = false
    }

    private fun configureVisualComponents(view: View){
        ivAlbumArt = view.findViewById(R.id.ivAlbumArt)

        tvPlaylistName = view.findViewById(R.id.tvPlaylistName)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvArtist = view.findViewById(R.id.tvArtist)
        tvTitleAlbum = view.findViewById(R.id.tvTitleAlbum)
        tvGenre = view.findViewById(R.id.tvGenre)

        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)
        sbProgressMusic = view.findViewById(R.id.sbProgressMusic)
        tvTotalTime = view.findViewById(R.id.tvTotalTime)

        ivSkipPrevious = view.findViewById(R.id.ivSkipPrevious)
        ivStop = view.findViewById(R.id.ivStop)
        ivPlayPause = view.findViewById(R.id.ivPlayPause)
        ivSkipNext = view.findViewById(R.id.ivSkipNext)
        ivPlaybackMode = view.findViewById(R.id.ivPlaybackMode)
    }

    private fun configureProgressMusic(){
        sbProgressMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser)
                    seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }

            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })
    }

    private fun configureSkipPrevious(){
        ivSkipPrevious.setOnClickListener {
            ivSkipPrevious.isEnabled = false

            val intent = Intent(requireContext(), MediaService::class.java)
            intent.putExtra(MediaService.MEDIA_COMMAND, MediaService.MEDIA_SKIP_TO_PREVIOUS)
            mainContext.startForegroundService(intent)
            handler.postDelayed(taskEnableSkipPreviousButton, 750)
        }
    }

    private fun configureStop(){
        ivStop.setOnClickListener {
            ivStop.isEnabled = false
            val intent = Intent(requireContext(), MediaService::class.java)
            intent.putExtra(MediaService.MEDIA_COMMAND, MediaService.MEDIA_STOP)
            mainContext.startForegroundService(intent)
            handler.postDelayed(taskEnableStopButton, 750)
        }
    }

    private fun configurePlayPause(){
        ivPlayPause.setOnClickListener {
            ivPlayPause.isEnabled = false
            val intent = Intent(requireContext(), MediaService::class.java)
            intent.putExtra(MediaService.MEDIA_COMMAND, MediaService.MEDIA_PLAY_PAUSE)
            mainContext.startForegroundService(intent)
            handler.postDelayed(taskEnablePlayPauseButton, 750)
        }
    }

    private fun configureSkipNext(){
        ivSkipNext.setOnClickListener {
            ivSkipNext.isEnabled = false
            val intent = Intent(requireContext(), MediaService::class.java)
            intent.putExtra(MediaService.MEDIA_COMMAND, MediaService.MEDIA_SKIP_TO_NEXT)
            mainContext.startForegroundService(intent)
            handler.postDelayed(taskEnableSkipNextButton, 750)
        }
    }

    private fun configurePlaybackMode(){
        ivPlaybackMode.setOnClickListener {
            ivPlaybackMode.isEnabled = false
            val intent = Intent(requireContext(), MediaService::class.java)
            if(MainStruct.getUnique().settings!!.isAsc){
                intent.putExtra(MediaService.MEDIA_COMMAND, MediaService.MEDIA_PLAYBACK_ISDESC)
            }else if(MainStruct.getUnique().settings!!.isDesc){
                intent.putExtra(MediaService.MEDIA_COMMAND, MediaService.MEDIA_PLAYBACK_ISREPEAT)
            }else if(MainStruct.getUnique().settings!!.isRepeat){
                intent.putExtra(MediaService.MEDIA_COMMAND, MediaService.MEDIA_PLAYBACK_ISRANDOMPLAYLIST)
            }else if(MainStruct.getUnique().settings!!.isRandomPlaylist){
                intent.putExtra(MediaService.MEDIA_COMMAND, MediaService.MEDIA_PLAYBACK_ISRANDOMALL)
            }else{
                intent.putExtra(MediaService.MEDIA_COMMAND, MediaService.MEDIA_PLAYBACK_ISASC)
            }
            mainContext.startForegroundService(intent)

            refreshButtons()

            handler.postDelayed(taskEnablePlaybackModeButton, 750)
        }
    }

    private fun configureObjects(){
        mainContext = requireContext()
    }

    private fun seekTo(position: Int){
        seekto = true
        val intent = Intent(requireContext(), MediaService::class.java)
        intent.putExtra(MediaService.MEDIA_COMMAND, MediaService.MEDIA_SEEK_TO)
        intent.putExtra(MediaService.MEDIA_SEEK_TO_POSITION, position)

        mainContext.startForegroundService(intent)
    }

    private fun refreshViwer(){
        val lastPlaylistMusic = MainStruct.getUnique().lastPlaylistMusic
        if(lastPlaylistMusic != null){
            if(!seekto) {
                refreshMainInfo(PlaylistMusic(lastPlaylistMusic.idPlaylist, lastPlaylistMusic.idMusic))
                refreshSeekBar(lastPlaylistMusic.position.toInt())
                refreshButtons()
                handler.postDelayed(taskRefreshViwer, 1000)
            }else{
                seekto = false
                handler.postDelayed(taskRefreshViwer, 300)
            }
        }else{
            handler.postDelayed(taskRefreshViwer, 1000)
        }
    }

    private fun refreshMainInfo(playlistMusic: PlaylistMusic){
        val music = mainStruct.musics!![playlistMusic.idMusic]!!
        if(oldMusic != music) {
            val playlist = mainStruct.playlists!![playlistMusic.idPlaylist]!!
            tvPlaylistName.text = playlist.name
            if (music.title.isNullOrEmpty()) {
                tvTitle.text = getString(R.string.NO_TITLE_INFORMATION)
            } else {
                tvTitle.text = music.title
            }
            if (music.artist.isNullOrEmpty()) {
                tvArtist.text = getString(R.string.NO_ARTIST_INFORMATION)
            } else {
                tvArtist.text = music.artist
            }
            if (music.album.isNullOrEmpty()) {
                tvTitleAlbum.text = getString(R.string.NO_ALBUM_INFORMATION)
            } else {
                tvTitleAlbum.text = music.album
            }
            if (music.genre.isNullOrEmpty()) {
                tvGenre.text = getString(R.string.NO_GENRE_INFORMATION)
            } else {
                tvGenre.text = music.genre
            }

            if (music.art != null) {
                ivAlbumArt.setImageBitmap(BitmapFactory.decodeByteArray(music.art, 0, music.art!!.size))
            } else {
                ivAlbumArt.setImageResource(R.drawable.music)
            }

            if (sbProgressMusic.max != music.duration!!.toInt()) {
                sbProgressMusic.max = music.duration!!.toInt()
                tvTotalTime.text = Utils.getTimeString(music.duration!!.toInt())
            }
            oldMusic = music
        }
    }

    private fun refreshSeekBar(currentProgress: Int){
        if(currentProgress != sbProgressMusic.progress){
            sbProgressMusic.progress = currentProgress
            tvCurrentTime.text = Utils.getTimeString(currentProgress)
        }
        sbProgressMusic.isEnabled = currentProgress > 0
    }

    private fun refreshButtons(){
        if(mainStruct.playbackState == PlaybackState.STATE_PAUSED || mainStruct.playbackState == PlaybackState.STATE_STOPPED){
            ivPlayPause.setImageResource(R.drawable.play_45)
        }else{
            ivPlayPause.setImageResource(R.drawable.pause_45)
        }

        if(mainStruct.settings!!.isAsc){
            ivPlaybackMode.setImageResource(R.drawable.sortaz_38)
        }else if(mainStruct.settings!!.isDesc){
            ivPlaybackMode.setImageResource(R.drawable.sortza_38)
        }else if(mainStruct.settings!!.isRepeat){
            ivPlaybackMode.setImageResource(R.drawable.repeat_38)
        }else if(mainStruct.settings!!.isRandomPlaylist){
            ivPlaybackMode.setImageResource(R.drawable.randomfolder_38)
        }else if(mainStruct.settings!!.isRandomAll){
            ivPlaybackMode.setImageResource(R.drawable.randomall_38)
        }
    }

}