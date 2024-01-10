package classes

import java.io.Serializable

open class Music(
    var filePath: String,
    var fileName: String): Serializable {

    var title: String? = null
    var artist: String? = null
    var album: String? = null
    var genre: String? = null
    var year: Long? = null
    var trackNumber: Long? = null
    var duration: Long? = null
    var art: ByteArray? = null

    override fun equals(other: Any?): Boolean {
        if(other === null){
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

}