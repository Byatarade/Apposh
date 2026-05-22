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
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    private lateinit var nameLayout: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvLoginLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

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
        nameLayout = findViewById(R.id.name_layout)
        etName = findViewById(R.id.et_name)
        emailLayout = findViewById(R.id.email_layout)
        etEmail = findViewById(R.id.et_email)
        passwordLayout = findViewById(R.id.password_layout)
        etPassword = findViewById(R.id.et_password)
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnRegister = findViewById(R.id.btn_register)
        tvLoginLink = findViewById(R.id.tv_login_link)
    }

    private fun setupListeners() {
        btnRegister.setOnClickListener {
            registerUser()
        }

        tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        var isValid = true

        if (name.isEmpty()) {
            nameLayout.error = "Nama lengkap tidak boleh kosong"
            isValid = false
        } else {
            nameLayout.error = null
        }

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
        } else if (password.length < 6) {
            passwordLayout.error = "Password minimal 6 karakter"
            isValid = false
        } else {
            passwordLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.error = "Konfirmasi password tidak boleh kosong"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordLayout.error = "Password tidak cocok"
            isValid = false
        } else {
            confirmPasswordLayout.error = null
        }

        if (!isValid) return

        btnRegister.isEnabled = false
        Toast.makeText(this, "Mendaftarkan akun...", Toast.LENGTH_SHORT).show()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid ?: return@addOnCompleteListener

                    // Simpan data profil user ke Realtime Database
                    val userMap = hashMapOf(
                        "userId" to userId,
                        "name" to name,
                        "email" to email
                    )

                    usersRef.child(userId).setValue(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            btnRegister.isEnabled = true
                            Toast.makeText(this, "Gagal menyimpan profil: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    btnRegister.isEnabled = true
                    val errorMsg = task.exception?.message ?: "Registrasi gagal"
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
    }
}
