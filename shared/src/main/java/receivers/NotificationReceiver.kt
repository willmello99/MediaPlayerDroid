package receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.mediaplayerdroid.MediaService

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null && context != null) {
            val mediaServiceIntent = Intent(context, MediaService::class.java)
            mediaServiceIntent.putExtra(MediaService.MEDIA_COMMAND, intent.action)
            context.startForegroundService(mediaServiceIntent)
        }
    }
}