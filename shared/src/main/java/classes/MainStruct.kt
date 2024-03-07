package classes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.LinkedList
import kotlin.collections.LinkedHashMap

class MainStruct private constructor(): Serializable {

    companion object{
        const val FILE_MUSICS: String = "mediaMusics.trx"
        const val FILE_PLAYLISTS: String = "mediaPlaylists.trx"
        const val FILE_RECENTS: String = "mediaRecents.trx"
        const val FILE_LOG: String = "mediaLog.trx"
        const val FILE_SETTINGS: String = "mediaSettings.trx"
        const val FILE_LAST_PLAYLIST_MUSIC: String = "mediaLastPlaylistMusic.trx"


        @Volatile
        private var instance: MainStruct? = null

        fun getUnique() =
            instance ?: synchronized(this) {
                instance ?: MainStruct().also { instance = it }
            }
    }

    var musics: LinkedHashMap<Int, Music>? = null
    var playlists: LinkedHashMap<Int, Playlist>? = null
    var recents: LinkedList<PlaylistMusic>? = null
    var logs: ArrayList<String>? = null
    var settings: Settings? = null
    var lastPlaylistMusic: LastPlaylistMusic? = null
    var mainActivity: Activity? = null
    var mainIntent: Intent? = null

    var playbackState: Int = 0

    private var dataDir: String? = null

    fun reloadFromFiles(){
        if(musics != null){
            musics!!.clear()
        }
        if(playlists != null) {
            playlists!!.clear()
            loadPlaylistsFromFile(dataDir!!)
        }
        if(recents != null){
            recents!!.clear()
        }
        lastPlaylistMusic = null
    }

    fun loadFromFile(directory: String){
        dataDir = directory
        // Tem que carregar primeiro as configurações
        if(settings == null) {
            loadSettingsQuicknessFromFile(dataDir!!)
        }
        if(settings == null){
            settings = Settings()
            saveSettingsToFile(directory)
        }

        if(musics == null) { // Performance
            if(!settings!!.reloadSongsNextBoot)
                loadSongsQuicknessFromDirectory(dataDir!!)
            if(musics == null)
                musics = LinkedHashMap()
        }
        if(playlists == null) { // Performance
            if (!settings!!.reloadSongsNextBoot) {
                loadPlaylistQuicknessFromDirectory(dataDir!!)
            }
        }
        if(recents == null) { // Performance
            loadRecentsQuicknessFromFile(dataDir!!)
        }
        if(recents == null){
            recents = LinkedList()
            saveRecentsToFile(directory)
        }
        if(logs == null) { // Performance
            loadLogQuicknessFromFile(dataDir!!)
        }
        if(logs == null){
            logs = ArrayList()
            saveLogToFile(directory)
        }
        if(lastPlaylistMusic == null) {
            loadLastQuicknessMusic(dataDir!!)
        }
    }

    private fun loadSongsQuicknessFromDirectory(directory: String){
        val fileMusics = File("$directory/$FILE_MUSICS")
        if (fileMusics.exists()) {
            val fis = FileInputStream(fileMusics)
            val osi = ObjectInputStream(fis)
            try {
                @Suppress("UNCHECKED_CAST")
                musics = osi.readObject() as? LinkedHashMap<Int, Music>
            } catch (_: Exception) {
                fileMusics.delete()
            }
            osi.close()
            fis.close()
        }
    }

    private fun loadPlaylistQuicknessFromDirectory(directory: String){
        val filePlaylists = File("$directory/$FILE_PLAYLISTS")
        if (filePlaylists.exists()) {
            val fis = FileInputStream(filePlaylists)
            val osi = ObjectInputStream(fis)
            try {
                @Suppress("UNCHECKED_CAST")
                playlists = osi.readObject() as LinkedHashMap<Int, Playlist>
            } catch (_: Exception) {
                filePlaylists.delete()
                loadPlaylistsFromFile(directory)
            }
            osi.close()
            fis.close()
        }else{
            loadPlaylistsFromFile(directory)
        }
    }

    private fun loadRecentsQuicknessFromFile(directory: String){
        val fileRecents = File("$directory/$FILE_RECENTS")
        if (fileRecents.exists()) {
            val fis = FileInputStream(fileRecents)
            val osi = ObjectInputStream(fis)
            try {
                @Suppress("UNCHECKED_CAST")
                recents = osi.readObject() as LinkedList<PlaylistMusic>
            } catch (_: Exception) {
                fileRecents.delete()
            }
            osi.close()
            fis.close()
        }
    }

