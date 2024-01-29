package com.automotivemusic.classes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.mediaplayerdroid.R
import classes.Music
import java.util.LinkedList

class ListAdapterMusics(context: Context, dataArrayList: LinkedList<Music>):
    ArrayAdapter<Music>(context, R.layout.list_item, dataArrayList){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val listData = getItem(position)
        val view =
            convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)

        val listName = view!!.findViewById<TextView>(R.id.tvTextItem)
        listName.text = listData!!.fileName

        return view
    }
}