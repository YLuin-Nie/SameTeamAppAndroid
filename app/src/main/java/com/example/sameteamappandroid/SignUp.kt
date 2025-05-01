package com.example.sameteamappandroid

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sameteamappandroid.databinding.ActivitySignUpBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private var role = ""
    private var loading = false
    private var showPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
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

        // 🎭 Role selection
        binding.roleParentButton.setOnClickListener {
            role = "Parent"
            binding.roleParentButton.isSelected = true
            binding.roleChildButton.isSelected = false
        }

        binding.roleChildButton.setOnClickListener {
            role = "Child"
            binding.roleParentButton.isSelected = false
            binding.roleChildButton.isSelected = true
        }

        // 🚀 Sign up logic
        binding.signUpButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()

            // 🔐 Input validation
            if (username.isEmpty() || email.isEmpty() || password.length < 6 || role.isEmpty()) {
                Toast.makeText(this, "Please complete all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ⏳ UI feedback
            loading = true
            binding.signUpButton.isEnabled = false
            Toast.makeText(this, "Processing...", Toast.LENGTH_SHORT).show()

            val request = RegisterRequest(
                username = username,
                email = email,
                password = password,
                role = role,
                team = null,
                teamPassword = null
            )

            Log.d("SIGNUP_REQUEST", "Sending: $request")

            RetrofitClient.instance.register(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    loading = false
                    binding.signUpButton.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@SignUp, getString(R.string.sign_up_success), Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string()
                        Log.e("SIGNUP_FAIL", "Code: ${response.code()}, Body: $errorMsg")
                        Toast.makeText(this@SignUp, "Failed: ${response.code()} ${errorMsg}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    loading = false
                    binding.signUpButton.isEnabled = true
                    Log.e("SIGNUP_ERROR", "Network failure: ${t.message}", t)
                    Toast.makeText(this@SignUp, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}
