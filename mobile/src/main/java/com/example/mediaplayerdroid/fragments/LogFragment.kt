package com.example.mediaplayerdroid.fragments

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import classes.MainStruct
import com.automotivemusic.fragments.HomeFragment
import com.automotivemusic.fragments.SubFolderFragment
import com.example.mediaplayerdroid.R
import com.example.mediaplayerdroid.classes.ListAdapterLogs
import com.google.android.material.navigation.NavigationView

class LogFragment(
    private var fragmentManager: FragmentManager
) : Fragment() {

    private lateinit var mainStruct: MainStruct
    private lateinit var mainActivity: Activity
    private lateinit var ivBackToolbar: ImageView
    private lateinit var lvLog: ListView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainStruct = MainStruct.getUnique()
        mainActivity = mainStruct.mainActivity!!
        mainActivity.title = "Log de erros"

        ivBackToolbar = mainActivity.findViewById(R.id.ivBackToolbar)
        ivBackToolbar.isVisible = true
        ivBackToolbar.setOnClickListener {
            var lbFindFragment = false
            for(fragment in fragmentManager.fragments) {
                if (fragment !is LogFragment) {
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
                menu.findItem(R.id.nav_log).isChecked = false
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment()).commit()
            }
        }

        lvLog = view.findViewById(R.id.lvLog)
        val logs = ArrayList<String>()
        for(log in MainStruct.getUnique().logs!!){
            logs.add(log)
        }
        lvLog.adapter = ListAdapterLogs(requireContext(), logs)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_log, container, false)
    }

}