    private fun loadLogQuicknessFromFile(directory: String){
        val fileLog = File("$directory/$FILE_LOG")
        if (fileLog.exists()) {
            val fis = FileInputStream(fileLog)
            val osi = ObjectInputStream(fis)
            try {
                @Suppress("UNCHECKED_CAST")
                logs = osi.readObject() as ArrayList<String>
            } catch (_: Exception) {
                fileLog.delete()
            }
            osi.close()
            fis.close()
        }
    }

    private fun loadSettingsQuicknessFromFile(directory: String){
        val fileSettings = File("$directory/$FILE_SETTINGS")
        if (fileSettings.exists()) {
            val fis = FileInputStream(fileSettings)
            val osi = ObjectInputStream(fis)
            try {
                settings = osi.readObject() as Settings
            } catch (_: Exception) {
                fileSettings.delete()
            }
            osi.close()
            fis.close()
        }
    }

    private fun loadLastQuicknessMusic(directory: String){
        val fileLastMusic = File("$directory/$FILE_LAST_PLAYLIST_MUSIC")
        if (fileLastMusic.exists()) {
            val fis = FileInputStream(fileLastMusic)
            val osi = ObjectInputStream(fis)
            try {
                lastPlaylistMusic = osi.readObject() as LastPlaylistMusic
            } catch (_: Exception) {
                fileLastMusic.delete()
            }
            osi.close()
            fis.close()
        }
    }

    private fun loadPlaylistsFromFile(directory: String) {
        if (playlists == null) {
            playlists = LinkedHashMap()
        }
        if (playlists!!.isEmpty()) {
            val folders = File("/storage/emulated/0/Músicas")
            if (folders.listFiles() != null) {
                var idMusic = 0
                var idPlaylist = 0
                for (folder in folders.listFiles()!!) {
                    if (folder.listFiles() != null) {
                        val playlist = Playlist(idPlaylist, folder.name)
                        for (file in folder.listFiles()!!) {
                            if (file.isFile) {
                                var music = Music(idMusic, file.absolutePath, file.name)

                                val i = musics!!.values.indexOf(music)
                                if (i > -1) {
                                    music = musics!![i]!!
                                } else {
                                    musics!![idMusic] = music
                                    idMusic++
                                }
                                playlist.idsMusics.add(music.id)
                            }
                        }
                        if (playlist.idsMusics.isNotEmpty()) {
                            playlists!![idPlaylist] = playlist
                            //playlist.idsMusics.sortBy { musics!![it]!!.fileName }
                            idPlaylist++
                        }
                    }
                }
                //playlists!!.toList().sortedBy { it.second.name }
            }

            saveMusicsToFile(directory)
            savePlaylistsToFile(directory)
        }
    }

    fun saveToFile(directory: String){
        dataDir = directory

        saveSettingsToFile(directory)
        //saveMusicsToFile(directory) Essa lista nunca vai sofrer alteração
        savePlaylistsToFile(directory)
        saveRecentsToFile(directory)
        saveLogToFile(directory)
        saveLastPlaylistMusic(directory)
    }

    private fun saveMusicsToFile(directory: String? = dataDir){
        val fos = FileOutputStream(File("$directory/$FILE_MUSICS"))
        val os = ObjectOutputStream(fos)
        os.writeObject(musics)
        os.close()
        fos.close()
    }

    private fun savePlaylistsToFile(directory: String? = dataDir){
        val fos = FileOutputStream(File("$directory/$FILE_PLAYLISTS"))
        val os = ObjectOutputStream(fos)
        os.writeObject(playlists)
        os.close()
        fos.close()
    }

    fun saveRecentsToFile(directory: String? = dataDir){
        val fos = FileOutputStream(File("$directory/$FILE_RECENTS"))
        val os = ObjectOutputStream(fos)
        os.writeObject(recents)
        os.close()
        fos.close()
    }

    fun saveLogToFile(directory: String? = dataDir){
        val fos = FileOutputStream(File("$directory/$FILE_LOG"))
        val os = ObjectOutputStream(fos)
        os.writeObject(logs)
        os.close()
        fos.close()
    }

    private fun saveSettingsToFile(directory: String? = dataDir){
        val fos = FileOutputStream(File("$directory/$FILE_SETTINGS"))
        val os = ObjectOutputStream(fos)
        os.writeObject(settings)
        os.close()
        fos.close()
    }

    fun saveLastPlaylistMusic(directory: String? = dataDir){
        if(lastPlaylistMusic != null) {
            val fos = FileOutputStream(File("$directory/$FILE_LAST_PLAYLIST_MUSIC"))
            val os = ObjectOutputStream(fos)
            os.writeObject(lastPlaylistMusic)
            os.close()
            fos.close()
        }
    }
}