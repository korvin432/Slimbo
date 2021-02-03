package com.mindyapps.asleep

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.android.billingclient.api.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mindyapps.asleep.data.model.Recording
import com.mindyapps.asleep.preferences.SleepingStore
import com.mindyapps.asleep.ui.onboarding.OnboardingActivity
import com.mindyapps.asleep.ui.sleeping.SleepingActivity


class MainActivity : AppCompatActivity(), SkuDetailsResponseListener {

    val skusWithSkuDetails = MutableLiveData<Map<String, SkuDetails>>()
    private var currentNavController: LiveData<NavController>? = null
    private lateinit var sleepingStore: SleepingStore
    var recording: Recording? = null
    var subscribed = false

    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(
                "onboarding_complete", false
            )
        ) {
            val onboarding = Intent(this, OnboardingActivity::class.java)
            startActivity(onboarding)
            finish()
            return
        }

        sleepingStore = SleepingStore(
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }
        if (sleepingStore.isWorking) {
            val intent = Intent(this, SleepingActivity::class.java)
            intent.putExtra("isSubscribed", subscribed)
            startActivity(intent)
        }

        if (intent.getParcelableExtra<Recording>("recording") != null) {
            recording = intent.getParcelableExtra("recording")
        }

        billingClient = BillingClient.newBuilder(this)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.d("qwwe", "The BillingClient is ready")
                    querySkuDetails()

                    val activeSubs =
                        billingClient.queryPurchases(BillingClient.SkuType.SUBS).purchasesList
                    Log.d("qwwe", "active subs: $activeSubs")
                    activeSubs!!.forEach {
                        if (it.sku == "asleep_subscription" || it.sku == "half" || it.sku == "year") {
                            subscribed = true
                        }
                    }
                    Log.d("qwwe", "subscribed: $subscribed")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d("qwwe", "onBillingServiceDisconnected")
            }
        })

    }

    private val purchaseUpdateListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            subscribed =
                billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null
            Log.d("qwwe", "updating subscription status to $subscribed")
        }

    fun querySkuDetails() {
        Log.d("qwwe", "querySkuDetails")
        val params = SkuDetailsParams.newBuilder()
            .setType(BillingClient.SkuType.SUBS)
            .setSkusList(
                listOf(
                    "half",
                    "asleep_subscription",
                    "year"
                )
            )
            .build()
        params?.let { skuDetailsParams ->
            Log.i("qwwe", "querySkuDetailsAsync")
            billingClient.querySkuDetailsAsync(skuDetailsParams, this)
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

    override fun onSkuDetailsResponse(
        billingResult: BillingResult,
        skuDetailsList: MutableList<SkuDetails>?
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (skuDetailsList == null) {
                    skusWithSkuDetails.postValue(emptyMap())
                } else
                    skusWithSkuDetails.postValue(HashMap<String, SkuDetails>().apply {
                        for (details in skuDetailsList) {
                            put(details.sku, details)
                        }
                    }.also { postedValue ->
                        Log.i("qwwe", "onSkuDetailsResponse: count ${postedValue.size}")
                    })
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.ERROR -> {
                Log.e("qwwe", "onSkuDetailsResponse: $responseCode $debugMessage")
            }
            BillingClient.BillingResponseCode.USER_CANCELED,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                // These response codes are not expected.
                Log.wtf("qwwe", "onSkuDetailsResponse: $responseCode $debugMessage")
            }
        }
    }

}
