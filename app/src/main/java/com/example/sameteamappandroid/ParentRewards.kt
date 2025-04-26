package com.example.sameteamappandroid

import android.os.Bundle
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

        binding.rewardButton.setOnClickListener { rewardChild() }
        binding.addRewardButton.setOnClickListener { addNewReward() }
    }

    private fun fetchUsersAndRewards() {
        RetrofitClient.instance.fetchUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    children = response.body()?.filter { it.role == "Child" } ?: emptyList()
                    val adapter = ArrayAdapter(this@ParentRewards, android.R.layout.simple_spinner_item, children.map { it.username })
                    binding.childSpinner.adapter = adapter
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

    private fun rewardChild() {
        val selectedIndex = binding.childSpinner.selectedItemPosition
        val name = binding.rewardNameEditText.text.toString().trim()
        val points = binding.rewardPointsEditText.text.toString().toIntOrNull() ?: return

        if (selectedIndex < 0 || name.isEmpty() || points <= 0) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val chore = Chore(
            choreText = name,
            points = points,
            assignedTo = children[selectedIndex].userId,
            dateAssigned = LocalDate.now().toString(),
            completed = true
        )

        RetrofitClient.instance.rewardAsChore(chore).enqueue(object : Callback<Chore> {
            override fun onResponse(call: Call<Chore>, response: Response<Chore>) {
                Toast.makeText(this@ParentRewards, "Rewarded!", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<Chore>, t: Throwable) {
                Toast.makeText(this@ParentRewards, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addNewReward() {
        val name = binding.newRewardNameEditText.text.toString().trim()
        val cost = binding.newRewardPointsEditText.text.toString().toIntOrNull() ?: return

        if (name.isEmpty() || cost <= 0) {
            Toast.makeText(this, "Please enter valid name and cost", Toast.LENGTH_SHORT).show()
            return
        }

        val reward = Reward(0, name, cost)
        RetrofitClient.instance.postReward(reward).enqueue(object : Callback<Reward> {
            override fun onResponse(call: Call<Reward>, response: Response<Reward>) {
                response.body()?.let {
                    rewards.add(it)
                    displayRewards()
                }
            }
            override fun onFailure(call: Call<Reward>, t: Throwable) {}
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

                        val updatedReward = Reward(reward.id, updatedName, updatedCost)
                        RetrofitClient.instance.updateReward(reward.id, updatedReward).enqueue(object : Callback<Reward> {
                            override fun onResponse(call: Call<Reward>, response: Response<Reward>) {
                                if (response.isSuccessful) {
                                    val index = rewards.indexOfFirst { it.id == reward.id }
                                    rewards[index] = response.body()!!
                                    displayRewards()
                                    Toast.makeText(this@ParentRewards, "Reward updated", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: Call<Reward>, t: Throwable) {
                                Toast.makeText(this@ParentRewards, "Update failed", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }

            val deleteButton = Button(this).apply {
                text = getString(R.string.delete)
                setOnClickListener {
                    RetrofitClient.instance.deleteReward(reward.id).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            rewards.removeAll { it.id == reward.id }
                            displayRewards()
                            Toast.makeText(this@ParentRewards, "Reward deleted", Toast.LENGTH_SHORT).show()
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@ParentRewards, "Delete failed", Toast.LENGTH_SHORT).show()
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
}
