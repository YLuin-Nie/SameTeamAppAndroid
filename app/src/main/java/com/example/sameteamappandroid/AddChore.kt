package com.example.sameteamappandroid

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sameteamappandroid.databinding.ActivityAddChoreBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddChore : AppCompatActivity() {

    private lateinit var binding: ActivityAddChoreBinding
    private lateinit var childList: List<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchUsers()

        binding.addChoreButton.setOnClickListener {
            addChore()
        }
    }

    private fun fetchUsers() {
        RetrofitClient.instance.fetchUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful && response.body() != null) {
                    childList = response.body()!!.filter { it.role == "Child" }
                    val adapter = ArrayAdapter(
                        this@AddChore,
                        android.R.layout.simple_spinner_item,
                        childList.map { it.username }
                    )
                    binding.userSpinner.adapter = adapter
                } else {
                    Toast.makeText(this@AddChore, "Failed to fetch users", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@AddChore, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
            choreText = choreText,
            points = points,
            assignedTo = selectedUser.userId,
            dateAssigned = date,
            completed = false
        )

        RetrofitClient.instance.postChore(chore).enqueue(object : Callback<Chore> {
            override fun onResponse(call: Call<Chore>, response: Response<Chore>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddChore, "Chore added!", Toast.LENGTH_SHORT).show()
                    finish() // optionally refresh instead
                } else {
                    Toast.makeText(this@AddChore, "Failed to add chore", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Chore>, t: Throwable) {
                Toast.makeText(this@AddChore, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
