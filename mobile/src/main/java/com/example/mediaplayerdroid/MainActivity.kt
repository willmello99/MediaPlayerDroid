package com.example.mediaplayerdroid

import android.content.Intent
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.mediaplayerdroid.shared.MediaService

class MainActivity : AppCompatActivity() {

    private var mainIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(mainIntent == null){
            mainIntent = Intent(this, MediaService::class.java)
            mainIntent!!.putExtra("COMMAND", "START")
            //"/storage/emulated/0/Músicas/NewRetroWave/Celestial - Femmepop & Kalax.mp3"
            mainIntent!!.putExtra("MUSIC", "Celestial - Femmepop & Kalax.mp3")
            mainIntent!!.putExtra(MediaService.MEDIA_PLAYLIST_ID, 1)
            startForegroundService(mainIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mainIntent != null) {
            stopService(mainIntent)
        }
    }

}