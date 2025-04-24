package com.example.sameteamappandroid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.sameteamappandroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var darkModeEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load dark mode state
        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        darkModeEnabled = sharedPref.getBoolean("darkMode", false)
        applyDarkMode(darkModeEnabled)

        // Toggle dark mode
        binding.toggleThemeButton.setOnClickListener {
            darkModeEnabled = !darkModeEnabled
            applyDarkMode(darkModeEnabled)
            sharedPref.edit().putBoolean("darkMode", darkModeEnabled).apply()
        }

        // Navigate to Sign In (placeholder activity)
        binding.signInButton.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }

        // Navigate to Sign Up (placeholder activity)
        binding.signUpButton.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }

    private fun applyDarkMode(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
