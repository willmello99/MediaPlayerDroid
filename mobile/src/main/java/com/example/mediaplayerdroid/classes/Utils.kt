package com.example.mediaplayerdroid.classes

class Utils {

    companion object{

        fun getTimeString(millis: Int): String{
            val buf = StringBuffer()
            //val hours = (millis / (1000 * 60 * 60))
            val minutes = (millis % (1000 * 60 * 60) / (1000 * 60))
            val seconds = (millis % (1000 * 60 * 60) % (1000 * 60) / 1000)
            buf
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds))
            return buf.toString()
        }
    }
}