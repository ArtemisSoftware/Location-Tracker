package com.artemissoftware.locationtracker

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import android.R.string.cancel
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import android.net.Uri.fromParts
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.artemissoftware.locationtracker.adapters.PinAdapter
import com.artemissoftware.locationtracker.models.Pin
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
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
                Date()
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

        //--toggleButtons();
    }



    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions() = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED


    private fun requestPermissions() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(this)
            .check()
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

        if (!checkPermissions()) {
            requestPermissions()
        }
        else {

            fusedLocationClient.lastLocation.addOnCompleteListener { taskLocation ->

                    if (taskLocation.isSuccessful && taskLocation.result != null) {

                        mCurrentLocation = taskLocation.result!!

                        updateLocationUI()

                    }
                    else {
                        //Log.w(TAG, "getLastLocation:exception", taskLocation.exception)
                        showSnackbar(R.string.no_location_detected)
                    }
                }
        }
    }



    override fun onSuccess(LocationSettingsResponse: LocationSettingsResponse?) {

        showSnackbar(R.string.start_location_updates)

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
                    //Log.i(TAG, "PendingIntent unable to execute request.");
                }

            }

            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                val errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                //Log.e(TAG, errorMessage);

                //Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }

        updateLocationUI();
    }


    override fun onComplete(task: Task<Void>) {
        showSnackbar(R.string.stop_location_updates)
    }


    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private fun startLocationUpdates() {

        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(this)
            .addOnFailureListener(this)

    }


    /**
     * Removing location updates
     */
    private fun stopLocationUpdates() {

        fusedLocationClient.removeLocationUpdates(mLocationCallback)
            .addOnCompleteListener(this)
    }





    /**
     * Shows a [Snackbar].
     *
     * @param snackStrId The id for the string resource for the Snackbar text.
     * @param actionStrId The text of the action item.
     * @param listener The listener associated with the Snackbar action.
     */
    private fun showSnackbar(snackStrId: Int, actionStrId: Int = 0, listener: View.OnClickListener? = null) {

        val snackbar = Snackbar.make(findViewById(android.R.id.content), getString(snackStrId), Snackbar.LENGTH_INDEFINITE)

        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }

        snackbar.show()
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
            showSettingsDialog();
        }
    }



    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     */
    private fun showSettingsDialog() {

        val builder = AlertDialog.Builder(this@MainActivity)

        builder.setTitle("Need Permissions")
        builder.setMessage(getString(R.string.permission_rationale))
        builder.setPositiveButton("GOTO SETTINGS",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()
                openSettings()
            })
        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        builder.show()

    }


    // navigating user to app settings
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivityForResult(intent, 101)
    }




}