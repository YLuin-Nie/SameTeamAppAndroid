package com.example.sameteamappandroid

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sameteamappandroid.databinding.ActivityChildDashBoardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class ChildDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChildDashBoardBinding
    private var currentUserId: Int = -1
    private var allChores: List<Chore> = listOf()
    private var points: Int = 0
    private var totalPoints: Int = 0
    private val levelThresholds = listOf(0, 200, 400, 600, 1000, 10000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("userId", -1)

        if (currentUserId != -1) {
            fetchChores()
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchChores() {
        RetrofitClient.instance.fetchChores().enqueue(object : Callback<List<Chore>> {
            override fun onResponse(call: Call<List<Chore>>, response: Response<List<Chore>>) {
                if (response.isSuccessful) {
                    allChores = response.body()?.filter { it.assignedTo == currentUserId } ?: listOf()
                    updateUI()
                }
            }

            override fun onFailure(call: Call<List<Chore>>, t: Throwable) {
                Toast.makeText(this@ChildDashboardActivity, "Failed to load chores", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI() {
        val earned = allChores.filter { it.completed }.sumOf { it.points }
        totalPoints = earned
        points = earned

        binding.totalPointsText.text = getString(R.string.total_points) + " $totalPoints"
        binding.unspentPointsText.text = getString(R.string.unspent_points) + " $points"

        val level = levelThresholds.indexOfFirst { totalPoints < it }.takeIf { it > 0 } ?: levelThresholds.size
        val levelNames = resources.getStringArray(R.array.levels_array).toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, levelNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.levelSpinner.adapter = adapter
        binding.levelSpinner.setSelection(level - 1)
        binding.levelSpinner.isEnabled = false

        binding.progressBar.progress = (totalPoints - levelThresholds[level - 1])

        val today = LocalDate.now()
        val upcoming = allChores.filter {
            !it.completed && LocalDate.parse(it.dateAssigned) in today..today.plusDays(6)
        }.sortedBy { it.dateAssigned }

        binding.upcomingChoresLayout.removeAllViews()

        if (upcoming.isEmpty()) {
            val noTasks = TextView(this).apply {
                text = getString(R.string.no_upcoming_chores)
            }
            binding.upcomingChoresLayout.addView(noTasks)
        } else {
            for (chore in upcoming) {
                val view = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 8, 0, 8)
                }

                val choreText = TextView(this).apply {
                    text = "${chore.choreText}\nDue: ${chore.dateAssigned}\nPoints: ${chore.points}"
                }

                val button = Button(this).apply {
                    text = getString(R.string.complete_button)
                    setOnClickListener { markChoreComplete(chore) }
                }

                view.addView(choreText)
                view.addView(button)
                binding.upcomingChoresLayout.addView(view)
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
                Toast.makeText(this@ChildDashboardActivity, "Failed to complete chore", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
