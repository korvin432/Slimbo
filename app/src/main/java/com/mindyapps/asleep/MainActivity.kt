package com.mindyapps.asleep

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mindyapps.asleep.data.model.Recording
import com.mindyapps.asleep.preferences.SleepingStore
import com.mindyapps.asleep.ui.sleeping.SleepingActivity


class MainActivity : AppCompatActivity() {

    private var currentNavController: LiveData<NavController>? = null
    private lateinit var sleepingStore: SleepingStore
    var recording: Recording? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sleepingStore = SleepingStore(
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }
        if (sleepingStore.isWorking) {
            startActivity(Intent(this, SleepingActivity::class.java))
        }

        if (intent.getParcelableExtra<Recording>("recording") != null) {
            recording = intent.getParcelableExtra("recording")
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }


    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)

        val navGraphIds =
            listOf(
                R.navigation.sleep,
                R.navigation.history,
                R.navigation.statistics,
                R.navigation.relax,
                R.navigation.more
            )

        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment,
            intent = intent
        )

        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.sleeping_tip_fragment -> {
                        bottomNavigationView.visibility = View.GONE
                    }
                    R.id.navigation_sleep -> {
                        theme.applyStyle(R.style.AppThemeTransparent, true)
                        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        window.statusBarColor =
                            ContextCompat.getColor(this, R.color.colorTransparent)
                    }
                    else -> {
                        bottomNavigationView.visibility = View.VISIBLE
                        supportActionBar!!.setBackgroundDrawable(
                            ColorDrawable(ContextCompat.getColor(this, R.color.activity_bg))
                        )
                        window.statusBarColor = ContextCompat.getColor(this, R.color.activity_bg)
                    }
                }
            }
        })


        currentNavController = controller

        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportActionBar!!.elevation = 0f
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

}