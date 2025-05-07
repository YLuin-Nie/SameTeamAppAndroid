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
    private var showPassword = false

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

        // 🔑 Sign in logic
        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(email, password)
            RetrofitClient.instance.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        loginResponse?.let {
                            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
                            prefs.putString("token", it.token)
                            prefs.putInt("userId", it.user.userId)
                            prefs.putString("role", it.user.role)
                            prefs.apply()

                            val userRole = it.user.role
                            if (userRole == "Child") {
                                startActivity(Intent(this@SignIn, ChildDashboard::class.java))
                            } else {
                                startActivity(Intent(this@SignIn, ParentDashboard::class.java))
                            }
                            finish()
                        }
                    } else {
                        Toast.makeText(this@SignIn, getString(R.string.signup_error_default), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@SignIn, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 🔄 Navigate to SignUp screen
        binding.signUpRedirectTextView.setOnClickListener {
            val intent = Intent(this@SignIn, SignUp::class.java)
            startActivity(intent)
        }
    }
}
