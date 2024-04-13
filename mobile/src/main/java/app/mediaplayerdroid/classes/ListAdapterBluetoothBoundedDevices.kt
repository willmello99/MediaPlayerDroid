package app.mediaplayerdroid.classes

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.app.ActivityCompat
import app.mediaplayerdroid.R

class ListAdapterBluetoothBoundedDevices(context: Context, dataArrayList: ArrayList<BluetoothDevice>):
    ArrayAdapter<BluetoothDevice>(context, R.layout.list_item, dataArrayList){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val boundedDevice = getItem(position)
        val view =
            convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        val listName = view!!.findViewById<TextView>(R.id.tvTextItem)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            listName.text = "${boundedDevice!!.name}: ${boundedDevice.address}"
        }
        return view
    }
}
