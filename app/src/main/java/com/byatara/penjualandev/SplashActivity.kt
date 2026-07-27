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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val mainView = findViewById<View>(R.id.text_container).parent as View
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referensi View
        val tvAppName = findViewById<TextView>(R.id.tv_app_name)
        val tvSubtitle = findViewById<TextView>(R.id.tv_subtitle)
        val lineDivider = findViewById<View>(R.id.line_divider)
        val tvFooter = findViewById<TextView>(R.id.tv_footer)

        // Set nama user
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val namaUser = currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Kasir"
        tvSubtitle.text = namaUser

        // Setup Awal Animasi
        tvAppName.translationY = 50f
        tvAppName.alpha = 0f
        tvSubtitle.translationY = 50f
        tvSubtitle.alpha = 0f
        lineDivider.scaleX = 0f
        lineDivider.alpha = 0f
        tvFooter.translationY = 30f
        tvFooter.alpha = 0f

        // Animasi Teks & Garis dengan efek perlahan dan elegan
        tvAppName.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(800)
            .setInterpolator(OvershootInterpolator(1.0f))
            .setStartDelay(200)
            .start()

        tvSubtitle.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(800)
            .setInterpolator(OvershootInterpolator(1.0f))
            .setStartDelay(400)
            .start()
            
        lineDivider.animate()
            .scaleX(1f)
            .alpha(1f)
            .setDuration(600)
            .setInterpolator(OvershootInterpolator(1.5f))
            .setStartDelay(600)
            .start()
            
        tvFooter.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(800)
            .start()

        // Animasi tambahan dihilangkan karena teks sudah meluncur masuk
        // Pindah ke MainActivity atau LoginActivity setelah animasi selesai
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            
            val intent = if (currentUser != null) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
            
            // Efek transisi antar activity yang halus (Fade In)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 3200)
    }
}
