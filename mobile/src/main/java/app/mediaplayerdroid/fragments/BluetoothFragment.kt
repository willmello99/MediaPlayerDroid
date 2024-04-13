package app.mediaplayerdroid.fragments

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import app.mediaplayerdroid.R
import app.mediaplayerdroid.classes.ListAdapterBluetoothBoundedDevices
import classes.MainStruct
import com.google.android.material.navigation.NavigationView

class BluetoothFragment(
    private var fragmentManager: FragmentManager
) : Fragment() {

    private lateinit var ivBackToolbar: ImageView
    private lateinit var lvBoundedDevices: ListView
    private lateinit var mainActivity: Activity
    private lateinit var mainStruct: MainStruct
    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null

    private val registerForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(requireContext(), "Bluetooth ativado", Toast.LENGTH_LONG).show()

            showBoundedDevices()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainStruct = MainStruct.getUnique()
        mainActivity = mainStruct.mainActivity!!
        mainActivity.title = "Dispositivos conectados"

        ivBackToolbar = mainActivity.findViewById(R.id.ivBackToolbar)
        ivBackToolbar.isVisible = true
        ivBackToolbar.setOnClickListener {
            var lbFindFragment = false
            for(fragment in fragmentManager.fragments) {
                if (fragment !is BluetoothFragment) {
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
                menu.findItem(R.id.nav_bluetooth).isChecked = false
                menu.findItem(R.id.nav_info).isChecked = false
                menu.findItem(R.id.nav_log).isChecked = false
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment()).commit()
            }
        }

        lvBoundedDevices = view.findViewById(R.id.lvBoundedDevices)
        bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if(bluetoothManager.adapter != null){
            bluetoothAdapter = bluetoothManager.adapter

            // Bluetooth está ativo?
            if(!bluetoothAdapter!!.isEnabled){
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                registerForResult.launch(enableBluetoothIntent)
            }else{
                showBoundedDevices()
            }
        }
    }

    private fun showBoundedDevices(){
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val listBoundedDevices = ArrayList<BluetoothDevice>()
            for(boundedDevice in bluetoothAdapter!!.bondedDevices){
                listBoundedDevices.add(boundedDevice)
            }
            listBoundedDevices.sortBy { it.name }
            val adapter = ListAdapterBluetoothBoundedDevices(requireContext(), listBoundedDevices)
            lvBoundedDevices.adapter = adapter
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth, container, false)
    }

}