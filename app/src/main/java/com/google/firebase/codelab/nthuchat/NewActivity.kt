package com.google.firebase.codelab.nthuchat

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_new.*
import java.text.SimpleDateFormat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlacePicker
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class NewActivity : AppCompatActivity() {
    private val PLACE_PICKER_REQUEST = 1
    private var mGoogleApiClient: GoogleApiClient? = null

    var database = FirebaseDatabase.getInstance()
    var myRef = database.getReference("message")
    var selectedType : String? = null
    var lat : String? = null
    var long : String?  = null

    lateinit var eventTitleUI: EditText
    lateinit var startDateUI: TextView
    lateinit var endDateUI: TextView
    lateinit var startTimeUI: TextView
    lateinit var endTimeUI: TextView
    lateinit var addressUI: TextView
    lateinit var notesUI: EditText
    lateinit var buttonSendUI: Button
    lateinit var typeUI: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new)

        eventTitleUI = findViewById(R.id.eventTitle)
        startDateUI = findViewById(R.id.startDate)
        startTimeUI = findViewById(R.id.startTime)
        endDateUI = findViewById(R.id.endDate)
        endTimeUI = findViewById(R.id.endTime)
        addressUI = findViewById(R.id.address)
        notesUI = findViewById(R.id.notes)
        typeUI = findViewById(R.id.type)


        // Call Calendar
        startDateUI.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDate ->
                startDateUI.setText("" + mDate + "/" + mMonth + "/" + mYear)
            }, year, month, day)
            dpd.show()
        }


        endDateUI.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDate ->
                endDateUI.setText("" + mDate + "/" + mMonth + "/" + mYear)
            }, year, month, day)
            dpd.show()
        }

        // Call TimePicker
        startTimeUI.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListenner = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                //set time to textview
                startTimeUI.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this, timeSetListenner, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }



        endTimeUI.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListenner = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                //set time to textview
                endTimeUI.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this, timeSetListenner, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        buttonSendUI = findViewById(R.id.buttonSend)
        buttonSendUI.setOnClickListener {
            sendSchedule()
        }

        //Initialise GoogleApiClient
        buildGoogleApiClient()

        //OnClickListener
        addressUI.setOnClickListener(View.OnClickListener {
            if (!checkGPSEnabled()) {
                return@OnClickListener
            }
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
        })


        val scheduleType = arrayOf("啓發", "學習", "娛樂", "社交")
        typeUI.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, scheduleType)

        // Initializing an ArrayAdapter
        var adapter = ArrayAdapter(
                this, // Context
                android.R.layout.simple_spinner_item, // Layout
                scheduleType // Array
        )

        //item selected listener for spinner
        typeUI.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//                Toast.makeText(this@NewActivity, scheduleType[p2], LENGTH_SHORT).show()
                selectedType = scheduleType[p2]
            }



        }
    }

    fun sendSchedule() {
        val name = eventTitleUI.text.toString()
        val startDate = startDateUI.text.toString()
        val startTime = startTimeUI.text.toString()
        val endDate = endDateUI.text.toString()
        val endTime = endTimeUI.text.toString()
        val notes = notesUI.text.toString()
        val address = addressUI.text.toString()
        val createrID: String = "ken"
        var type = selectedType.toString()

        if (name.isEmpty() || startDate.isEmpty() || startTime.isEmpty() || endDate.isEmpty() || endTime.isEmpty() || address.isEmpty()) {
            Toast.makeText(applicationContext, "請填寫完整資料", Toast.LENGTH_SHORT).show()
            return
        }

        val sDate = SimpleDateFormat("dd/MM/yyyy").parse(startDate)
        val eDate = SimpleDateFormat("dd/MM/yyyy").parse(endDate)
        val sTime = SimpleDateFormat("hh:mm").parse(startTime)
        val eTime = SimpleDateFormat("hh:mm").parse(endTime)



        if (sDate > eDate) {
            Toast.makeText(applicationContext, "請輸入正確日期", Toast.LENGTH_SHORT).show()
            return
        }

        if (sDate == eDate && sTime >= eTime) {
            Toast.makeText(applicationContext, "請輸入正確時間", Toast.LENGTH_SHORT).show()
            return
        }


        val ref = FirebaseDatabase.getInstance().getReference("Schedule")
        val scheduleId = ref.push().key.toString()

        val schedule = Schedule(name = name, startDate = startDate, startTime = startTime,
                endDate = endDate, endTime = endTime, address = address, notes = notes, createrID = createrID, type = type)

        ref.child(scheduleId).setValue(schedule).addOnCompleteListener() {
            Toast.makeText(applicationContext, "Schedule Add Success!", Toast.LENGTH_SHORT).show()

        }
    }

    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, null)
                .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val place = PlacePicker.getPlace(data, this)
            val toastMsg = String.format("Place: %s", place.name)
            lat = place!!.latLng.latitude.toString()
            long = place!!.latLng.longitude.toString()
            addressUI.text = place!!.latLng.latitude.toString().plus("\n".plus(place!!.latLng.longitude.toString()))
            Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkGPSEnabled(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Locations Settings is set to 'Off'.\nEnable Location to use this app")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }


}
