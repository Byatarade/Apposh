package com.byatara.penjualandev

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
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
        val cartContainer = findViewById<FrameLayout>(R.id.cart_container)
        val imgCart = findViewById<ImageView>(R.id.img_cart)
        val tvAppName = findViewById<TextView>(R.id.tv_app_name)
        val tvSubtitle = findViewById<TextView>(R.id.tv_subtitle)
        
        val speedLine1 = findViewById<View>(R.id.speed_line_1)
        val speedLine2 = findViewById<View>(R.id.speed_line_2)
        val speedLine3 = findViewById<View>(R.id.speed_line_3)

        // Setup Awal Animasi
        cartContainer.translationX = -800f // Mulai dari luar layar sebelah kiri
        
        tvAppName.translationY = 30f
        tvAppName.alpha = 0f
        tvSubtitle.translationY = 30f
        tvSubtitle.alpha = 0f

        // 1. Animasi Keranjang Meluncur Cepat (Ngebut) dari Kiri ke Tengah
        cartContainer.animate()
            .translationX(0f)
            .setDuration(800)
            .setInterpolator(OvershootInterpolator(1.5f)) // Memantul saat ngerem
            .setStartDelay(300)
            .withEndAction {
                // Efek "mesin menyala/bergetar" (Wobble) saat keranjang berhenti
                val wobble = PropertyValuesHolder.ofFloat(View.ROTATION, 0f, -3f, 3f, 0f)
                val wobbleAnim = ObjectAnimator.ofPropertyValuesHolder(imgCart, wobble)
                wobbleAnim.duration = 400
                wobbleAnim.repeatCount = ValueAnimator.INFINITE
                wobbleAnim.start()
            }
            .start()

        // 2. Animasi Garis Angin (Speed Lines) untuk Efek Ngebut
        fun animateSpeedLine(line: View, durationMs: Long, delayMs: Long) {
            val windAnim = ObjectAnimator.ofFloat(line, View.TRANSLATION_X, 800f, -800f)
            windAnim.duration = durationMs
            windAnim.startDelay = delayMs
            windAnim.interpolator = LinearInterpolator()
            windAnim.repeatCount = ValueAnimator.INFINITE
            windAnim.start()
        }

        animateSpeedLine(speedLine1, 600, 400)
        animateSpeedLine(speedLine2, 500, 550)
        animateSpeedLine(speedLine3, 700, 600)

        // 3. Jalankan Animasi Teks Muncul
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
        }, 3200)
    }
}
