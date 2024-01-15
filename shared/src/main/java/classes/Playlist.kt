package classes

import java.io.Serializable

class Playlist(
    var id: Int,
    var name: String): Serializable{

    var idsMusics = ArrayList<Int>()

    override fun equals(other: Any?): Boolean {
        if(other == null){
            return false
        }
        if(other === this){
            return true
        }
        if(other !is Playlist){
            return false
        }
        return this.id == other.id
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    fun getMusic(idMusic: Int): Music?{
        return MainStruct.getUnique().musics!![idMusic]
    }
}