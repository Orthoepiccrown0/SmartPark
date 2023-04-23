package com.epiccrown.smartpark.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionHelper {
    fun isPermissionGranted(context: Context, values: Array<String>): Boolean {
        return values.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasPositionPermissions(context: Context): Boolean {
        return isGranted(context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) &&
                isGranted(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    fun hasFilesPermissions(context: Context): Boolean {
        return isGranted(context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) &&
                isGranted(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private fun isGranted(checkSelfPermission: Int): Boolean {
        return checkSelfPermission == PackageManager.PERMISSION_GRANTED
    }
}