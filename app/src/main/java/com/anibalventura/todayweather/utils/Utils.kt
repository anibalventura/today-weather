package com.anibalventura.todayweather.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

fun sharedPref(context: Context): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(context)
}

fun snackBarMsg(view: View, message: String) {
    val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    snackBar.show()
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }
}

@Suppress("NAME_SHADOWING")
fun getUnit(value: String): String {
    var value = "˚C"
    if ("US" == value || "LR" == value || "MM" == value) value = "˚F"
    return value
}

@SuppressLint("SimpleDateFormat")
fun getUnixTime(timex: Long): String {
    val date = Date(timex * 1000L)
    val sdf = SimpleDateFormat("HH:mm", Locale.UK)
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(date)
}