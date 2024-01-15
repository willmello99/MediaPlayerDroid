package classes

import java.io.Serializable

class PlaylistMusic(
    var idPlaylist: Int,
    var idMusic: Int): Serializable {
    override fun equals(other: Any?): Boolean {
        if(other == null){
            return false
        }
        if(other === this){
            return true
        }
        if(other !is PlaylistMusic){
            return false
        }
        return this.idPlaylist == other.idPlaylist && this.idMusic == other.idMusic
    }

    override fun hashCode(): Int {
        var result = idPlaylist.hashCode()
        result = 31 * result + idMusic
        return result
    }

    fun getMusic(): Music?{
        return MainStruct.getUnique().musics!![idMusic]
    }

    fun getPlaylist(): Playlist?{
        return MainStruct.getUnique().playlists!![idPlaylist]
    }
}