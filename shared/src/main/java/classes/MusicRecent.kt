package classes

import java.io.Serializable

class MusicRecent(var playlistId: Int, filePath: String, fileName: String) : Music(filePath, fileName), Serializable {

    override fun equals(other: Any?): Boolean {
        if(other === null){
            return false
        }
        if(other === this){
            return true
        }
        if(other !is MusicRecent){
            return false
        }
        return this.playlistId == other.playlistId && this.fileName == other.fileName
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + playlistId
        return result
    }
}