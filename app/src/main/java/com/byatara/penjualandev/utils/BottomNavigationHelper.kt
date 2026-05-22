package com.byatara.penjualandev.utils

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import com.byatara.penjualandev.HistoriActivity
import com.byatara.penjualandev.MainActivity
import com.byatara.penjualandev.R
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavigationHelper {

    /**
     * Mengatur Bottom Navigation secara dinamis dan membagikan logika navigasi.
     * @param activity Activity yang sedang aktif saat ini.
     * @param activeItemId Menu ID yang harus ditandai sebagai aktif/dipilih.
     */
    fun setup(activity: Activity, activeItemId: Int) {
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation) ?: return

        // Set item aktif saat ini tanpa memicu listener (dihentikan sementara agar tidak rekursif)
        bottomNav.setOnItemSelectedListener(null)
        bottomNav.selectedItemId = activeItemId

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == activeItemId) {
                return@setOnItemSelectedListener true
            }

            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    activity.startActivity(intent)
                    if (activity !is MainActivity) {
                        activity.finish()
                    }
                    true
                }
                R.id.navigation_analytics -> {
                    val intent = Intent(activity, HistoriActivity::class.java)
                    activity.startActivity(intent)
                    if (activity !is MainActivity) {
                        activity.finish()
                    }
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(activity, com.byatara.penjualandev.ProfileActivity::class.java)
                    activity.startActivity(intent)
                    if (activity !is MainActivity) {
                        activity.finish()
                    }
                    true
                }
                R.id.exit -> {
                    showExitDialog(activity)
                    false // false agar ikon exit tidak terpilih secara visual
                }
                else -> true
            }
        }
    }

    private fun showExitDialog(activity: Activity) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.activity_exit)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            activity.finishAffinity()
        }

        dialog.show()
    }
}
