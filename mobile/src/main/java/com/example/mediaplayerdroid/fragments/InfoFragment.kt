package com.automotivemusic.fragments

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.pm.PackageInfoCompat
import com.example.mediaplayerdroid.R

class InfoFragment : Fragment() {

    private lateinit var tvVersionName: TextView
    private lateinit var tvVersionNumber: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvVersionName = view.findViewById(R.id.tvVersionName)
        tvVersionNumber = view.findViewById(R.id.tvVersionNumber)

        try{
            val packageManager = context?.packageManager!!
            val packageName = context?.packageName!!
            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            tvVersionName.text = "v ${packageInfo.versionName}"
            tvVersionNumber.text = PackageInfoCompat.getLongVersionCode(packageInfo).toString()
        }catch (e: Exception) {
            tvVersionName.text = "Versão desconhecida"
            tvVersionNumber.text = ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

}