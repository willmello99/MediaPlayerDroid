package com.automotivemusic.fragments

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import classes.MainStruct
import com.example.mediaplayerdroid.R
import com.google.android.material.navigation.NavigationView

class SettingsFragment(
    private var fragmentManager: FragmentManager) : Fragment() {

    private lateinit var ivBackToolbar: ImageView
    private lateinit var cbRepeatAlreadyMusics: CheckBox
    private lateinit var mainStruct: MainStruct
    private lateinit var mainActivity: Activity


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainStruct = MainStruct.getUnique()
        mainActivity = mainStruct.mainActivity!!
        mainActivity.title = "Configurações"

        ivBackToolbar = mainActivity.findViewById(R.id.ivBackToolbar)
        ivBackToolbar.isVisible = true
        ivBackToolbar.setOnClickListener {
            var lbFindFragment = false
            for(fragment in fragmentManager.fragments) {
                if (fragment !is SettingsFragment) {
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

        cbRepeatAlreadyMusics = view.findViewById(R.id.cbRepeatAlreadyMusics)
        cbRepeatAlreadyMusics.isChecked = mainStruct.settings!!.repeatAlreadyPlayedSongs
        cbRepeatAlreadyMusics.setOnClickListener {
            mainStruct.settings!!.repeatAlreadyPlayedSongs = cbRepeatAlreadyMusics.isChecked
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

}