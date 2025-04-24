package com.example.sameteamappandroid

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sameteamappandroid.databinding.ActivitySignInBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignIn : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private var showPassword: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 👁 Toggle password visibility
        binding.eyeIcon.setOnClickListener {
            showPassword = !showPassword
            binding.passwordEditText.inputType =
                if (showPassword) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.eyeIcon.setImageResource(
                if (showPassword) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
            )
        }

        // 🔐 Sign in button
        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(email, password)

            RetrofitClient.instance.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val loginResponse = response.body()!!
                        val token = loginResponse.token
                        val user = loginResponse.user

                        // Store token locally
                        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("token", token)
                            putInt("userId", user.userId)
                            putString("role", user.role)
                            putInt("teamId", user.teamId ?: -1)
                            apply()
                        }

                        Toast.makeText(this@SignIn, "Welcome, ${user.username}", Toast.LENGTH_SHORT).show()

                        // Navigate (optional: go to dashboard based on role)
                        val intent = Intent(this@SignIn, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@SignIn, "Login failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@SignIn, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
