package com.untels.intelligent.systems.util

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

infix fun Pair<CoordinatorLayout, View>.showSnackbar(message: String) {
    Snackbar.make(first, message, Snackbar.LENGTH_SHORT).setAnchorView(second).show()
}

fun isPermissionGranted(context: Context, permission: String?): Boolean {
    if (ContextCompat.checkSelfPermission(context, permission!!) == PackageManager.PERMISSION_GRANTED) {
        Log.i("SI UNTELS", "Permission granted: $permission")
        return true
    }
    Log.i("SI UNTELS", "Permission NOT granted: $permission")
    return false
}