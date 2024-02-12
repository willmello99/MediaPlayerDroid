package com.example.mediaplayerdroid.activitys

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import classes.MainStruct
import com.example.mediaplayerdroid.R

class LoadingActivity : AppCompatActivity() {

    private lateinit var ivLogo: ImageView
    private lateinit var tvLoading: TextView
    private var stopTimer: Boolean = false
    private lateinit var handler: Handler
    private val taskUpdateTextViewLoading = Runnable { updateTextViewLoading() }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var enableStart = true
            for(perm in permissions.entries){
                if(!perm.value){
                    Toast.makeText(this,
                        getString(R.string.PERMISSIONS_NOT_GRANTED_SHUTDOWN_APP), Toast.LENGTH_LONG).show()
                    finish()
                    enableStart = false
                    break
                }
            }
            if(enableStart) {
                // Se chegar aqui é por que todas as pemissões foram concedidas
                startApplication()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        handler = Handler(Looper.getMainLooper())

        val permissions = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.POST_NOTIFICATIONS,
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.BLUETOOTH_CONNECT)
        }else{
            arrayOf(android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.BLUETOOTH_CONNECT)
        }
        requestMultiplePermissions.launch(permissions)
    }

    private fun startApplication(){
        ivLogo = findViewById(R.id.ivLogo)
        tvLoading = findViewById(R.id.tvLoading)

        val rotate = RotateAnimation(
            0f,
            360f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = 5000
        rotate.repeatCount = 120 // Equivale a girar por 10 minutos
        rotate.interpolator = LinearInterpolator()

        ivLogo.startAnimation(rotate)

        updateTextViewLoading()

        loadMusics()
    }

    private fun updateTextViewLoading(){
        if(!stopTimer) {
            if (tvLoading.text != "Carregando....") {
                tvLoading.text = buildString {
                        append(tvLoading.text.toString())
                        append(".")
                    }
            } else {
                tvLoading.text = getString(R.string.LOADING)
            }
            handler.postDelayed(taskUpdateTextViewLoading, 750)
        }
    }


    private fun loadMusics(){
        // Inicia um serviço em segundo plano para evitar ANR
        //val intent = Intent(this, MultimediaBrowserService::class.java)
        //intent.putExtra(getString(com.automotivemusic.shared.R.string.MultimediaCommand), MultimediaCommands.mcLoadMusics)
        //startForegroundService(intent)
        Thread {
            MainStruct.getUnique().loadFromFile(dataDir.path)
            openMainActivity()
        }.start()
    }

    private fun openMainActivity(){
        ivLogo.clearAnimation()
        val intentStartMainActivity = Intent(this, MainActivity::class.java)
        startActivity(intentStartMainActivity)
        finish()
    }

}
