package classes

import android.media.MediaMetadataRetriever
import java.io.Serializable
import java.time.LocalDateTime

open class Music(
    var id: Int = 0,
    var filePath: String = "",
    var fileName: String = ""): Serializable {

    var title: String? = null
    var artist: String? = null
    var album: String? = null
    var genre: String? = null
    var year: Long? = null
    var trackNumber: Long? = null
    var duration: Long? = null
    var art: ByteArray? = null

    override fun equals(other: Any?): Boolean {
        if(other == null){
            return false
        }
        if(other === this){
            return true
        }
        if(other !is Music){
            return false
        }
        return this.fileName == other.fileName
    }

    override fun hashCode(): Int {
        var result = filePath.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + (genre?.hashCode() ?: 0)
        result = 31 * result + (year?.hashCode() ?: 0)
        result = 31 * result + (trackNumber?.hashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + (art?.contentHashCode() ?: 0)
        return result
    }

    fun loadInformations(){
        if(title.isNullOrEmpty()){
            val mmr = MediaMetadataRetriever()
            val mainStruct = MainStruct.getUnique()
            try{
                var gerouErro= false
                mmr.setDataSource(filePath)

                try {
                    title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                }catch(e:Exception){
                    gerouErro = true
                    mainStruct.logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) do arquivo $filePath - ${e.message}")
                }
                try{
                    artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                }catch(e:Exception){
                    gerouErro = true
                    mainStruct.logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) do arquivo $filePath - ${e.message}")
                }
                try{
                    album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                }catch(e:Exception){
                    gerouErro = true
                    mainStruct.logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) do arquivo $filePath - ${e.message}")
                }
                try{
                    genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                }catch(e:Exception){
                    gerouErro = true
                    mainStruct.logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) do arquivo $filePath - ${e.message}")
                }
                try{
                    val yearStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                    if(yearStr != null){
                        year = yearStr.toLong()
                    }
                }catch(e:Exception){
                    gerouErro = true
                    mainStruct.logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR) do arquivo $filePath - ${e.message}")
                }
                try{
                    val trackNumberStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                    if(trackNumberStr != null){
                        trackNumber = trackNumberStr.toLong()
                    }
                }catch(e:Exception){
                    gerouErro = true
                    mainStruct.logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER) do arquivo $filePath - ${e.message}")
                }
                try{
                    duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                }catch(e:Exception){
                    gerouErro = true
                    mainStruct.logs!!.add("${LocalDateTime.now()}: Erro em mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) do arquivo $filePath - ${e.message}")
                }
                try{
                    art = mmr.embeddedPicture
                }catch(e:Exception){
                    gerouErro = true
                    mainStruct.logs!!.add("${LocalDateTime.now()}: Erro em mmr.embeddedPicture do arquivo $filePath - ${e.message}")
                }

                if(gerouErro){
                    mainStruct.saveLogToFile()
                }
            }catch (e:Exception){
                mainStruct.logs!!.add("${LocalDateTime.now()}: Erro em mmr.setDataSource($filePath) - ${e.message}")
                mainStruct.saveLogToFile()
            }
        }
    }

}