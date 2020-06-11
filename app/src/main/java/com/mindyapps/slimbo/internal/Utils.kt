package com.mindyapps.slimbo.internal

import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Factor
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.model.TYPE_ALARM
import com.mindyapps.slimbo.data.model.TYPE_MUSIC
import java.io.File
import java.text.DateFormat
import java.util.*

class Utils(val context: Context) {

    fun minutesToMills(min: Int): Long {
        return (min * 1000).toLong()
    }

    fun getAlarmTime(array: IntArray?): String {
        val cal = Calendar.getInstance()
        if (!android.text.format.DateFormat.is24HourFormat(context)) {
            cal.set(Calendar.HOUR, array!![0])
        } else {
            cal.set(Calendar.HOUR_OF_DAY, array!![0])
        }
        cal.set(Calendar.MINUTE, array[1])
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.time)
    }

    fun getFactorsList(): MutableList<Factor> {
        val factors = mutableListOf<Factor>()
        factors.add(Factor(null, "coffee", "ic_coffee"))
        factors.add(Factor(null, "disease", "ic_ill"))
        factors.add(Factor(null, "nose", "ic_nose"))
        factors.add(Factor(null, "shower", "ic_showe"))
        factors.add(Factor(null, "wine", "ic_wine"))
        factors.add(Factor(null, "workout", "ic_workout"))
        factors.add(Factor(null, "late_dinner", "ic_late_dinner"))
        factors.add(Factor(null, "not_my_bed", "ic_not_my_bed"))
        factors.add(Factor(null, "tired", "ic_tired"))
        return factors
    }

    fun getMusicList(): MutableList<Music> {
        val musicList = mutableListOf<Music>()
        musicList.add(
            Music(null, context.getString(R.string.do_not_use), "", true, TYPE_MUSIC, 0)
        )
        musicList.add(
            Music(
                null, "Autumn dream lullaby", "autumn_dream_lullaby", true, TYPE_MUSIC,
                getDurationLength(
                    context,
                    R.raw.autumn_dream_lullaby
                )
            )
        )
        musicList.add(
            Music(
                null, "Looking at the night sky", "looking_at_the_night_sky", false, TYPE_MUSIC,
                385000
            )
        )
        musicList.add(
            Music(
                null, "Mystic sea", "mystic_sea", false, TYPE_MUSIC,300000
            )
        )
        musicList.add(
            Music(
                null, "Ambient Background", "ambient_background", true, TYPE_MUSIC,
                getDurationLength(
                    context,
                    R.raw.ambient_background
                )
            )
        )
        musicList.add(
            Music(
                null, "Asian meditation", "asian_meditation", false, TYPE_MUSIC,
                600000
            )
        )
        musicList.add(
            Music(
                null, "Magic Forest", "magic_forest", false, TYPE_MUSIC,
                270000
            )
        )
        musicList.add(
            Music(
                null, "Meditation manatees", "meditation_manatees", false, TYPE_MUSIC,
                1050000
            )
        )
        musicList.add(
            Music(
                null, "Nature music", "nature_music", false, TYPE_MUSIC,
                270000
            )
        )
        musicList.add(
            Music(
                null, "River music", "river_music", false, TYPE_MUSIC,
                300000
            )
        )
        musicList.add(
            Music(
                null, "Singing birds", "singing_birds", false, TYPE_MUSIC,
                240000
            )
        )
        musicList.add(
            Music(
                null, "The house glows", "the_house_glows", false, TYPE_MUSIC,
                336000
            )
        )
        musicList.add(
            Music(
                null, "Water meditation", "water_meditation", false, TYPE_MUSIC,
                232000
            )
        )
        musicList.add(
            Music(
                null, "Zen stones", "zen_stones", true, TYPE_MUSIC,
                getDurationLength(
                    context,
                    R.raw.zen_stones
                )
            )
        )
        musicList.add(
            Music(
                null, "Alarm tone", "alarm_tone", true, TYPE_ALARM,
                getDurationLength(
                    context,
                    R.raw.alarm_tone
                )
            )
        )
        musicList.add(
            Music(
                null, "Clock sound", "clock_sound", true, TYPE_ALARM,
                getDurationLength(
                    context,
                    R.raw.clock_sound
                )
            )
        )
        musicList.add(
            Music(
                null, "Ringing clock", "ringing_clock", true, TYPE_ALARM,
                getDurationLength(
                    context,
                    R.raw.ringing_clock
                )
            )
        )
        musicList.add(
            Music(
                null, "Wake up", "wake_up", true, TYPE_ALARM,
                getDurationLength(
                    context,
                    R.raw.wake_up
                )
            )
        )
        musicList.add(
            Music(
                null, "Piano Zen", "piano_zen", false, TYPE_MUSIC,
                195000
            )
        )
        musicList.add(
            Music(
                null, "Quiet River", "quiet_river", false, TYPE_MUSIC,
                300000
            )
        )
        musicList.add(
            Music(
                null, "Relaxing Harp", "relaxing_harp", false, TYPE_MUSIC,
                190000
            )
        )
        musicList.add(
            Music(
                null, "Singing Bowl", "singing_bowl", false, TYPE_MUSIC,
                192000
            )
        )
        musicList.add(
            Music(
                null, "Sunrise", "sunrise", false, TYPE_MUSIC,
                186000
            )
        )
        musicList.add(
            Music(
                null, "Zen Spring", "zen_spring", false, TYPE_MUSIC,
                170000
            )
        )

        return musicList
    }

    private fun getDurationLength(context: Context, id: Int): Long {
        val mp: MediaPlayer = MediaPlayer.create(context, id)
        return mp.duration.toLong()
    }

}