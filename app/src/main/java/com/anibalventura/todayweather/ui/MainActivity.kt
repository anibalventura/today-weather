package com.anibalventura.todayweather.ui

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.anibalventura.todayweather.R
import com.anibalventura.todayweather.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ThemeApp) // Set theme after splash screen.
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        setupNavigation()
    }

    /* ===================================== Navigation ===================================== */

    override fun onSupportNavigateUp(): Boolean {
        return (Navigation.findNavController(this, R.id.navHostFragment).navigateUp()
                || super.onSupportNavigateUp())
    }

    private fun setupNavigation() {
        val navController: NavController = findNavController(R.id.navHostFragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination: NavDestination, _ ->
            val toolBar = supportActionBar ?: return@addOnDestinationChangedListener

            binding.toolbar.setBackgroundColor(
                ActivityCompat.getColor(this, R.color.backgroundColor)
            )
            this.window.navigationBarColor = ActivityCompat.getColor(this, R.color.primaryColor)
            this.window.statusBarColor = ActivityCompat.getColor(this, R.color.primaryColor)

            when (destination.id) {
                R.id.weatherFragment -> setToolBarView(toolBar, true, false)
            }
        }
    }

    private fun setToolBarView(
        toolBar: ActionBar, showTitle: Boolean, showUpButton: Boolean
    ) {
        toolBar.setDisplayShowTitleEnabled(showTitle)
        toolBar.setDisplayHomeAsUpEnabled(showUpButton)
    }

    /** ===================================== Activity exit/close ===================================== **/

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}