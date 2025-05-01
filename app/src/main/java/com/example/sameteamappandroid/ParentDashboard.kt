package com.example.sameteamappandroid

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.sameteamappandroid.databinding.ActivityParentDashboardBinding
import org.threeten.bp.LocalDate
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ParentDashboard : AppCompatActivity() {

    private lateinit var binding: ActivityParentDashboardBinding
    private var currentUserId = -1
    private var teamName = ""
    private var currentTeamId: Int? = null
    private var children: List<User> = listOf()
    private var allChores: List<Chore> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("userId", -1)

        if (currentUserId != -1) {
            fetchDashboardData()
        } else {
            showToast("Invalid user ID. Please log in again.")
        }

        binding.buttonCreateTeam.setOnClickListener {
            showPopup(R.layout.popup_create_team)
        }

        binding.buttonJoinTeam.setOnClickListener {
            showPopup(R.layout.popup_join_team)
        }

        binding.buttonAddToTeam.setOnClickListener {
            showPopup(R.layout.popup_add_to_team)
        }

        binding.buttonDatePicker.setOnClickListener {
            openDatePicker()
        }

        binding.buttonClearDate.setOnClickListener {
            displayUpcomingChores()
        }

        binding.buttonLogout.setOnClickListener {
            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun showPopup(layoutId: Int) {
        val dialog = Dialog(this)
        val view = layoutInflater.inflate(layoutId, null)
        dialog.setContentView(view)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        when (layoutId) {
            R.layout.popup_create_team -> {
                val nameField = view.findViewById<EditText>(R.id.editCreateTeamName)
                val passField = view.findViewById<EditText>(R.id.editCreateTeamPassword)
                view.findViewById<Button>(R.id.buttonSubmitCreateTeam).setOnClickListener {
                    val req = CreateTeamRequest(currentUserId, nameField.text.toString(), passField.text.toString())
                    RetrofitClient.instance.createTeam(req).enqueue(object : Callback<Team> {
                        override fun onResponse(call: Call<Team>, response: Response<Team>) {
                            showToast("Team created!")
                            fetchDashboardData()
                            dialog.dismiss()
                        }
                        override fun onFailure(call: Call<Team>, t: Throwable) {
                            showToast("Error: ${t.message}")
                        }
                    })
                }
            }
            R.layout.popup_join_team -> {
                val nameField = view.findViewById<EditText>(R.id.editJoinTeamName)
                val passField = view.findViewById<EditText>(R.id.editJoinTeamPassword)
                view.findViewById<Button>(R.id.buttonSubmitJoinTeam).setOnClickListener {
                    val req = JoinTeamRequest(currentUserId, nameField.text.toString(), passField.text.toString())
                    RetrofitClient.instance.joinTeam(req).enqueue(object : Callback<Team> {
                        override fun onResponse(call: Call<Team>, response: Response<Team>) {
                            showToast("Joined team!")
                            fetchDashboardData()
                            dialog.dismiss()
                        }
                        override fun onFailure(call: Call<Team>, t: Throwable) {
                            showToast("Error: ${t.message}")
                        }
                    })
                }
            }
            R.layout.popup_add_to_team -> {
                val emailField = view.findViewById<EditText>(R.id.editAddUserEmail)
                view.findViewById<Button>(R.id.buttonSubmitAddUser).setOnClickListener {
                    if (currentTeamId != null) {
                        val req = AddUserToTeamRequest(emailField.text.toString(), currentTeamId!!)
                        RetrofitClient.instance.addUserToTeam(req).enqueue(object : Callback<User> {
                            override fun onResponse(call: Call<User>, response: Response<User>) {
                                showToast("User added to team.")
                                fetchUsersThenChores()
                                dialog.dismiss()
                            }
                            override fun onFailure(call: Call<User>, t: Throwable) {
                                showToast("Error: ${t.message}")
                            }
                        })
                    } else {
                        showToast("No team found.")
                    }
                }
            }
        }
    }

    private fun fetchUsersThenChores() {
        RetrofitClient.instance.fetchUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val users = response.body() ?: listOf()
                    val currentUser = users.find { it.userId == currentUserId }
                    if (currentUser == null) {
                        showToast("Current user not found.")
                        return
                    }
                    children = users.filter { it.role == "Child" && it.teamId == currentUser.teamId }
                    fetchChores()
                } else {
                    showToast("Failed to refresh user list.")
                }
            }
            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun fetchDashboardData() {
        RetrofitClient.instance.fetchUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val users = response.body() ?: listOf()
                    val currentUser = users.find { it.userId == currentUserId }

                    if (currentUser == null) {
                        showToast("Current user not found.")
                        return
                    }

                    currentUser.teamId?.let { fetchTeamName(it) }
                    children = users.filter { it.role == "Child" && it.teamId == currentUser.teamId }
                    fetchChores()
                } else {
                    showToast("Failed to load users.")
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun fetchTeamName(teamId: Int) {
        RetrofitClient.instance.fetchTeam(teamId).enqueue(object : Callback<Team> {
            override fun onResponse(call: Call<Team>, response: Response<Team>) {
                if (response.isSuccessful) {
                    teamName = response.body()?.teamName ?: ""
                    currentTeamId = response.body()?.teamId
                    binding.textTeamName.text = "Team: $teamName"
                }
            }

            override fun onFailure(call: Call<Team>, t: Throwable) {
                showToast("Error fetching team: ${t.message}")
            }
        })
    }

    private fun fetchChores() {
        RetrofitClient.instance.fetchChores().enqueue(object : Callback<List<Chore>> {
            override fun onResponse(call: Call<List<Chore>>, response: Response<List<Chore>>) {
                if (response.isSuccessful) {
                    allChores = response.body() ?: listOf()
                    displayChildrenLevels()
                    displayUpcomingChores()
                }
            }

            override fun onFailure(call: Call<List<Chore>>, t: Throwable) {
                showToast("Error loading chores: ${t.message}")
            }
        })
    }

    private fun displayChildrenLevels() {
        binding.layoutChildrenLevels.removeAllViews()

        val thresholds = listOf(0, 200, 400, 600, 1000, 10000)
        val levels = listOf("Beginner", "Rising Star", "Helper Pro", "Superstar", "Legend")
        val colors = listOf("#cccccc", "#ccffcc", "#aaaaff", "#ffffaa", "#ffcc88")

        for (child in children) {
            val points = allChores.filter { it.assignedTo == child.userId && it.completed }.sumOf { it.points }
            val levelIndex = thresholds.indexOfFirst { points < it }.let { if (it > 0) it - 1 else 0 }

            val container = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 8, 8, 8)
            }

            val tv = TextView(this).apply {
                text = "${child.username} - Level ${levelIndex + 1} (${levels[levelIndex]}) - $points pts"
                setBackgroundColor(android.graphics.Color.parseColor(colors[levelIndex]))
                setPadding(12, 12, 12, 12)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val removeBtn = Button(this).apply {
                text = "❌"
                setOnClickListener {
                    AlertDialog.Builder(this@ParentDashboard)
                        .setTitle("Remove Child")
                        .setMessage("Are you sure you want to remove ${child.username}?")
                        .setPositiveButton("Yes") { _, _ ->
                            RetrofitClient.instance.removeUserFromTeam(child.userId)
                                .enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                        if (response.isSuccessful) {
                                            showToast("Removed ${child.username}")
                                            fetchUsersThenChores()
                                        } else {
                                            showToast("Server error: ${response.code()}")
                                            Log.e("REMOVE_FAIL", "Error ${response.code()}: ${response.errorBody()?.string()}")
                                        }
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                        showToast("Failed to remove user: ${t.message}")
                                        Log.e("REMOVE_FAIL", "Network error: ${t.message}")
                                    }
                                })
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }

            container.addView(tv)
            container.addView(removeBtn)
            binding.layoutChildrenLevels.addView(container)
        }
    }






    private fun displayUpcomingChores() {
        val today = LocalDate.now()
        val endDate = today.plusDays(6)

        val childUserIds = children.map { it.userId }

        val filtered = allChores.filter {
            !it.completed &&
                    childUserIds.contains(it.assignedTo) &&
                    LocalDate.parse(it.dateAssigned).let { date -> !date.isBefore(today) && !date.isAfter(endDate) }
        }.sortedBy { it.dateAssigned }

        binding.layoutChoreList.removeAllViews()

        if (filtered.isEmpty()) {
            val tv = TextView(this).apply { text = "No chores." }
            binding.layoutChoreList.addView(tv)
        } else {
            filtered.forEach { chore ->
                val assignedTo = children.find { it.userId == chore.assignedTo }?.username ?: "Unknown"
                val tv = TextView(this).apply {
                    text = "${chore.choreText} - ${chore.dateAssigned} - $assignedTo (${chore.points} pts)"
                    setPadding(0, 8, 0, 8)
                }
                binding.layoutChoreList.addView(tv)
            }
        }
    }


    private fun displayChoresOnDate(date: LocalDate) {
        displayChoresInRange(date, date)
    }

    private fun displayChoresInRange(startDate: LocalDate, endDate: LocalDate) {
        binding.layoutChoreList.removeAllViews()

        val filtered = allChores.filter {
            !it.completed && LocalDate.parse(it.dateAssigned).let { d -> !d.isBefore(startDate) && !d.isAfter(endDate) }
        }.sortedBy { it.dateAssigned }

        if (filtered.isEmpty()) {
            val tv = TextView(this).apply { text = "No chores." }
            binding.layoutChoreList.addView(tv)
        } else {
            filtered.forEach { chore ->
                val tv = TextView(this).apply {
                    text = "${chore.choreText} - ${chore.dateAssigned} (${chore.points} pts)"
                    setPadding(0, 8, 0, 8)
                }
                binding.layoutChoreList.addView(tv)
            }
        }
    }

    private fun openDatePicker() {
        val c = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selected = LocalDate.of(year, month + 1, day)
                displayChoresOnDate(selected)
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}






