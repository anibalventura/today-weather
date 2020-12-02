package com.anibalventura.todayweather.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun snackBarMsg(view: View, message: String) {
    val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    snackBar.show()
}