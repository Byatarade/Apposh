package com.byatara.penjualandev

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Tetap pastikan mode terang global dari awal aplikasi dijalankan
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val mainView = findViewById<View>(R.id.animation_container).parent as View
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referensi View
        val imgReceipt = findViewById<LinearLayout>(R.id.img_receipt)
        val tvAppName = findViewById<TextView>(R.id.tv_app_name)
        val tvSubtitle = findViewById<TextView>(R.id.tv_subtitle)

        // Setup Awal Animasi (Sembunyikan Struk di bawah printer)
        imgReceipt.translationY = 100f
        imgReceipt.alpha = 0f
        
        tvAppName.translationY = 30f
        tvSubtitle.translationY = 30f

        // Jalankan Animasi Struk Keluar dari Printer
        imgReceipt.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(1200)
            .setInterpolator(OvershootInterpolator(1.2f))
            .setStartDelay(500)
            .start()

        // Jalankan Animasi Teks Muncul
        tvAppName.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(1000)
            .start()

        tvSubtitle.animate()
            .translationY(0f)
            .alpha(0.8f) // Opacity 80%
            .setDuration(800)
            .setStartDelay(1200)
            .start()

        // Pindah ke MainActivity setelah animasi selesai
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            
            // Efek transisi antar activity yang halus (Fade In)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 2800)
    }
}
