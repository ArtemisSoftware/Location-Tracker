package com.artemissoftware.locationtracker.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.artemissoftware.locationtracker.R
import com.google.android.material.snackbar.Snackbar

class Messages {

    companion object{


        /**
         * Shows a [Snackbar].
         *
         * @param snackStrId The id for the string resource for the Snackbar text.
         * @param actionStrId The text of the action item.
         * @param listener The listener associated with the Snackbar action.
         */
        fun showSnackbar(activity : Activity, snackStrId: Int, actionStrId: Int = 0, listener: View.OnClickListener? = null) {

            val snackbar = Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(snackStrId), Snackbar.LENGTH_INDEFINITE)

            if (actionStrId != 0 && listener != null) {
                snackbar.setAction(activity.getString(actionStrId), listener)
            }

            snackbar.show()
        }


        /**
         * Showing Alert Dialog with Settings option
         * Navigates user to app settings
         */
        fun showSettingsDialog(activity : Activity) {

            val builder = AlertDialog.Builder(activity)

            builder.setTitle("Need Permissions")
            builder.setMessage(activity.getString(R.string.permission_rationale))
            builder.setPositiveButton("GOTO SETTINGS",
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                    openSettings(activity)
                })
            builder.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
            builder.show()

        }



        // navigating user to app settings
        private fun openSettings(activity : Activity) {

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.setData(uri)
            activity.startActivityForResult(intent, 101)
        }
    }
}