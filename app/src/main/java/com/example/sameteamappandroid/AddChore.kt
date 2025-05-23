package com.example.sameteamappandroid

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sameteamappandroid.databinding.ActivityAddChoreBinding
import org.threeten.bp.LocalDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AddChore : AppCompatActivity() {

    private lateinit var binding: ActivityAddChoreBinding
    private lateinit var childList: List<User>
    private lateinit var allChores: List<Chore>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "%04d-%02d-%02d".format(selectedYear, selectedMonth + 1, selectedDay)
                    binding.dateEditText.setText(selectedDate)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        binding.buttonGoDashboard.setOnClickListener {
            startActivity(Intent(this, ParentDashboard::class.java)); finish()
        }
        binding.buttonGoAddChore.setOnClickListener {
            startActivity(Intent(this, AddChore::class.java)); finish()
        }
        binding.buttonGoRewards.setOnClickListener {
            startActivity(Intent(this, ParentRewards::class.java)); finish()
        }
        binding.buttonLogout.setOnClickListener {
            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
            prefs.clear().apply()
            startActivity(Intent(this, MainActivity::class.java)); finish()
        }

        fetchUsers()
        binding.addChoreButton.setOnClickListener { addChore() }
    }

    private fun fetchUsers() {
        RetrofitClient.instance.fetchUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful && response.body() != null) {
                    val allUsers = response.body()!!
                    val currentUserId = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("userId", -1)
                    val currentUser = allUsers.find { it.userId == currentUserId }

                    if (currentUser == null) {
                        Toast.makeText(this@AddChore, getString(R.string.invalid_user), Toast.LENGTH_SHORT).show()
                        return
                    }

                    childList = allUsers.filter { it.role == "Child" && it.teamId == currentUser.teamId }

                    val adapter = ArrayAdapter(
                        this@AddChore,
                        android.R.layout.simple_spinner_item,
                        childList.map { it.username }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.userSpinner.adapter = adapter

                    fetchChores()
                } else {
                    Toast.makeText(this@AddChore, "Failed to fetch users", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@AddChore, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchChores() {
        RetrofitClient.instance.fetchChores().enqueue(object : Callback<List<Chore>> {
            override fun onResponse(call: Call<List<Chore>>, response: Response<List<Chore>>) {
                if (response.isSuccessful) {
                    allChores = response.body() ?: emptyList()
                    displayPendingChores()
                }
            }
            override fun onFailure(call: Call<List<Chore>>, t: Throwable) {
                Toast.makeText(this@AddChore, "Error fetching chores", Toast.LENGTH_SHORT).show()
            }
        })

        RetrofitClient.instance.fetchCompletedChores().enqueue(object : Callback<List<Chore>> {
            override fun onResponse(call: Call<List<Chore>>, response: Response<List<Chore>>) {
                if (response.isSuccessful) {
                    displayCompletedChores(response.body() ?: emptyList())
                }
            }
            override fun onFailure(call: Call<List<Chore>>, t: Throwable) {
                Toast.makeText(this@AddChore, "Error fetching completed chores", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayPendingChores() {
        binding.pendingChoresLayout.removeAllViews()

        val pending = allChores.filter { !it.completed && childList.any { child -> child.userId == it.assignedTo } }
        if (pending.isEmpty()) {
            binding.pendingChoresLayout.addView(TextView(this).apply {
                text = getString(R.string.no_pending)
            })
            return
        }

        for (chore in pending) {
            val assignedChild = childList.find { it.userId == chore.assignedTo }
            val choreText = "${chore.choreText} — ${chore.points} pts — Assigned to: ${assignedChild?.username ?: "Unknown"}"

            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 12, 0, 12)
            }

            val textView = TextView(this).apply { text = choreText }

            val buttonLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val editButton = Button(this).apply {
                text = getString(R.string.edit)
                setOnClickListener { showEditChorePopup(chore) }
            }

            val completeButton = Button(this).apply {
                text = getString(R.string.complete_button)
                setOnClickListener { completeChore(chore.choreId) }
            }

            val deleteButton = Button(this).apply {
                text = getString(R.string.delete)
                setOnClickListener { deleteChore(chore.choreId) }
            }

            buttonLayout.addView(editButton)
            buttonLayout.addView(completeButton)
            buttonLayout.addView(deleteButton)

            itemLayout.addView(textView)
            itemLayout.addView(buttonLayout)
            binding.pendingChoresLayout.addView(itemLayout)
        }
    }

    private fun displayCompletedChores(completed: List<Chore>) {
        binding.completedChoresLayout.removeAllViews()

        val recent = completed.filter {
            LocalDate.parse(it.dateAssigned).isAfter(LocalDate.now().minusDays(8)) &&
                    childList.any { child -> child.userId == it.assignedTo }
        }

        if (recent.isEmpty()) {
            binding.completedChoresLayout.addView(TextView(this).apply {
                text = getString(R.string.no_completed)
            })
            return
        }

        for (chore in recent) {
            val assignedChild = childList.find { it.userId == chore.assignedTo }
            val choreText = "${chore.choreText} — ${chore.points} pts — ${assignedChild?.username ?: "Unknown"}"

            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 12, 0, 12)
            }

            val textView = TextView(this).apply {
                text = choreText
                paint.isStrikeThruText = true
            }

            val button = Button(this).apply {
                text = getString(R.string.undo_button)
                setOnClickListener { undoCompletedChore(chore.choreId) }
            }

            itemLayout.addView(textView)
            itemLayout.addView(button)
            binding.completedChoresLayout.addView(itemLayout)
        }
    }

    private fun addChore() {
        val choreText = binding.choreTextEditText.text.toString().trim()
        val points = binding.pointsEditText.text.toString().toIntOrNull() ?: 10
        val date = binding.dateEditText.text.toString().trim()
        val selectedIndex = binding.userSpinner.selectedItemPosition

        if (choreText.isEmpty() || selectedIndex < 0 || date.isEmpty()) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedUser = childList[selectedIndex]
        val chore = Chore(
            choreId = 0,
            choreText = choreText,
            points = points,
            assignedTo = selectedUser.userId,
            dateAssigned = date,
            completed = false
        )

        RetrofitClient.instance.postChore(chore).enqueue(object : Callback<Chore> {
            override fun onResponse(call: Call<Chore>, response: Response<Chore>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddChore, getString(R.string.chore_success), Toast.LENGTH_SHORT).show()
                    fetchChores()
                } else {
                    Toast.makeText(this@AddChore, getString(R.string.chore_fail), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Chore>, t: Throwable) {
                Toast.makeText(this@AddChore, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun completeChore(choreId: Int) {
        RetrofitClient.instance.moveChoreToCompleted(choreId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                fetchChores()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AddChore, "Failed to complete chore", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun undoCompletedChore(completedId: Int) {
        RetrofitClient.instance.undoCompletedChore(completedId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                fetchChores()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AddChore, "Failed to undo chore", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteChore(choreId: Int) {
        RetrofitClient.instance.deleteChore(choreId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                fetchChores()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AddChore, "Delete failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEditChorePopup(chore: Chore) {
            val dialog = Dialog(this)
            val view = layoutInflater.inflate(R.layout.popup_edit_chore, null)
            dialog.setContentView(view)

            val nameField = view.findViewById<EditText>(R.id.editChoreName)
            val pointsField = view.findViewById<EditText>(R.id.editChorePoints)
            val dateField = view.findViewById<EditText>(R.id.editChoreDate)
            val spinner = view.findViewById<Spinner>(R.id.editChoreSpinner)
            val submitButton = view.findViewById<Button>(R.id.buttonSubmitEditChore)

            nameField.setText(chore.choreText)
            pointsField.setText(chore.points.toString())
            dateField.setText(chore.dateAssigned)

            // Populate spinner with child usernames
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, childList.map { it.username })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            // Pre-select the assigned child
            val selectedIndex = childList.indexOfFirst { it.userId == chore.assignedTo }
            if (selectedIndex != -1) {
                spinner.setSelection(selectedIndex)
            }

            // Optional: add DatePicker to dateField
            dateField.setOnClickListener {
                val calendar = Calendar.getInstance()
                val parts = chore.dateAssigned.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt() - 1
                val day = parts[2].toInt()

                DatePickerDialog(this, { _, y, m, d ->
                    val selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                    dateField.setText(selectedDate)
                }, year, month, day).show()
            }

            submitButton.setOnClickListener {
                val newAssignedUser = childList[spinner.selectedItemPosition]

                val updated = chore.copy(
                    choreText = nameField.text.toString().trim(),
                    points = pointsField.text.toString().toIntOrNull() ?: chore.points,
                    dateAssigned = dateField.text.toString().trim(),
                    assignedTo = newAssignedUser.userId
                )

                RetrofitClient.instance.updateChore(updated.choreId, updated).enqueue(object : Callback<Chore> {
                    override fun onResponse(call: Call<Chore>, response: Response<Chore>) {
                        fetchChores()
                        dialog.dismiss()
                    }

                    override fun onFailure(call: Call<Chore>, t: Throwable) {
                        Toast.makeText(this@AddChore, "Edit failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            val cancelButton = view.findViewById<Button>(R.id.buttonCancelEditChore)
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
    }
}
