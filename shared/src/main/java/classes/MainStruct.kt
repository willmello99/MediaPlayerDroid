package classes

import android.media.MediaMetadataRetriever
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.time.LocalDateTime
import java.util.LinkedList

class MainStruct private constructor(): Serializable {

    companion object{
        const val FILE_MUSICS: String = "mediaMusics.trx"
        const val FILE_PLAYLISTS: String = "mediaPlaylists.trx"
        const val FILE_RECENTS: String = "mediaRecents.trx"
        const val FILE_LOG: String = "mediaLog.trx"
        const val FILE_SETTINGS: String = "mediaSettings.trx"

        @Volatile
        private var instance: MainStruct? = null

        fun getUnique() =
            instance ?: synchronized(this) {
                instance ?: MainStruct().also { instance = it }
            }
    }

    var musics: ArrayList<Music>? = null
    var playlists: ArrayList<Playlist>? = null
    var recents: LinkedList<MusicRecent>? = null
    var logs: ArrayList<String>? = null
    var settings: Settings? = null

    private val mmr = MediaMetadataRetriever()
    private var dataDir: String? = null

    fun loadFromFile(directory: String){
        dataDir = directory

        loadMusicsFromFile(directory)
        loadPlaylistsFromFile(directory)
        loadRecentsFromFile(directory)
        loadLogFromFile(directory)
        loadSettingsFromFile(directory)
    }

    fun loadMusicsFromFile(directory: String){
        if(musics == null) { // Performance
            val fileMusics = File("$directory/$FILE_MUSICS")
            if (fileMusics.exists()) {
                val fis = FileInputStream(fileMusics)
                val osi = ObjectInputStream(fis)
                try {
                    musics = osi.readObject() as ArrayList<Music>
                } catch (_: Exception) {
                    fileMusics.delete()
                }
                osi.close()
                fis.close()
            }
            if(musics == null){
                musics = ArrayList()
            }
            if (musics!!.isEmpty()) {
                val folders = File("/storage/emulated/0/Músicas")
                if (folders.listFiles() != null) {
                    for (folder in folders.listFiles()!!) {
                        if (folder.listFiles() != null) {
                            for (file in folder.listFiles()!!) {
                                if (file.isFile) {
                                    musics!!.add(Music(file.absolutePath, file.name))
                                }
                            }
                        }
                    }
                }
                musics!!.sortBy { it.title + it.artist }
                saveMusicsToFile(directory)
            }
        }
    }

    fun loadPlaylistsFromFile(directory: String){
        if(playlists == null) { // Performance
            val filePlaylists = File("$directory/$FILE_PLAYLISTS")
            if (filePlaylists.exists()) {
                val fis = FileInputStream(filePlaylists)
                val osi = ObjectInputStream(fis)
                try {
                    playlists = osi.readObject() as ArrayList<Playlist>
                } catch (_: Exception) {
                    filePlaylists.delete()
                }
                osi.close()
                fis.close()
            }
            if(playlists == null){
                playlists = ArrayList()
            }
            if (playlists!!.isEmpty()) {
                var playlistId = 0
                val folders = File("/storage/emulated/0/Músicas")
                if (folders.listFiles() != null) {
                    for (folder in folders.listFiles()!!) {
                        if (folder.listFiles() != null) {
                            playlistId++
                            val playlist = Playlist(playlistId, folder.name)
                            for (file in folder.listFiles()!!) {
                                if (file.isFile) {
                                    var music = Music(file.absolutePath, file.name)
                                    val i = musics!!.indexOf(music)
                                    if (i > -1) {
                                        music = musics!![i]
                                    }
                                    playlist.add(music)
                                }
                            }
                            if (playlist.isNotEmpty()) {
                                playlists!!.add(playlist)
                            }
                        }
                    }
                }

                savePlaylistsToFile(directory)
            }
        }
    }

    fun loadRecentsFromFile(directory: String){
        if(recents == null) { // Performance
            val fileRecents = File("$directory/$FILE_RECENTS")
            if (fileRecents.exists()) {
                val fis = FileInputStream(fileRecents)
                val osi = ObjectInputStream(fis)
                try {
                    recents = osi.readObject() as LinkedList<MusicRecent>
                } catch (_: Exception) {
                    fileRecents.delete()
                }
                osi.close()
                fis.close()
            }
            if(recents == null){
                recents = LinkedList()
                saveRecentsToFile(directory)
            }
        }
    }

