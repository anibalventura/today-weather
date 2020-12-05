package com.anibalventura.todayweather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.anibalventura.todayweather.R
import com.anibalventura.todayweather.data.models.WeatherResponse
import com.anibalventura.todayweather.data.network.WeatherService
import com.anibalventura.todayweather.databinding.FragmentWeatherBinding
import com.anibalventura.todayweather.utils.Constants.API_KEY
import com.anibalventura.todayweather.utils.Constants.BASE_URL
import com.anibalventura.todayweather.utils.Constants.HIDE_PROGRESS
import com.anibalventura.todayweather.utils.Constants.METRIC_UNIT
import com.anibalventura.todayweather.utils.Constants.SHOW_PROGRESS
import com.anibalventura.todayweather.utils.isNetworkAvailable
import com.anibalventura.todayweather.utils.snackBarMsg
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit.*
import java.text.SimpleDateFormat
import java.util.*

class WeatherFragment : Fragment() {

    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var progressDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        getLocation()

        return binding.root
    }

    /** ===================================== Permissions. ===================================== **/

    private fun getLocation() {
        Dexter.withContext(requireContext()).withPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (isLocationEnabled() && report!!.areAllPermissionsGranted()) requestLocationData()
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>, token: PermissionToken
            ) = permissionDeniedDialog()

        }).onSameThread().check()
    }

    private fun permissionDeniedDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.dialog_permission_denied)
            message(R.string.dialog_permission_denied_msg)
            negativeButton(R.string.dialog_cancel)
            positiveButton(R.string.dialog_go_to_settings) {
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", activity?.packageName, null)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /** ===================================== Get Location. ===================================== **/

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!locationEnabled) {
            Toast.makeText(
                requireContext(), "Your GPS is OFF. Please turn it ON.", Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        return locationEnabled
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback(), Looper.myLooper()
        )
    }

    private fun locationCallback() = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            getWeatherDetails(lastLocation.latitude, lastLocation.longitude)
        }
    }

    /** ===================================== Get Weather. ===================================== **/

    private fun progressDialog(show: String) {
        when (show) {
            SHOW_PROGRESS -> {
                progressDialog = Dialog(requireContext())
                progressDialog!!.setContentView(R.layout.dialog_progress)
                progressDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                progressDialog!!.show()
            }
            HIDE_PROGRESS -> {
                if (progressDialog != null) progressDialog!!.dismiss()
            }
        }
    }

    private fun getWeatherDetails(latitude: Double, longitude: Double) {
        when {
            isNetworkAvailable(requireActivity()) -> {
                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service: WeatherService = retrofit.create(WeatherService::class.java)

                val listCall: Call<WeatherResponse> =
                    service.getWeather(latitude, longitude, METRIC_UNIT, API_KEY)

                progressDialog(SHOW_PROGRESS)

                listCall.enqueue(object : Callback<WeatherResponse> {
                    override fun onResponse(
                        response: Response<WeatherResponse>, retrofit: Retrofit
                    ) {
                        when {
                            response.isSuccess -> {
                                progressDialog(HIDE_PROGRESS)

                                val weatherList: WeatherResponse = response.body()
                                setWeatherView(weatherList)
                                Log.i("DATA", "$weatherList")
                            }
                            else -> {
                                when (response.code()) {
                                    400 -> Log.i("Error 400", "Bad Connection.")
                                    404 -> Log.i("Error 404", "Not Found.")
                                    else -> Log.i("Error", "Something went wrong.")
                                }
                            }
                        }
                    }

                    override fun onFailure(t: Throwable?) {
                        progressDialog(HIDE_PROGRESS)
                        Log.e("ERROR", t!!.message.toString())
                    }

                })
            }
            else -> snackBarMsg(requireView(), "Not connected to the internet.")
        }
    }

    /** ===================================== Setup view. ===================================== **/

    @SuppressLint("SetTextI18n")
    private fun setWeatherView(weatherList: WeatherResponse) {
        for (i in weatherList.weather.indices) {
            binding.tvWeather.text = weatherList.weather[i].main
            binding.tvWeatherDescription.text = weatherList.weather[i].description

            binding.tvTemperature.text =
                weatherList.main.temp.toString() + getUnit(resources.configuration.toString())
            binding.tvHumidity.text = "${weatherList.main.humidity.toString()}%"

            binding.tvMin.text =
                weatherList.main.temp_min.toString() + getUnit(resources.configuration.toString()) + " min"
            binding.tvMax.text =
                weatherList.main.temp_max.toString() + getUnit(resources.configuration.toString()) + " max"

            binding.tvWindSpeed.text = weatherList.wind.speed.toString()

            binding.tvLocationName.text = weatherList.name
            binding.tvLocationCountry.text = weatherList.sys.country
            binding.tvSunriseTime.text = getUnixTime(weatherList.sys.sunrise.toLong())
            binding.tvSunsetTime.text = getUnixTime(weatherList.sys.sunset.toLong())
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun getUnit(value: String): String {
        var value = "˚C"
        if ("US" == value || "LR" == value || "MM" == value) value = "˚F"
        return value
    }

    @SuppressLint("SimpleDateFormat")
    private fun getUnixTime(timex: Long): String {
        val date = Date(timex * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.UK)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    /** ===================================== Fragment exit/close. ===================================== **/

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}