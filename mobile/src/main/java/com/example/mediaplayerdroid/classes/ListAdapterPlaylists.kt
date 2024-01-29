package com.automotivemusic.classes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import classes.Playlist
import com.example.mediaplayerdroid.R
import java.util.LinkedList

class ListAdapterPlaylists(context: Context, dataArrayList: LinkedList<Playlist>):
    ArrayAdapter<Playlist>(context, R.layout.list_item, dataArrayList){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val listData = getItem(position)
        val view =
            convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        val listName = view!!.findViewById<TextView>(R.id.tvTextItem)
        listName.text = listData!!.name
        return view
    }
}