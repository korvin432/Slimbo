package com.mindyapps.asleep.ui.more

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mindyapps.asleep.MainActivity
import com.mindyapps.asleep.R
import com.mindyapps.asleep.ui.subs.SubscribeActivity
import kotlinx.android.synthetic.main.fragment_more.*
import java.io.File


class MoreFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        alarm_settings.setOnClickListener(this)
        anti_snore.setOnClickListener(this)
        music_cache.setOnClickListener(this)
        snore_cache.setOnClickListener(this)
        more_apps.setOnClickListener(this)
        feedback.setOnClickListener(this)
        about.setOnClickListener(this)
        subscribe.setOnClickListener(this)
        lucidly_new.setOnClickListener { openProject("lucidlynew") }
        affirmations.setOnClickListener { openProject("affirmations") }
        water_reminder.setOnClickListener { openProject("waterreminder") }

        val subscribed = (requireActivity() as MainActivity).subscribed
        if (subscribed){
            subscribe.visibility = View.GONE
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun openProject(name: String){
        val page: Uri = Uri.parse("https://play.google.com/store/apps/details?id=com.mindyapps.$name")
        val myIntent = Intent(Intent.ACTION_VIEW, page)
        startActivity(myIntent)
    }

    private fun deleteFiles(folder: File) {
        if (folder.isDirectory) {
            val children: Array<String> = folder.list()!!
            children.forEach {
                File(folder, it).delete()
            }
        }
        Toast.makeText(requireContext(), getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.subscribe -> {
                startActivity(Intent(requireContext(), SubscribeActivity::class.java))
            }
            R.id.alarm_settings -> {
                findNavController().navigate(R.id.alarmSettingsFragment)
            }
            R.id.anti_snore -> {
                findNavController().navigate(R.id.antiSnoreFragment)
            }
            R.id.music_cache -> {
                val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                with(builder)
                {
                    setMessage(getString(R.string.delete_music_alert))
                    setPositiveButton(android.R.string.yes) { _, _ ->
                        deleteFiles(File(requireContext().externalCacheDir!!.absolutePath, "Music"))
                    }
                    setNegativeButton(android.R.string.no) { dialog, _ ->
                        dialog.dismiss()
                    }
                    show()
                }
            }
            R.id.snore_cache -> {
                val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                with(builder)
                {
                    setMessage(getString(R.string.delete_snores_alert))
                    setPositiveButton(android.R.string.yes) { _, _ ->
                        deleteFiles(
                            File(requireContext().externalCacheDir!!.absolutePath, "AudioRecorder")
                        )
                    }
                    setNegativeButton(android.R.string.no) { dialog, _ ->
                        dialog.dismiss()
                    }
                    show()
                }
            }
            R.id.more_apps -> {
                val uri = Uri.parse("https://play.google.com/store/apps/dev?id=8188968846352525287")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            R.id.feedback -> {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "message/rfc822"
                i.putExtra(Intent.EXTRA_EMAIL, arrayOf("1nightygames@gmail.com"))
                i.putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.app_name) + " " + getString(R.string.feedback)
                )
                try {
                    startActivity(Intent.createChooser(i, "Email..."))
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.no_email),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            R.id.about -> {
                val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                with(builder)
                {
                    setMessage(getString(R.string.credits))
                    show()
                }
            }
        }
    }
}
