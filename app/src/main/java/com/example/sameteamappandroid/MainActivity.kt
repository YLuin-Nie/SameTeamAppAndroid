package com.example.sameteamappandroid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.sameteamappandroid.databinding.ActivityMainBinding
import com.jakewharton.threetenabp.AndroidThreeTen

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var darkModeEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidThreeTen.init(this)

        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        val role = sharedPref.getString("role", null)

        if (!token.isNullOrEmpty() && !role.isNullOrEmpty()) {
            if (role == "Child") {
                startActivity(Intent(this, ChildDashboard::class.java))
            } else {
                startActivity(Intent(this, ParentDashboard::class.java))
            }
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInButton.setOnClickListener {
            startActivity(Intent(this, SignIn::class.java))
        }

        binding.signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        binding.toggleThemeButton.setOnClickListener {
            darkModeEnabled = !darkModeEnabled
            applyDarkMode(darkModeEnabled)
        }
    }

    private fun applyDarkMode(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
