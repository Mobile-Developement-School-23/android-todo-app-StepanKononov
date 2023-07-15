package com.example.todo.ui.permissions


import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionHelper(context: Fragment, permissionListener: PermissionListener) {

    private var context: Fragment
    private var permissionListener: PermissionListener

    init {
        this.context = context
        this.permissionListener = permissionListener
    }

    fun checkForPermissions(manifestPermission: String) {
        when {
            context.requireContext().let {
                ContextCompat.checkSelfPermission(
                    it,
                    manifestPermission
                )
            } == PackageManager.PERMISSION_GRANTED -> {
                permissionListener.isPermissionGranted(true)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                context.requireContext() as Activity,
                manifestPermission
            ) -> {

                permissionListener.isPermissionGranted(false)
                permissionListener.shouldShowRationaleInfo()

            }

            else -> {
                launchPermissionDialog(manifestPermission)
            }
        }
    }


    fun launchPermissionDialog(manifestPermission: String) {
        requestPermissionLauncher.launch(manifestPermission)
    }


    private val requestPermissionLauncher =
        context.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                permissionListener.isPermissionGranted(true)
            } else {
                Log.i("Permission: ", "Denied")
            }
        }

}