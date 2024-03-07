package classes

import java.io.Serializable

class Settings: Serializable {
    var isRandomPlaylist: Boolean = false
    var isRandomAll: Boolean = false
    var isAsc: Boolean = true
    var isDesc: Boolean = false
    var isRepeat: Boolean = false
    var repeatAlreadyPlayedSongs = true
    var reloadSongsNextBoot = false
}