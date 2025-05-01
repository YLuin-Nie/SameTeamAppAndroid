package com.example.sameteamappandroid

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sameteamappandroid.databinding.ActivityParentRewardsBinding
import org.threeten.bp.LocalDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ParentRewards : AppCompatActivity() {

    private lateinit var binding: ActivityParentRewardsBinding
    private lateinit var children: List<User>
    private lateinit var rewards: MutableList<Reward>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchUsersAndRewards()

        // Popup buttons
        binding.buttonOpenRewardPopup.setOnClickListener {
            showPopup(R.layout.popup_reward_child)
        }

        binding.buttonOpenAddRewardPopup.setOnClickListener {
            showPopup(R.layout.popup_add_reward)
        }

        // Navigation
        binding.buttonGoDashboard.setOnClickListener {
            startActivity(Intent(this, ParentDashboard::class.java))
            finish()
        }

        binding.buttonGoAddChore.setOnClickListener {
            startActivity(Intent(this, AddChore::class.java))
        }

        binding.buttonGoRewards.setOnClickListener {
            startActivity(Intent(this, ParentRewards::class.java))
            finish()
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
            R.layout.popup_reward_child -> {
                val spinner = view.findViewById<Spinner>(R.id.popupChildSpinner)
                val nameField = view.findViewById<EditText>(R.id.popupRewardName)
                val pointsField = view.findViewById<EditText>(R.id.popupRewardPoints)
                val button = view.findViewById<Button>(R.id.buttonSubmitReward)

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, children.map { it.username })
                spinner.adapter = adapter

                button.setOnClickListener {
                    val index = spinner.selectedItemPosition
                    val name = nameField.text.toString().trim()
                    val points = pointsField.text.toString().toIntOrNull() ?: 0

                    if (index < 0 || name.isEmpty() || points <= 0) {
                        showToast("Please fill in all fields.")
                        return@setOnClickListener
                    }

                    val chore = Chore(
                        choreId = 0,
                        choreText = name,
                        points = points,
                        assignedTo = children[index].userId,
                        dateAssigned = LocalDate.now().toString(),
                        completed = false
                    )

                    RetrofitClient.instance.rewardAsChore(chore).enqueue(object : Callback<Chore> {
                        override fun onResponse(call: Call<Chore>, response: Response<Chore>) {
                            showToast("Rewarded!")
                            dialog.dismiss()
                        }

                        override fun onFailure(call: Call<Chore>, t: Throwable) {
                            showToast("Error: ${t.message}")
                        }
                    })
                }
            }

            R.layout.popup_add_reward -> {
                val nameField = view.findViewById<EditText>(R.id.popupNewRewardName)
                val costField = view.findViewById<EditText>(R.id.popupNewRewardCost)
                val button = view.findViewById<Button>(R.id.buttonSubmitNewReward)

                button.setOnClickListener {
                    val name = nameField.text.toString().trim()
                    val cost = costField.text.toString().toIntOrNull() ?: 0

                    if (name.isEmpty() || cost <= 0) {
                        showToast("Please enter valid name and cost.")
                        return@setOnClickListener
                    }

                    val reward = Reward(0, name, cost)
                    RetrofitClient.instance.postReward(reward).enqueue(object : Callback<Reward> {
                        override fun onResponse(call: Call<Reward>, response: Response<Reward>) {
                            if (response.isSuccessful) {
                                rewards.add(response.body()!!)
                                displayRewards()
                                showToast("Reward added.")
                                dialog.dismiss()
                            }
                        }

                        override fun onFailure(call: Call<Reward>, t: Throwable) {
                            showToast("Error: ${t.message}")
                        }
                    })
                }
            }
        }
    }

    private fun fetchUsersAndRewards() {
        RetrofitClient.instance.fetchUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    children = response.body()?.filter { it.role == "Child" } ?: emptyList()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {}
        })

        RetrofitClient.instance.fetchRewards().enqueue(object : Callback<List<Reward>> {
            override fun onResponse(call: Call<List<Reward>>, response: Response<List<Reward>>) {
                if (response.isSuccessful) {
                    rewards = response.body()?.toMutableList() ?: mutableListOf()
                    displayRewards()
                }
            }

            override fun onFailure(call: Call<List<Reward>>, t: Throwable) {}
        })
    }

    private fun displayRewards() {
        binding.rewardListLayout.removeAllViews()

        for (reward in rewards) {
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 16, 0, 16)
            }

            val rewardText = EditText(this).apply {
                setText("${reward.name} - ${reward.cost} pts")
                isEnabled = false
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val editButton = Button(this).apply {
                text = getString(R.string.edit)
                setOnClickListener {
                    rewardText.isEnabled = true
                    rewardText.setText(reward.name)
                    text = getString(R.string.save)

                    setOnClickListener {
                        val parts = rewardText.text.toString().split("-")
                        val updatedName = parts.getOrNull(0)?.trim() ?: reward.name
                        val updatedCost = parts.getOrNull(1)?.trim()?.split(" ")?.getOrNull(0)?.toIntOrNull() ?: reward.cost

                        val updatedReward = Reward(reward.rewardId, updatedName, updatedCost)
                        RetrofitClient.instance.updateReward(reward.rewardId, updatedReward).enqueue(object : Callback<Reward> {
                            override fun onResponse(call: Call<Reward>, response: Response<Reward>) {
                                if (response.isSuccessful) {
                                    val index = rewards.indexOfFirst { it.rewardId == reward.rewardId }
                                    rewards[index] = response.body()!!
                                    displayRewards()
                                    showToast("Reward updated")
                                }
                            }

                            override fun onFailure(call: Call<Reward>, t: Throwable) {
                                showToast("Update failed")
                            }
                        })
                    }
                }
            }

            val deleteButton = Button(this).apply {
                text = getString(R.string.delete)
                setOnClickListener {
                    RetrofitClient.instance.deleteReward(reward.rewardId).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            rewards.removeAll { it.rewardId == reward.rewardId }
                            displayRewards()
                            showToast("Reward deleted")
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            showToast("Delete failed")
                        }
                    })
                }
            }

            container.addView(rewardText)
            container.addView(editButton)
            container.addView(deleteButton)
            binding.rewardListLayout.addView(container)
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
