package com.automotivemusic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.mediaplayerdroid.R
import com.example.mediaplayerdroid.activitys.MainActivity

class FoldersFragment(
    private var fragmentManager: FragmentManager) : Fragment() {

    private lateinit var frameLayout: FrameLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        frameLayout = view.findViewById(R.id.flFrameLayout)

        val fragment = SubFolderFragment(fragmentManager, null, this)
        fragmentManager.beginTransaction().replace(R.id.flFrameLayout, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_folders, container, false)
    }

}