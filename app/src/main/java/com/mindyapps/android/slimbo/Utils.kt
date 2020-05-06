package com.mindyapps.android.slimbo

import android.content.Context
import android.media.MediaPlayer
import com.mindyapps.android.slimbo.data.db.SlimboDatabase
import com.mindyapps.android.slimbo.data.model.Factor
import com.mindyapps.android.slimbo.data.model.Music
import com.mindyapps.android.slimbo.data.model.TYPE_ALARM
import com.mindyapps.android.slimbo.data.model.TYPE_MUSIC

class Utils(val context:Context) {

     fun minutesToMills(min: Int): Long{
         return (min*1000).toLong()
     }

     fun getFactorsList(): MutableList<Factor>{
        val factors = mutableListOf<Factor>()
        factors.add(Factor(null, "coffee", "ic_coffee"))
        factors.add(Factor(null, "disease", "ic_ill"))
        factors.add(Factor(null, "nose", "ic_nose"))
        factors.add(Factor(null, "shower", "ic_showe"))
        factors.add(Factor(null, "wine", "ic_wine"))
        factors.add(Factor(null, "workout", "ic_workout"))
        return factors
    }

     fun getMusicList(): MutableList<Music>{
        val musicList = mutableListOf<Music>()
        musicList.add(
            Music(null, context.getString(R.string.do_not_use), "", TYPE_MUSIC, 0)
        )
        musicList.add(
            Music(
                null, "Autumn dream lullaby", "autumn_dream_lullaby", TYPE_MUSIC,
                getDurationLength(context, R.raw.autumn_dream_lullaby)
            )
        )
        musicList.add(
            Music(
                null, "Looking at the night sky", "looking_at_the_night_sky", TYPE_MUSIC,
                getDurationLength(context, R.raw.looking_at_the_night_sky)
            )
        )
        musicList.add(
            Music(
                null, "Mystic sea", "mystic_sea", TYPE_MUSIC,
                getDurationLength(context, R.raw.mystic_sea)
            )
        )
        musicList.add(
            Music(
                null, "Ambient Background", "ambient_background", TYPE_MUSIC,
                getDurationLength(context, R.raw.ambient_background)
            )
        )
        musicList.add(
            Music(
                null, "Ambient intro", "ambient_intro", TYPE_MUSIC,
                getDurationLength(context, R.raw.ambient_intro)
            )
        )
        musicList.add(
            Music(
                null, "Asian meditation", "asian_meditation", TYPE_MUSIC,
                getDurationLength(context, R.raw.asian_meditation)
            )
        )
        musicList.add(
            Music(
                null, "Magic Forest", "magic_forest", TYPE_MUSIC,
                getDurationLength(context, R.raw.magic_forest)
            )
        )
        musicList.add(
            Music(
                null, "Meditation manatees", "meditation_manatees", TYPE_MUSIC,
                getDurationLength(context, R.raw.meditation_manatees)
            )
        )
        musicList.add(
            Music(
                null, "Nature music", "nature_music", TYPE_MUSIC,
                getDurationLength(context, R.raw.nature_music)
            )
        )
        musicList.add(
            Music(
                null, "River music", "river_music", TYPE_MUSIC,
                getDurationLength(context, R.raw.river_music)
            )
        )
        musicList.add(
            Music(
                null, "Singing birds", "singing_birds", TYPE_MUSIC,
                getDurationLength(context, R.raw.singing_birds)
            )
        )
        musicList.add(
            Music(
                null, "The house glows", "the_house_glows", TYPE_MUSIC,
                getDurationLength(context, R.raw.the_house_glows)
            )
        )
        musicList.add(
            Music(
                null, "Water meditation", "water_meditation", TYPE_MUSIC,
                getDurationLength(context, R.raw.water_meditation)
            )
        )
        musicList.add(
            Music(
                null, "Zen stones", "zen_stones", TYPE_MUSIC,
                getDurationLength(context, R.raw.zen_stones)
            )
        )
         musicList.add(
             Music(
                 null, "Alarm tone", "alarm_tone", TYPE_ALARM,
                 getDurationLength(context, R.raw.alarm_tone)
             )
         )
         musicList.add(
             Music(
                 null, "Clock sound", "clock_sound", TYPE_ALARM,
                 getDurationLength(context, R.raw.clock_sound)
             )
         )
         musicList.add(
             Music(
                 null, "Ringing clock", "ringing_clock", TYPE_ALARM,
                 getDurationLength(context, R.raw.ringing_clock)
             )
         )
         musicList.add(
             Music(
                 null, "Wake up", "wake_up", TYPE_ALARM,
                 getDurationLength(context, R.raw.wake_up)
             )
         )
        return musicList
    }

    private fun getDurationLength(context: Context, id: Int): Long {
        val mp: MediaPlayer = MediaPlayer.create(context, id)
        return mp.duration.toLong()
    }
}