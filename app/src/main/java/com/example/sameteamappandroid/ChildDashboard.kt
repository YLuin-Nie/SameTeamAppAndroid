package com.example.sameteamappandroid

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sameteamappandroid.databinding.ActivityChildDashBoardBinding
import org.threeten.bp.LocalDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChildDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityChildDashBoardBinding
    private var currentUserId: Int = -1
    private var allChores: List<Chore> = listOf()
    private var completedChores: List<Chore> = listOf()
    private var selectedDate: LocalDate? = null
    private val levelThresholds = listOf(0, 200, 400, 600, 1000, 10000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("userId", -1)

        if (currentUserId != -1) {
            fetchChores()
        } else {
            Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
        }

        // 🗓️ Date picker + Clear
        binding.datePickerButton.setOnClickListener {
            val today = LocalDate.now()
            val datePicker = DatePickerDialog(this, { _, year, month, day ->
                selectedDate = LocalDate.of(year, month + 1, day)
                binding.textUpcomingChoresTitle.text = getString(R.string.chores_for_date_format, selectedDate.toString())
                displayChoresForSelectedDate()
            }, today.year, today.monthValue - 1, today.dayOfMonth)
            datePicker.show()
        }

        binding.clearDateButton.setOnClickListener {
            selectedDate = null
            binding.textUpcomingChoresTitle.text = getString(R.string.upcoming_chores)
            displayUpcomingChores()
        }

        // 🔘 Navigation Buttons
        binding.buttonGoDashboard.setOnClickListener {
            startActivity(Intent(this, ChildDashboard::class.java))
            finish()
        }

        binding.buttonGoChores.setOnClickListener {
            startActivity(Intent(this, ChoresList::class.java))
        }

        binding.buttonGoRewards.setOnClickListener {
            startActivity(Intent(this, ChildRewards::class.java))
        }

        binding.buttonGoLogout.setOnClickListener {
            val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun fetchChores() {
        RetrofitClient.instance.fetchChores().enqueue(object : Callback<List<Chore>> {
            override fun onResponse(call: Call<List<Chore>>, response: Response<List<Chore>>) {
                if (response.isSuccessful) {
                    allChores = response.body()?.filter { it.assignedTo == currentUserId } ?: listOf()
                    fetchCompleted()
                }
            }

            override fun onFailure(call: Call<List<Chore>>, t: Throwable) {
                Toast.makeText(this@ChildDashboard, getString(R.string.chore_fail), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCompleted() {
        RetrofitClient.instance.fetchCompletedChores().enqueue(object : Callback<List<Chore>> {
            override fun onResponse(call: Call<List<Chore>>, response: Response<List<Chore>>) {
                if (response.isSuccessful) {
                    completedChores = response.body()?.filter { it.assignedTo == currentUserId } ?: listOf()
                    updateDashboard()
                }
            }

            override fun onFailure(call: Call<List<Chore>>, t: Throwable) {
                Toast.makeText(this@ChildDashboard, getString(R.string.chore_fail), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateDashboard() {
        RetrofitClient.instance.fetchUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val user = response.body()?.find { it.userId == currentUserId }

                    if (user != null) {
                        val totalPoints = user.totalPoints
                        val unspentPoints = user.points

                        val thresholds = listOf(0, 200, 400, 600, 1000, 10000)
                        val levels = listOf("Beginner", "Rising Star", "Helper Pro", "Superstar", "Legend")
                        val colors = listOf("#cccccc", "#ccffcc", "#aaaaff", "#ffffaa", "#ffcc88")

                        val levelIndex = thresholds.indexOfFirst { totalPoints < it }.let { if (it > 0) it - 1 else 0 }

                        binding.totalPointsText.text = getString(R.string.total_points) + " $totalPoints"
                        binding.unspentPointsText.text = getString(R.string.unspent_points_label) + " $unspentPoints"

                        // ℹ️ Show Level Info
                        binding.childLevelInfo.text = "Level ${levelIndex + 1} (${levels[levelIndex]}) - $totalPoints pts"
                        binding.childLevelInfo.setBackgroundColor(android.graphics.Color.parseColor(colors[levelIndex]))
                        binding.childLevelInfo.setPadding(12, 12, 12, 12)

                        binding.progressBar.max = thresholds.getOrElse(levelIndex + 1) { thresholds.last() }
                        binding.progressBar.progress = totalPoints

                        if (selectedDate != null) displayChoresForSelectedDate()
                        else displayUpcomingChores()
                    } else {
                        Toast.makeText(this@ChildDashboard, "User not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ChildDashboard, "Failed to load user info", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@ChildDashboard, "Error fetching user points", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun displayChoresForSelectedDate() {
        binding.upcomingChoresLayout.removeAllViews()

        val filtered = allChores.filter {
            !it.completed && LocalDate.parse(it.dateAssigned) == selectedDate
        }

        if (filtered.isEmpty()) {
            val tv = TextView(this).apply { text = getString(R.string.no_chores_for_date) }
            binding.upcomingChoresLayout.addView(tv)
        } else {
            for (chore in filtered) {
                val layout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 8, 0, 8)
                }

                val tv = TextView(this).apply {
                    text = "${chore.choreText}\nDue: ${chore.dateAssigned}\nPoints: ${chore.points}"
                }

                layout.addView(tv)
                binding.upcomingChoresLayout.addView(layout)
            }
        }
    }

    private fun displayUpcomingChores() {
        val upcoming = allChores.filter { !it.completed }

        binding.upcomingChoresLayout.removeAllViews()

        if (upcoming.isEmpty()) {
            val tv = TextView(this).apply { text = getString(R.string.no_upcoming_chores) }
            binding.upcomingChoresLayout.addView(tv)
        } else {
            for (chore in upcoming.sortedBy { it.dateAssigned }) {
                val layout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 8, 0, 8)
                }

                val tv = TextView(this).apply {
                    text = "${chore.choreText}\nDue: ${chore.dateAssigned}\nPoints: ${chore.points}"
                }

                layout.addView(tv)
                binding.upcomingChoresLayout.addView(layout)
            }
        }
    }


}
