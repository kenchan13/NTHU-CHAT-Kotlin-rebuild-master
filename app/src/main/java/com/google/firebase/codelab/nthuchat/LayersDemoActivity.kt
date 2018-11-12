package com.google.firebase.codelab.nthuchat

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
//import android.util.Log
import android.view.View
import android.view.View.GONE
import android.widget.AdapterView
//import android.widget.ArrayAdapter
import android.widget.CheckBox
//import android.widget.Spinner
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import android.widget.RelativeLayout
import com.google.android.gms.maps.model.LatLng
import android.support.design.widget.BottomNavigationView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.layers_demo.*
import android.view.MenuItem

private const val LOCATION_PERMISSION_REQUEST_CODE = 1

class GPS(val lat: Double, val long: Double)
class GPSOTHERUSER(val lat: Double, val long: Double){
    constructor() : this(0.0, 0.0)
}

/**
 * Demonstrates the different base layers of a map.
 */
class LayersDemoActivity :
        AppCompatActivity(),
        OnMapReadyCallback,
        AdapterView.OnItemSelectedListener,
        EasyPermissions.PermissionCallbacks, GoogleMap.OnMyLocationChangeListener {//, GoogleMap.OnMyLocationClickListener {

    private lateinit var map: GoogleMap

    private lateinit var myLocationCheckbox: CheckBox

    enum class FragmentType {
        inspiration, study, entertainment, social, friends
    }

    val manager = supportFragmentManager
    private var locationManager: LocationManager? = null
    private lateinit var database_gps_data:DatabaseReference
    //private lateinit var gps_list: MutableList<GPS>

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * [.onRequestPermissionsResult].
     */
    private var showPermissionDeniedDialog = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layers_demo)

        myLocationCheckbox = this.findViewById(R.id.my_location)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //GPS button Position
        val locationButton = (mapFragment.view!!.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(Integer.parseInt("2"))

        val rlp = locationButton.layoutParams as RelativeLayout.LayoutParams
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
        rlp.setMargins(0, 50, 30, 0)

        // addSchedule Button
        addSchedulePlus.setOnClickListener{
            val intent = Intent(this , NewActivity::class.java)
            Intent(this, NewActivity::class.java)
            startActivity(intent)
        }



        bottomNavBarMap()
        checkLocation()

    }

    /**
     * Display a dialog box asking the user to grant permissions if they were denied
     */
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (showPermissionDeniedDialog) {
            AlertDialog.Builder(this).apply {
                setPositiveButton(R.string.ok, null)
                setMessage(R.string.location_permission_denied)
                create()
            }.show()
            showPermissionDeniedDialog = false
        }
    }

    private fun bottomNavBarMap() {
        // set navigation Listener
        //id in layers_demo.xml
        bottom_navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        // start from friends
        //changeFragmentTo(FragmentType.friends)
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.inspiration -> {
                //changeFragmentTo(FragmentType.inspiration)
                return@OnNavigationItemSelectedListener true
            }

            R.id.study -> {
                //changeFragmentTo(FragmentType.study)
                return@OnNavigationItemSelectedListener true
            }

            R.id.entertainment -> {
                //changeFragmentTo(FragmentType.entertainment)
                return@OnNavigationItemSelectedListener true
            }

            R.id.social -> {
                //changeFragmentTo(FragmentType.social)
                return@OnNavigationItemSelectedListener true
            }

            R.id.friends -> {
                //changeFragmentTo(FragmentType.friends)
                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    private val isLocationEnabled: Boolean
        get() {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }


    private fun checkLocation(): Boolean {
        if (!isLocationEnabled)
            showAlert()
        return isLocationEnabled
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this APP")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }

    private fun getOtherUserLocation(){
        database_gps_data =  FirebaseDatabase.getInstance().getReference("gps_data")
        database_gps_data!!.addListenerForSingleValueEvent(messageListener)
//        database_gps_data!!.removeEventListener(messageListener)
    }

    private val messageListener = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot) {
//            gps_list = mutableListOf()
            if (dataSnapshot.exists()) {
//                val message = dataSnapshot.getValue(Message::class.java)
                map.clear()
                for(child in dataSnapshot.children){
                        val userLocation = child.getValue(GPSOTHERUSER::class.java)
                        val otherUserLat = userLocation!!.lat
                        val otherUserLong = userLocation.long
                        //get their name here
                        //val name = userLocation.name
                        //pass title with name
                        val eachUserUid = child.key
                        var mAuth = FirebaseAuth.getInstance()
                        var currentUser = mAuth!!.currentUser
                        val userID = currentUser!!.uid

                        if(eachUserUid != userID){
                            map.addMarker(MarkerOptions()
                                    .position(LatLng(otherUserLat, otherUserLong))
                                    .title("Other User here")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker3))
                            ).showInfoWindow()
                        }else{
                            map.addMarker(MarkerOptions()
                                    .position(LatLng(otherUserLat, otherUserLong))
                                    .title("I'm here")
                            ).showInfoWindow()
                        }

//                    Toast.makeText(this@LayersDemoActivity, "$child $otherUserLat $otherUserLong",
//                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Failed to read value
        }
    }

    override fun onMyLocationChange(location: Location ){
        // Getting latitude of the current location
        var latitude = location.latitude;

        // Getting longitude of the current location
        var longitude = location.longitude;

        var database = FirebaseDatabase.getInstance()
        var gps = GPS(latitude, longitude)
        var mAuth = FirebaseAuth.getInstance()
        var currentUser = mAuth!!.currentUser
        val userID = currentUser!!.uid
        var ref = database.getReference("gps_data/$userID")
        ref.setValue(gps)

        map.setOnMyLocationChangeListener(null)
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {

        // exit early if the map was not initialised properly
        map = googleMap ?: return

        updateMapType()

        // Must deal with the location checkbox separately as must check that
        // location permission have been granted before enabling the 'My Location' layer.
        if (myLocationCheckbox.isChecked) enableMyLocation()


        // if this box is checked, must check for permission before enabling the My Location layer
        myLocationCheckbox.setOnClickListener {
            if (!myLocationCheckbox.isChecked) {
                map.isMyLocationEnabled = false
            } else {
                enableMyLocation()
            }
        }

        val NTHU = LatLng(24.794740, 120.993217)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(NTHU, 16.5F))
//        map.setOnMyLocationClickListener(this)
        //map.setOnMyLocationChangeListener(myLocationChangeListener)
        val updateUserLocationDelay = Handler()
        updateUserLocationDelay.postDelayed(object : Runnable {
            override fun run() {
                map.setOnMyLocationChangeListener(this@LayersDemoActivity)
                getOtherUserLocation()
                updateUserLocationDelay.postDelayed(this, 100000)
            }
        }, 100)

        //delay 30sec
        val updateOtherUserLocationDelay = Handler()
        updateOtherUserLocationDelay.postDelayed(object : Runnable {
            override fun run() {
                getOtherUserLocation()
                updateOtherUserLocationDelay.postDelayed(this, 30000)
            }
        }, 100)


        map.uiSettings.isMapToolbarEnabled = false
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(LOCATION_PERMISSION_REQUEST_CODE)
    private fun enableMyLocation() {
        // Enable the location layer. Request the location permission if needed.
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if (EasyPermissions.hasPermissions(this, *permissions)) {
            map.isMyLocationEnabled = true

        } else {
            // if permissions are not currently granted, request permissions
            EasyPermissions.requestPermissions(this,
                    getString(R.string.permission_rationale_location),
                    LOCATION_PERMISSION_REQUEST_CODE, *permissions)
        }
    }

    /**
     * Change the type of the map depending on the currently selected item in the spinner
     */
    private fun updateMapType() {
        // This can also be called by the Android framework in onCreate() at which
        // point map may not be ready yet.
        if (!::map.isInitialized) return
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,
                permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        // Un-check the box until the layer has been enabled
        // and show dialog box with permission rationale.
        myLocationCheckbox.isChecked = false
        showPermissionDeniedDialog = true
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // do nothing, handled in updateMyLocation
    }

    /**
     * Called as part of the AdapterView.OnItemSelectedListener
     */
    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        updateMapType()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Do nothing.
    }

}


