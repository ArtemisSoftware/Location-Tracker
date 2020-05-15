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


class MainActivity : AppCompatActivity(), PermissionListener {


    /**
     * Provides the entry point to the Fused Location Provider API.
     */

    //private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var txt_latitude: TextView
    private lateinit var txt_longitude: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txt_latitude = findViewById(R.id.txt_latitude)
        txt_longitude = findViewById(R.id.txt_longitude)


        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            .withListener(this)
            .check()

        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions() = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED


    private fun requestPermissions() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
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
    /*
    //@SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnCompleteListener { taskLocation ->
                if (taskLocation.isSuccessful && taskLocation.result != null) {

                    val location = taskLocation.result

                    latitudeText.text = resources
                        .getString(R.string.latitude_label, location?.latitude)
                    longitudeText.text = resources
                        .getString(R.string.longitude_label, location?.longitude)
                } else {
                    Log.w(TAG, "getLastLocation:exception", taskLocation.exception)
                    showSnackbar(R.string.no_location_detected)
                }
            }
    }
    */




    override fun onStart() {
        super.onStart()

        if (!checkPermissions()) {
            requestPermissions()
        }
        /* else {
            getLastLocation()
        }
        */
    }


    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        //getLastLocation()
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
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
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





    /**
     * Shows a [Snackbar].
     *
     * @param snackStrId The id for the string resource for the Snackbar text.
     * @param actionStrId The text of the action item.
     * @param listener The listener associated with the Snackbar action.
     *//*
    private fun showSnackbar(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), getString(snackStrId),
            LENGTH_INDEFINITE)
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }
*/
    /**
     * Return the current state of the permissions needed.
     *//*

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE)
    }


*/
    /**
     * Callback received when a permissions request has been completed.
     *//*
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")

                // Permission granted.
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> getLastLocation()

                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                else -> {
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        View.OnClickListener {
                            // Build intent that displays the App settings screen.
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", APPLICATION_ID, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                        })
                }
            }
        }
    }
*/
}