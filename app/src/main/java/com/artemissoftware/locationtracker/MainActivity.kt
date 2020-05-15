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
import android.net.Uri
import android.provider.Settings
import android.view.View
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity(), PermissionListener {


    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var txt_latitude: TextView
    private lateinit var txt_longitude: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txt_latitude = findViewById(R.id.txt_latitude)
        txt_longitude = findViewById(R.id.txt_longitude)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
        fusedLocationClient.lastLocation

            .addOnCompleteListener { taskLocation ->

                if (taskLocation.isSuccessful && taskLocation.result != null) {

                    val location = taskLocation.result

                    txt_latitude.text = location?.latitude.toString()
                    txt_longitude.text = location?.longitude.toString()

                }
                else {
                    //Log.w(TAG, "getLastLocation:exception", taskLocation.exception)
                    showSnackbar(R.string.no_location_detected)
                }
            }
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




    override fun onStart() {
        super.onStart()

        if (!checkPermissions()) {
            requestPermissions()
        }
        else {
            getLastLocation()
        }

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