package com.mindyapps.asleep.ui.subs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.*
import com.mindyapps.asleep.R
import com.mindyapps.asleep.data.model.Music
import kotlinx.android.synthetic.main.activity_subscribe.*
import kotlinx.coroutines.launch
import kotlin.math.round

class SubscribeActivity : AppCompatActivity(), View.OnClickListener, SkuDetailsResponseListener {

    val skusWithSkuDetails = MutableLiveData<Map<String, SkuDetails>>()
    private lateinit var billingClient: BillingClient
    private lateinit var observerSku: Observer<Map<String, SkuDetails>>
    private var selectedSku = "half"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscribe)
        theme.applyStyle(R.style.AppThemeTransparent, true)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        supportActionBar!!.title = ""


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
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d("qwwe", "onBillingServiceDisconnected")
            }
        })


        month_layout.setOnClickListener(this)
        three_month_layout.setOnClickListener(this)
        year_layout.setOnClickListener(this)
        sub_button.setOnClickListener(this)

        observerSku = Observer { newsSource ->
            if (newsSource != null) {
                setUpPrices()
            }
        }
        lifecycleScope.launch {
            skusWithSkuDetails.observe(this@SubscribeActivity, observerSku)
        }

        three_month_layout.setBackgroundResource(R.drawable.sub_button_gradient)
        month_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
        year_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
        selectedSku = "half"
    }

    private val purchaseUpdateListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
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

    fun setUpPrices() {
        Log.d("qwwe", "${skusWithSkuDetails.value}")
        val month = skusWithSkuDetails.value?.get("asleep_subscription")!!
        val threeMonths = skusWithSkuDetails.value?.get("half")!!
        val year = skusWithSkuDetails.value?.get("year")!!

        month_price.text = month.price
        val perMonth =
            ((month.priceAmountMicros.toDouble() / 1000000).toInt()).toString() + " " +
                    month.priceCurrencyCode
        month_monthly_price.text = perMonth + "/ " + getString(R.string.month).toLowerCase()

        three_price.text = threeMonths.price
        val threePerMonth =
            ((threeMonths.priceAmountMicros.toDouble() / 3000000).toInt()).toString() + " " +
                    threeMonths.priceCurrencyCode
        three_monthly_price.text = threePerMonth + "/ " + getString(R.string.month).toLowerCase()

        year_price.text = year.price
        val yearPerMonth =
            ((year.priceAmountMicros.toDouble() / 12000000).toInt()).toString() + " " +
                    year.priceCurrencyCode
        year_monthly_price.text = yearPerMonth + "/ " + getString(R.string.month).toLowerCase()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.month_layout -> {
                month_layout.setBackgroundResource(R.drawable.sub_button_gradient)
                three_month_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
                year_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
                selectedSku = "asleep_subscription"
            }
            R.id.three_month_layout -> {
                three_month_layout.setBackgroundResource(R.drawable.sub_button_gradient)
                month_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
                year_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
                selectedSku = "half"
            }
            R.id.year_layout -> {
                year_layout.setBackgroundResource(R.drawable.sub_button_gradient)
                three_month_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
                month_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
                selectedSku = "year"
            }
            R.id.sub_button -> {
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skusWithSkuDetails.value?.get(selectedSku)!!)
                    .build()
                val responseCode = billingClient.launchBillingFlow(this, flowParams)
                Log.d(
                    "qwwe",
                    "buying response ${responseCode.responseCode}. message: ${responseCode.debugMessage}"
                )
            }
        }
    }

    override fun onSkuDetailsResponse(
        billingResult: BillingResult,
        skuDetailsList: MutableList<SkuDetails>?
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.i("qwwe", "onSkuDetailsResponse: $responseCode $debugMessage")
                if (skuDetailsList == null) {
                    Log.w("qwwe", "onSkuDetailsResponse: null SkuDetails list")
                    skusWithSkuDetails.postValue(emptyMap())
                } else
                    skusWithSkuDetails.postValue(HashMap<String, SkuDetails>().apply {
                        for (details in skuDetailsList) {
                            Log.d("qwwe", "putting ${details.sku}")
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
