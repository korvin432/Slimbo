package com.mindyapps.asleep.ui.onboarding

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.codemybrainsout.onboarder.AhoyOnboarderActivity
import com.codemybrainsout.onboarder.AhoyOnboarderCard
import com.mindyapps.asleep.MainActivity
import com.mindyapps.asleep.R
import com.mindyapps.asleep.ui.subs.SubscribeActivity


class OnboardingActivity : AhoyOnboarderActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ahoyOnboarderCard1 = AhoyOnboarderCard(
            getString(R.string.app_name),
            getString(R.string.start_sleeping_better),
            R.drawable.ic_launcher_big
        )
        val ahoyOnboarderCard2 = AhoyOnboarderCard(
            getString(R.string.monitoring),
            getString(R.string.monitor_text),
            R.drawable.sleep_tip_image
        )
        val ahoyOnboarderCard3 = AhoyOnboarderCard(
            getString(R.string.statistics),
            getString(R.string.watch_changes),
            R.drawable.stat_image
        )
        val ahoyOnboarderCard4 = AhoyOnboarderCard(
            getString(R.string.title_relax),
            getString(R.string.relax_text),
            R.drawable.relax_image
        )

        ahoyOnboarderCard1.setBackgroundColor(R.color.black_transparent)
        ahoyOnboarderCard2.setBackgroundColor(R.color.black_transparent)
        ahoyOnboarderCard3.setBackgroundColor(R.color.black_transparent)
        ahoyOnboarderCard4.setBackgroundColor(R.color.black_transparent)

        val pages: MutableList<AhoyOnboarderCard> = ArrayList()

        pages.add(ahoyOnboarderCard1)
        pages.add(ahoyOnboarderCard2)
        pages.add(ahoyOnboarderCard3)
        pages.add(ahoyOnboarderCard4)

        for (page in pages) {
            page.setTitleColor(R.color.colorWhiteText)
            page.setDescriptionColor(R.color.colorLightHint)
        }

        setFinishButtonTitle(getString(R.string.get_started))
        showNavigationControls(true)

        val colorList: MutableList<Int> = ArrayList()
        colorList.add(R.color.activity_bg)
        colorList.add(R.color.card_bg)
        colorList.add(R.color.card_divider)
        colorList.add(R.color.card_bg2)
        setColorBackground(colorList)

        setFinishButtonDrawableStyle(ContextCompat.getDrawable(this, R.drawable.boarding_gradient))

        setOnboardPages(pages)
    }


    override fun onFinishButtonPressed() {
        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
            .putBoolean("onboarding_complete", true).apply()
        startActivity(Intent(this, MainActivity::class.java))
        startActivity(Intent(this, SubscribeActivity::class.java))
        finish()
    }
}
