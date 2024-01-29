package com.automotivemusic.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import classes.MainStruct
import classes.Music
import classes.Playlist
import com.automotivemusic.classes.ListAdapterMusics
import com.automotivemusic.classes.ListAdapterPlaylists
import com.example.mediaplayerdroid.R
import com.example.mediaplayerdroid.shared.MediaService
import com.google.android.material.navigation.NavigationView
import java.util.LinkedList

class SubFolderFragment(
    private var fragmentManager: FragmentManager,
    private var playlist: Playlist?,
    private var parent: Fragment) : Fragment() {

    private lateinit var mainActivity: Activity
    private lateinit var mainStruct: MainStruct
    private lateinit var lvItems: ListView
    private lateinit var ivBackToolbar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivity = MainStruct.getUnique().mainActivity!!
        mainStruct = MainStruct.getUnique()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lvItems = view.findViewById(R.id.lvItems)

        ivBackToolbar = mainActivity.findViewById(R.id.ivBackToolbar)
        ivBackToolbar.isVisible = true
        lvItems.adapter = if(playlist != null){
            mainActivity.title = playlist!!.name
            ivBackToolbar.setOnClickListener {
                fragmentManager.beginTransaction().replace(R.id.flFrameLayout, parent).commit()
            }

            val musics = LinkedList<Music>()
            for(idMusic in playlist!!.idsMusics){
                musics.add(mainStruct.musics!![idMusic]!!)
            }
            musics.sortBy { it.fileName }
            ListAdapterMusics(requireContext(), musics)
        }else{
            mainActivity.title = "Músicas p/ pasta"
            ivBackToolbar.setOnClickListener {
                val menu = mainActivity.findViewById<NavigationView>(R.id.nav_view).menu
                menu.findItem(R.id.nav_home).isChecked = true
                menu.findItem(R.id.nav_folders).isChecked = false
                menu.findItem(R.id.nav_settings).isChecked = false
                menu.findItem(R.id.nav_historic).isChecked = false
                menu.findItem(R.id.nav_info).isChecked = false
                fragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
            }
            val playlists = LinkedList<Playlist>()
            for(entry in mainStruct.playlists!!.entries){
                playlists.add(entry.value)
            }
            playlists.sortBy { it.name }
            ListAdapterPlaylists(requireContext(), playlists)
        }

        lvItems.setOnItemClickListener { parent, view, position, id ->
            val element = lvItems.adapter.getItem(position) // The item that was clicked

            if(element is Playlist){
                // Clicou em uma pasta
                val fragment = SubFolderFragment(fragmentManager, element, this)
                fragmentManager.beginTransaction().replace(R.id.flFrameLayout, fragment).commit()
            }else if(element is Music){
                // Clicou em uma música
                val intent = Intent(requireContext(), MediaService::class.java)

                // Inicia a próxima reprodução
                intent.putExtra(MediaService.MEDIA_COMMAND, MediaService.MEDIA_START)
                intent.putExtra(MediaService.MEDIA_PLAYLIST_ID, playlist!!.id)
                intent.putExtra(MediaService.MEDIA_MUSIC_ID, element.id)
                requireContext().startForegroundService(intent)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sub_folder, container, false)
    }

}