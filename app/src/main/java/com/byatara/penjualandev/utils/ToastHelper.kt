package com.byatara.penjualandev.utils

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.byatara.penjualandev.R

object ToastHelper {

    /**
     * Menampilkan Toast kustom premium dengan logo Apposh
     */
    fun showToast(context: Context, message: String) {
        try {
            val inflater = LayoutInflater.from(context)
            val layout = inflater.inflate(R.layout.layout_custom_toast, null)

            val imgIcon = layout.findViewById<ImageView>(R.id.img_toast_icon)
            val tvMessage = layout.findViewById<TextView>(R.id.tv_toast_message)

            imgIcon.setImageResource(R.drawable.logo)
            tvMessage.text = message

            val toast = Toast(context.applicationContext)
            toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 200)
            toast.duration = Toast.LENGTH_SHORT
            toast.view = layout
            toast.show()
        } catch (e: Exception) {
            // Fallback ke toast standar jika terjadi kendala saat inflasi view kustom
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
