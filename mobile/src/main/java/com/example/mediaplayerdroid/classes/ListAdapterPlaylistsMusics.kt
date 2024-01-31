package com.example.mediaplayerdroid.classes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import classes.MainStruct
import classes.PlaylistMusic
import com.example.mediaplayerdroid.R
import java.util.LinkedList

class ListAdapterPlaylistsMusics(context: Context, dataArrayList: LinkedList<PlaylistMusic>):
    ArrayAdapter<PlaylistMusic>(context, R.layout.list_item, dataArrayList){

    private val mainStruct = MainStruct.getUnique()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val listData = getItem(position)
        val view =
            convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        val listName = view!!.findViewById<TextView>(R.id.tvTextItem)
        val music = mainStruct.musics!![listData!!.idMusic]!!
        listName.text = music.fileName
        return view
    }
}