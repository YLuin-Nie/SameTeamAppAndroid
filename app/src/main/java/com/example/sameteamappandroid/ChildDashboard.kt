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
                displayChoresForSelectedDate()
            }, today.year, today.monthValue - 1, today.dayOfMonth)
            datePicker.show()
        }

        binding.clearDateButton.setOnClickListener {
            selectedDate = null
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
        val earnedPoints = completedChores.sumOf { it.points }
        val levelIndex = levelThresholds.indexOfFirst { earnedPoints < it }.takeIf { it > 0 } ?: levelThresholds.size
        val levelNames = resources.getStringArray(R.array.levels_array)

        binding.totalPointsText.text = getString(R.string.total_points) + " $earnedPoints"
        binding.unspentPointsText.text = getString(R.string.unspent_points_label) + " $earnedPoints"

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, levelNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.levelSpinner.adapter = adapter
        binding.levelSpinner.setSelection(levelIndex - 1)
        binding.levelSpinner.isEnabled = false

        binding.progressBar.max = levelThresholds.getOrElse(levelIndex) { levelThresholds.last() }
        binding.progressBar.progress = earnedPoints

        if (selectedDate != null) displayChoresForSelectedDate() else displayUpcomingChores()
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

                val btn = Button(this).apply {
                    text = getString(R.string.complete_button)
                    setOnClickListener { markChoreComplete(chore) }
                }

                layout.addView(tv)
                layout.addView(btn)
                binding.upcomingChoresLayout.addView(layout)
            }
        }
    }

    private fun displayUpcomingChores() {
        val today = LocalDate.now()
        val upcoming = allChores.filter {
            !it.completed && LocalDate.parse(it.dateAssigned) in today..today.plusDays(6)
        }.sortedBy { it.dateAssigned }

        binding.upcomingChoresLayout.removeAllViews()

        if (upcoming.isEmpty()) {
            val tv = TextView(this).apply { text = getString(R.string.no_upcoming_chores) }
            binding.upcomingChoresLayout.addView(tv)
        } else {
            for (chore in upcoming) {
                val layout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 8, 0, 8)
                }

                val tv = TextView(this).apply {
                    text = "${chore.choreText}\nDue: ${chore.dateAssigned}\nPoints: ${chore.points}"
                }

                val btn = Button(this).apply {
                    text = getString(R.string.complete_button)
                    setOnClickListener { markChoreComplete(chore) }
                }

                layout.addView(tv)
                layout.addView(btn)
                binding.upcomingChoresLayout.addView(layout)
            }
        }
    }

    private fun markChoreComplete(chore: Chore) {
        val updated = chore.copy(completed = true)
        RetrofitClient.instance.completeChore(chore.choreId, updated).enqueue(object : Callback<Chore> {
            override fun onResponse(call: Call<Chore>, response: Response<Chore>) {
                if (response.isSuccessful) fetchChores()
            }

            override fun onFailure(call: Call<Chore>, t: Throwable) {
                Toast.makeText(this@ChildDashboard, getString(R.string.chore_fail), Toast.LENGTH_SHORT).show()
            }
        })
    }
}
