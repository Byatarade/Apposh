package com.byatara.penjualandev

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var emailLayout: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvRegisterLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val mainView = findViewById<View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        emailLayout = findViewById(R.id.email_layout)
        etEmail = findViewById(R.id.et_email)
        passwordLayout = findViewById(R.id.password_layout)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        tvRegisterLink = findViewById(R.id.tv_register_link)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            loginUser()
        }

        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        var isValid = true

        if (email.isEmpty()) {
            emailLayout.error = "Email tidak boleh kosong"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Format email tidak valid"
            isValid = false
        } else {
            emailLayout.error = null
        }

        if (password.isEmpty()) {
            passwordLayout.error = "Password tidak boleh kosong"
            isValid = false
        } else {
            passwordLayout.error = null
        }

        if (!isValid) return

        btnLogin.isEnabled = false
        Toast.makeText(this, "Mencoba masuk...", Toast.LENGTH_SHORT).show()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                btnLogin.isEnabled = true
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Selamat datang!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    val errorMsg = task.exception?.message ?: "Login gagal"
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
    }
}
