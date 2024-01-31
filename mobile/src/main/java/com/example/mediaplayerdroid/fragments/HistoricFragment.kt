package com.automotivemusic.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import classes.MainStruct
import classes.Music
import classes.Playlist
import classes.PlaylistMusic
import com.example.mediaplayerdroid.R
import com.example.mediaplayerdroid.classes.ListAdapterPlaylistsMusics
import com.example.mediaplayerdroid.shared.MediaService
import com.google.android.material.navigation.NavigationView

class HistoricFragment(
    private var fragmentManager: FragmentManager) : Fragment() {

    private lateinit var ivBackToolbar: ImageView
    private lateinit var lvItemsRecents: ListView
    private lateinit var btnClearListRecents: Button
    private lateinit var mainActivity: Activity
    private lateinit var mainStruct: MainStruct
    private lateinit var adapter: ListAdapterPlaylistsMusics
    private lateinit var handler: Handler
    private val taskUpdateChange = Runnable { updateChanged() }
    //private var counter: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //counter = 0
        mainStruct = MainStruct.getUnique()
        mainActivity = mainStruct.mainActivity!!
        mainActivity.title = "Histórico de músicas"
        handler = Handler(Looper.getMainLooper())

        ivBackToolbar = mainActivity.findViewById(R.id.ivBackToolbar)
        ivBackToolbar.isVisible = true
        ivBackToolbar.setOnClickListener {
            var lbFindFragment = false
            for(fragment in fragmentManager.fragments) {
                if (fragment !is HistoricFragment) {
                    // Volta para o último fragment aberto antes de abrir o histórico
                    if (fragment is SubFolderFragment) {
                        var prevSubFragment = fragmentManager.fragments[0] as SubFolderFragment
                        while (true) {
                            if (prevSubFragment.parent is SubFolderFragment) {
                                prevSubFragment = prevSubFragment.parent as SubFolderFragment
                            } else {
                                lbFindFragment = true
                                fragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, prevSubFragment.parent)
                                    .commit()
                                break
                            }
                        }
                    } else {
                        lbFindFragment = true
                        fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment).commit()
                    }
                }
            }
            if(!lbFindFragment){
                val menu = mainActivity.findViewById<NavigationView>(R.id.nav_view).menu
                menu.findItem(R.id.nav_home).isChecked = true
                menu.findItem(R.id.nav_folders).isChecked = false
                menu.findItem(R.id.nav_settings).isChecked = false
                menu.findItem(R.id.nav_historic).isChecked = false
                menu.findItem(R.id.nav_info).isChecked = false
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment()).commit()
            }
        }

        lvItemsRecents = view.findViewById(R.id.lvItemsRecents)
        adapter = ListAdapterPlaylistsMusics(requireContext(), mainStruct.recents!!)
        lvItemsRecents.adapter = adapter
        lvItemsRecents.setOnItemClickListener { parent, view, position, id ->
            val element = lvItemsRecents.adapter.getItem(position) // The item that was clicked

            if(element is PlaylistMusic){
                // Clicou em uma música
                val intent = Intent(requireContext(), MediaService::class.java)

                // Inicia a próxima reprodução
                intent.putExtra(MediaService.MEDIA_COMMAND, MediaService.MEDIA_START)
                intent.putExtra(MediaService.MEDIA_PLAYLIST_ID, element.idPlaylist)
                intent.putExtra(MediaService.MEDIA_MUSIC_ID, element.idMusic)
                requireContext().startForegroundService(intent)
                handler.postDelayed(taskUpdateChange, 1000)
            }
        }

        btnClearListRecents = view.findViewById(R.id.btnClearListRecents)
        btnClearListRecents.setOnClickListener{
            if(mainStruct.recents!!.isEmpty()){
                Toast.makeText(requireContext(), "A lista de músicas recentes está vazia", Toast.LENGTH_LONG).show()
            }else{
                mainStruct.recents!!.clear()
                adapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "A lista de músicas recentes foi limpa", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun updateChanged() {
        val lock = Any()
        synchronized(lock){
            adapter.notifyDataSetChanged()
            //if(counter < 3) {
                //counter++
                //handler.postDelayed(taskUpdateChange, 1000)
            //}
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_historic, container, false)
    }


}