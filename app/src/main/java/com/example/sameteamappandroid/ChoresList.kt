package com.example.sameteamappandroid

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sameteamappandroid.databinding.ActivityChoresListBinding
import org.threeten.bp.LocalDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChoresList : AppCompatActivity() {

    private lateinit var binding: ActivityChoresListBinding
    private var currentUserId: Int = -1
    private var allChores: List<Chore> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChoresListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        currentUserId = prefs.getInt("userId", -1)

        if (currentUserId != -1) {
            fetchChores()
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
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
            prefs.edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun fetchChores() {
        RetrofitClient.instance.fetchChores().enqueue(object : Callback<List<Chore>> {
            override fun onResponse(call: Call<List<Chore>>, response: Response<List<Chore>>) {
                if (response.isSuccessful) {
                    allChores = response.body()?.filter { it.assignedTo == currentUserId } ?: emptyList()

                    // 🟡 Fetch completed chores from CompletedChores table
                    RetrofitClient.instance.fetchCompletedChores().enqueue(object : Callback<List<Chore>> {
                        override fun onResponse(call: Call<List<Chore>>, completedRes: Response<List<Chore>>) {
                            if (completedRes.isSuccessful) {
                                val completedChores = completedRes.body()?.filter { it.assignedTo == currentUserId } ?: listOf()


                                // 🟡 Fetch redeemed rewards
                                RetrofitClient.instance.getUser(currentUserId).enqueue(object : Callback<User> {
                                    override fun onResponse(call: Call<User>, response: Response<User>) {
                                        if (response.isSuccessful) {
                                            val user = response.body()
                                            val points = user?.points ?: 0
                                            binding.pointsTextView.text = getString(R.string.your_points) + " $points"
                                        } else {
                                            binding.pointsTextView.text = getString(R.string.your_points) + " 0"
                                        }
                                    }

                                    override fun onFailure(call: Call<User>, t: Throwable) {
                                        binding.pointsTextView.text = getString(R.string.your_points) + " 0"
                                    }
                                })

                                displayChores(completedChores)
                            }
                        }

                        override fun onFailure(call: Call<List<Chore>>, t: Throwable) {
                            Toast.makeText(this@ChoresList, "Error loading completed chores", Toast.LENGTH_SHORT).show()
                        }
                    })

                } else {
                    Toast.makeText(this@ChoresList, "Failed to load chores", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Chore>>, t: Throwable) {
                Toast.makeText(this@ChoresList, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayChores(completedChores: List<Chore>) {
        val today = LocalDate.now()
        val sevenDaysAgo = today.minusDays(7)

        val pending = allChores.filter { !it.completed }
        val recentCompleted = completedChores.filter { LocalDate.parse(it.dateAssigned) >= sevenDaysAgo }

        val completionPercent = if (allChores.isNotEmpty() && allChores.all { it.completed }) 100 else 0
        binding.progressTextView.text = getString(R.string.task_progress) + " $completionPercent%"
        binding.progressBar.progress = completionPercent

        binding.pendingLayout.removeAllViews()
        binding.completedLayout.removeAllViews()

        if (pending.isEmpty()) {
            binding.pendingLayout.addView(TextView(this).apply {
                text = getString(R.string.no_pending)
            })
        } else {
            pending.forEach { chore ->
                binding.pendingLayout.addView(createChoreItem(chore, true))
            }
        }

        if (recentCompleted.isEmpty()) {
            binding.completedLayout.addView(TextView(this).apply {
                text = getString(R.string.no_completed)
            })
        } else {
            recentCompleted.forEach { chore ->
                binding.completedLayout.addView(createChoreItem(chore, false))
            }
        }
    }

    private fun createChoreItem(chore: Chore, isPending: Boolean): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 16)
        }

        val choreText = TextView(this).apply {
            text = "${chore.choreText} (${chore.points} pts)"
            if (!isPending) paint.isStrikeThruText = true
        }

        val dateText = TextView(this).apply {
            text = if (isPending) "Due: ${chore.dateAssigned}" else "Completed on: ${chore.dateAssigned}"
        }

        val actionButton = Button(this).apply {
            text = getString(if (isPending) R.string.complete_button else R.string.undo_button)
            setOnClickListener { toggleCompletion(chore, isPending) }
        }

        layout.addView(choreText)
        layout.addView(dateText)
        layout.addView(actionButton)
        return layout
    }

    private fun toggleCompletion(chore: Chore, isPending: Boolean) {
        if (isPending) {
            RetrofitClient.instance.moveChoreToCompleted(chore.choreId)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) fetchChores()
                        else Toast.makeText(this@ChoresList, "Failed to complete chore", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@ChoresList, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            RetrofitClient.instance.undoCompletedChore(chore.choreId)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) fetchChores()
                        else Toast.makeText(this@ChoresList, "Failed to undo chore", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@ChoresList, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