    fun loadLogFromFile(directory: String){
        if(logs == null) { // Performance
            val fileLog = File("$directory/$FILE_LOG")
            if (fileLog.exists()) {
                val fis = FileInputStream(fileLog)
                val osi = ObjectInputStream(fis)
                try {
                    logs = osi.readObject() as ArrayList<String>
                } catch (_: Exception) {
                    fileLog.delete()
                }
                osi.close()
                fis.close()
            }
            if(logs == null){
                logs = ArrayList()
                saveLogToFile(directory)
            }
        }
    }

    fun loadSettingsFromFile(directory: String){
        if(settings == null) {
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
            if(settings == null){
                settings = Settings()
                saveSettingsToFile(directory)
            }
        }
    }

    fun saveToFile(directory: String){
        dataDir = directory

        //saveMusicsToFile(directory) Essa lista nunca vai sofrer alteração
        savePlaylistsToFile(directory)
        saveRecentsToFile(directory)
        saveLogToFile(directory)
        saveSettingsToFile(directory)
    }

    fun saveMusicsToFile(directory: String){
        val fos = FileOutputStream(File("$directory/$FILE_MUSICS"))
        val os = ObjectOutputStream(fos)
        os.writeObject(musics)
        os.close()
        fos.close()
    }

    fun savePlaylistsToFile(directory: String){
        val fos = FileOutputStream(File("$directory/$FILE_PLAYLISTS"))
        val os = ObjectOutputStream(fos)
        os.writeObject(playlists)
        os.close()
        fos.close()
    }

    fun saveRecentsToFile(directory: String){
        val fos = FileOutputStream(File("$directory/$FILE_RECENTS"))
        val os = ObjectOutputStream(fos)
        os.writeObject(recents)
        os.close()
        fos.close()
    }

    fun saveLogToFile(directory: String){
        val fos = FileOutputStream(File("$directory/$FILE_LOG"))
        val os = ObjectOutputStream(fos)
        os.writeObject(logs)
        os.close()
        fos.close()
    }

    fun saveSettingsToFile(directory: String){
        val fos = FileOutputStream(File("$directory/$FILE_SETTINGS"))
        val os = ObjectOutputStream(fos)
        os.writeObject(settings)
        os.close()
        fos.close()
    }

    fun loadInformations(music: Music){
        if(music.title.isNullOrEmpty()){

            try{
                var gerouErro= false
                mmr.setDataSource(music.filePath)

                try {
                    music.title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                }catch(e:Exception){
                    gerouErro = true
                    logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) do arquivo ${music.filePath} - ${e.message}")
                }
                try{
                    music.artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                }catch(e:Exception){
                    gerouErro = true
                    logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) do arquivo ${music.filePath} - ${e.message}")
                }
                try{
                    music.album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                }catch(e:Exception){
                    gerouErro = true
                    logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) do arquivo ${music.filePath} - ${e.message}")
                }
                try{
                    music.genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                }catch(e:Exception){
                    gerouErro = true
                    logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) do arquivo ${music.filePath} - ${e.message}")
                }
                try{
                    val yearStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                    if(yearStr != null){
                        music.year = yearStr.toLong()
                    }
                }catch(e:Exception){
                    gerouErro = true
                    logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR) do arquivo ${music.filePath} - ${e.message}")
                }
                try{
                    val trackNumberStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                    if(trackNumberStr != null){
                        music.trackNumber = trackNumberStr.toLong()
                    }
                }catch(e:Exception){
                    gerouErro = true
                    logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER) do arquivo ${music.filePath} - ${e.message}")
                }
                try{
                    music.duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                }catch(e:Exception){
                    gerouErro = true
                    logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) do arquivo ${music.filePath} - ${e.message}")
                }
                try{
                    music.art = mmr.embeddedPicture
                }catch(e:Exception){
                    gerouErro = true
                    logs!!.add("${LocalDateTime.now()}: Erro em mmr.embeddedPicture do arquivo ${music.filePath} - ${e.message}")
                }

                if(gerouErro){
                    saveLogToFile(dataDir!!)
                }
            }catch (e:Exception){
                logs!!.add("${LocalDateTime.now()}: Erro em mmr.setDataSource(${music.filePath}) - ${e.message}")
                saveLogToFile(dataDir!!)
            }
        }
    }

}