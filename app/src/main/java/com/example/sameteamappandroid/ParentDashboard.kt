package com.example.sameteamappandroid

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sameteamappandroid.databinding.ActivityParentDashboardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class ParentDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityParentDashboardBinding
    private var currentUserId: Int = -1
    private var teamName: String = ""
    private var children: List<User> = listOf()
    private var allChores: List<Chore> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("userId", -1)

        if (currentUserId != -1) {
            fetchDashboardData()
        }

        binding.showAddChildButton.setOnClickListener {
            binding.addChildLayout.visibility = View.VISIBLE
        }

        binding.cancelAddChildButton.setOnClickListener {
            binding.addChildLayout.visibility = View.GONE
        }

        binding.submitAddChildButton.setOnClickListener {
            val childEmail = binding.childEmailEditText.text.toString().trim()
            if (childEmail.isNotEmpty()) addChild(childEmail)
        }
    }

    private fun fetchDashboardData() {
        RetrofitClient.instance.fetchUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, userResponse: Response<List<User>>) {
                if (userResponse.isSuccessful) {
                    val allUsers = userResponse.body() ?: listOf()
                    val parent = allUsers.find { it.userId == currentUserId }

                    if (parent?.teamId != null) {
                        RetrofitClient.instance.fetchTeam(parent.teamId).enqueue(object : Callback<Team> {
                            override fun onResponse(call: Call<Team>, teamResponse: Response<Team>) {
                                if (teamResponse.isSuccessful) {
                                    teamName = teamResponse.body()?.teamName ?: ""
                                    binding.teamNameTextView.text = "Team: $teamName"
                                }
                            }

                            override fun onFailure(call: Call<Team>, t: Throwable) {}
                        })
                    }

                    children = allUsers.filter { it.role == "Child" && it.parentId == currentUserId }
                    fetchChores()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {}
        })
    }

    private fun fetchChores() {
        RetrofitClient.instance.fetchChores().enqueue(object : Callback<List<Chore>> {
            override fun onResponse(call: Call<List<Chore>>, choreResponse: Response<List<Chore>>) {
                if (choreResponse.isSuccessful) {
                    allChores = choreResponse.body() ?: listOf()
                    displayChildrenLevels()
                    displayUpcomingChores()
                }
            }

            override fun onFailure(call: Call<List<Chore>>, t: Throwable) {}
        })
    }

    private fun displayChildrenLevels() {
        binding.childrenListLayout.removeAllViews()
        val levels = listOf("Beginner", "Rising Star", "Helper Pro", "Superstar", "Legend")
        val thresholds = listOf(0, 200, 400, 600, 1000, 10000)
        val colors = listOf("#cccccc", "#aaaaff", "#88ff88", "#ffffaa", "#ffcc88")

        for (child in children) {
            val childChores = allChores.filter { it.assignedTo == child.userId && it.completed }
            val points = childChores.sumOf { it.points }

            val levelIndex = thresholds.indexOfFirst { points < it }.let { if (it > 0) it - 1 else 0 }

            val view = TextView(this).apply {
                text = "${child.username} - Level ${levelIndex + 1} (${levels[levelIndex]}) - $points pts"
                setPadding(0, 8, 0, 8)
                setBackgroundColor(android.graphics.Color.parseColor(colors[levelIndex]))
            }

            binding.childrenListLayout.addView(view)
        }
    }

    private fun displayUpcomingChores() {
        binding.choresListLayout.removeAllViews()

        val today = LocalDate.now()
        val next7Days = today..today.plusDays(6)

        val upcoming = allChores.filter {
            !it.completed && LocalDate.parse(it.dateAssigned) in next7Days
        }

        if (upcoming.isEmpty()) {
            val empty = TextView(this).apply { text = getString(R.string.no_upcoming_chores) }
            binding.choresListLayout.addView(empty)
        } else {
            for (chore in upcoming.sortedBy { it.dateAssigned }) {
                val text = TextView(this).apply {
                    text = "${chore.choreText} - Due: ${chore.dateAssigned} (User: ${chore.assignedTo})"
                    setPadding(0, 8, 0, 8)
                }
                binding.choresListLayout.addView(text)
            }
        }
    }

    private fun addChild(email: String) {
        RetrofitClient.instance.addChild(email, currentUserId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ParentDashboard, "Child added!", Toast.LENGTH_SHORT).show()
                    binding.childEmailEditText.text.clear()
                    binding.addChildLayout.visibility = View.GONE
                    fetchDashboardData()
                } else {
                    Toast.makeText(this@ParentDashboard, "Failed to add child.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@ParentDashboard, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
