package com.byatara.penjualandev

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnLogout: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val mainView = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        initViews()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val email = currentUser.email
            tvProfileEmail.text = email
            etEmail.setText(email)
            
            usersRef.child(currentUser.uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val name = snapshot.child("name").value.toString()
                    tvProfileName.text = name
                    etName.setText(name)
                }
            }
        }

        btnSave.setOnClickListener {
            saveProfile()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = android.content.Intent(this, LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupBottomNavigation()
    }

    private fun initViews() {
        tvProfileName = findViewById(R.id.tv_profile_name)
        tvProfileEmail = findViewById(R.id.tv_profile_email)
        etName = findViewById(R.id.et_profile_name)
        etEmail = findViewById(R.id.et_profile_email)
        etNewPassword = findViewById(R.id.et_profile_new_password)
        etConfirmPassword = findViewById(R.id.et_profile_confirm_password)
        btnSave = findViewById(R.id.btn_save_profile)
        btnLogout = findViewById(R.id.btn_logout)
    }

    private fun saveProfile() {
        val newName = etName.text.toString().trim()
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        val currentUser = auth.currentUser ?: return

        if (newName.isEmpty()) {
            etName.error = "Nama tidak boleh kosong"
            return
        }

        btnSave.isEnabled = false
        val updates = mutableMapOf<String, Any>()
        updates["name"] = newName

        usersRef.child(currentUser.uid).updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                tvProfileName.text = newName
                
                if (newPassword.isNotEmpty()) {
                    if (newPassword.length < 6) {
                        etNewPassword.error = "Password minimal 6 karakter"
                        btnSave.isEnabled = true
                        return@addOnCompleteListener
                    }
                    if (newPassword != confirmPassword) {
                        etConfirmPassword.error = "Password tidak cocok"
                        btnSave.isEnabled = true
                        return@addOnCompleteListener
                    }

                    currentUser.updatePassword(newPassword).addOnCompleteListener { pwTask ->
                        btnSave.isEnabled = true
                        if (pwTask.isSuccessful) {
                            Toast.makeText(this, "Profil dan password berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            etNewPassword.text?.clear()
                            etConfirmPassword.text?.clear()
                        } else {
                            Toast.makeText(this, "Gagal memperbarui password: ${pwTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    btnSave.isEnabled = true
                    Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
            } else {
                btnSave.isEnabled = true
                Toast.makeText(this, "Gagal memperbarui profil: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        com.byatara.penjualandev.utils.BottomNavigationHelper.setup(this, R.id.navigation_profile)
    }
}
