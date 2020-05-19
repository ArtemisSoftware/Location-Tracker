package com.artemissoftware.locationtracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import android.content.IntentSender
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.artemissoftware.locationtracker.adapters.PinAdapter
import com.artemissoftware.locationtracker.models.Pin
import com.artemissoftware.locationtracker.util.Battery
import com.artemissoftware.locationtracker.util.Messages
import com.artemissoftware.locationtracker.util.Permissions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.*


class MainActivity : AppCompatActivity(), PermissionListener, View.OnClickListener,
    OnSuccessListener<LocationSettingsResponse>, OnFailureListener, OnCompleteListener<Void> {



    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private lateinit var mSettingsClient: SettingsClient;
    private lateinit var mLocationRequest: LocationRequest;
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest;
    private lateinit var mLocationCallback: LocationCallback ;
    private var mCurrentLocation: Location? = null;


    // location updates interval - 10sec
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 3 * 1000

    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000

    private val REQUEST_CHECK_SETTINGS = 100


    // boolean flag to toggle the ui
    private var  mRequestingLocationUpdates: Boolean = false


    private lateinit var pinAdapter: PinAdapter;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener(this);
        fab_last_location.setOnClickListener(this);
        fab_start_tracking.setOnClickListener(this);
        fab_clear.setOnClickListener(this);
        fab_stop_tracking.setOnClickListener(this);

        pinAdapter = PinAdapter();

        rcl_locations.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = pinAdapter
        }

        init()

    }


    private fun init(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mSettingsClient = LocationServices.getSettingsClient(this);


        mLocationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                // location is received
                mCurrentLocation = locationResult?.getLastLocation()!!;

                updateLocationUI();
            }
        }


        mRequestingLocationUpdates = false;

        mLocationRequest = LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        val builder = LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

    }



    /**
     * Update the UI displaying the location data and toggling the buttons
     */
    private fun updateLocationUI() {

        if(mCurrentLocation != null) {
            txt_latitude.text = mCurrentLocation?.latitude.toString()
            txt_longitude.text = mCurrentLocation?.longitude.toString()

            val pin = Pin(
                mCurrentLocation?.latitude.toString(),
                mCurrentLocation?.longitude.toString(),
                Date(),
                Battery.getBatteryPercentage(applicationContext)
            )
            pinAdapter.addPin(pin);
        }
        //--toggleButtons();
    }


    /**
     * Clear the UI displaying the location data
     */
    private fun clearUI() {

        txt_latitude.text = ""
        txt_longitude.text = ""

        pinAdapter.clear()

    }





    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     *
     * Note: this method should be called after location permission has been granted.
     */
    //@SuppressLint("MissingPermission")
    private fun getLastLocation() {

        if (!Permissions.checkPermissions(applicationContext)) {
            Permissions.requestPermissions(applicationContext, this)
        }
        else {

            fusedLocationClient.lastLocation.addOnCompleteListener { taskLocation ->

                    if (taskLocation.isSuccessful && taskLocation.result != null) {

                        mCurrentLocation = taskLocation.result!!
                        updateLocationUI()
                    }
                    else {
                        Messages.showSnackbar(this, R.string.no_location_detected)
                    }
                }
        }
    }




    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private fun startLocationUpdates() {

        if (!Permissions.checkPermissions(applicationContext)) {
            Permissions.requestPermissions(applicationContext, this)
        }
        else {
            mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this)
                .addOnFailureListener(this)
        }
    }


    /**
     * Removing location updates
     */
    private fun stopLocationUpdates() {

        fusedLocationClient.removeLocationUpdates(mLocationCallback)
            .addOnCompleteListener(this)
    }



    private fun getAddress() : String{

        val geocoder = Geocoder(this, Locale.getDefault());

        val addresses = geocoder.getFromLocation(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude, 1);
        return addresses.get(0).getAddressLine(0);
    }





    override fun onSuccess(LocationSettingsResponse: LocationSettingsResponse?) {

        Messages.showSnackbar(this, R.string.start_location_updates)

        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

        updateLocationUI();
    }


    override fun onFailure(e: Exception) {

        val statusCode = (e as ApiException).getStatusCode()

        when (statusCode) {
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {

                try {
                    val rae =  e as ResolvableApiException;
                    rae.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                }
                catch (sie : IntentSender.SendIntentException) {
                    Messages.showSnackbar(this, R.string.pending_intent_unable)
                }
            }

            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                Messages.showSnackbar(this, R.string.location_settings_inadequate)
            }
        }

        updateLocationUI();
    }


    override fun onComplete(task: Task<Void>) {
        Messages.showSnackbar(this, R.string.stop_location_updates)
    }



    override fun onClick(v: View?) {

        when(v?.id){

            R.id.fab_last_location -> getLastLocation()
            R.id.fab_start_tracking -> startLocationUpdates()
            R.id.fab_clear -> clearUI()
            R.id.fab_stop_tracking -> stopLocationUpdates()
        }

        fab_menu.close(false);
    }



    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        getLastLocation()
    }

    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {

        // check for permanent denial of permission
        if (response?.isPermanentlyDenied()!!) {
            Messages.showSettingsDialog(this);
        }
    }




}