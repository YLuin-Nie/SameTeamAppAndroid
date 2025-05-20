package com.example.sameteamappandroid

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sameteamappandroid.databinding.ActivityChildRewardsBinding
import org.threeten.bp.LocalDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChildRewards : AppCompatActivity() {

    private lateinit var binding: ActivityChildRewardsBinding
    private var rewards: List<Reward> = listOf()
    private var redeemedRewards: List<RedeemedReward> = listOf()
    private var userId: Int = -1
    private var points: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        if (userId != -1) {
            loadRewards()
        } else {
            Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
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

    private fun loadRewards() {
        RetrofitClient.instance.fetchRewards().enqueue(object : Callback<List<Reward>> {
            override fun onResponse(call: Call<List<Reward>>, response: Response<List<Reward>>) {
                if (response.isSuccessful) {
                    rewards = response.body() ?: listOf()
                    loadRedeemed()
                }
            }

            override fun onFailure(call: Call<List<Reward>>, t: Throwable) {}
        })
    }

    private fun loadRedeemed() {
        RetrofitClient.instance.fetchRedeemedRewards(userId).enqueue(object : Callback<List<RedeemedReward>> {
            override fun onResponse(call: Call<List<RedeemedReward>>, response: Response<List<RedeemedReward>>) {
                if (response.isSuccessful) {
                    redeemedRewards = response.body() ?: listOf()
                    loadCompletedChores()
                }
            }

            override fun onFailure(call: Call<List<RedeemedReward>>, t: Throwable) {}
        })
    }

    private fun loadCompletedChores() {
        RetrofitClient.instance.fetchCompletedChores().enqueue(object : Callback<List<Chore>> {
            override fun onResponse(call: Call<List<Chore>>, response: Response<List<Chore>>) {
                if (response.isSuccessful) {
                    val userCompleted = response.body()?.filter { it.assignedTo == userId } ?: listOf()
                    val earned = userCompleted.sumOf { it.points }
                    val spent = redeemedRewards.sumOf { it.pointsSpent }
                    points = earned - spent
                    binding.pointsTextView.text = getString(R.string.unspent_points_format, points)
                    displayRewards()
                    displayRedeemed()
                }
            }

            override fun onFailure(call: Call<List<Chore>>, t: Throwable) {}
        })
    }

    private fun displayRewards() {
        binding.availableRewardsLayout.removeAllViews()

        if (rewards.isEmpty()) {
            val empty = TextView(this).apply {
                text = getString(R.string.no_rewards_available)
            }
            binding.availableRewardsLayout.addView(empty)
        } else {
            for (reward in rewards) {
                val layout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 8, 0, 8)
                }

                val rewardTextView = TextView(this).apply {
                    text = getString(R.string.reward_item_format, reward.name, reward.cost)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val button = Button(this).apply {
                    text = getString(R.string.redeem_button)
                    setOnClickListener { redeem(reward) }
                }

                layout.addView(rewardTextView)
                layout.addView(button)
                binding.availableRewardsLayout.addView(layout)
            }
        }
    }

    private fun displayRedeemed() {
        binding.redeemedHistoryLayout.removeAllViews()

        if (redeemedRewards.isEmpty()) {
            val empty = TextView(this).apply {
                text = getString(R.string.no_redemptions)
            }
            binding.redeemedHistoryLayout.addView(empty)
        } else {
            for (reward in redeemedRewards) {
                val name = if (!reward.rewardName.isNullOrBlank()) {
                    reward.rewardName
                } else {
                    rewards.find { it.rewardId == reward.rewardId }?.name ?: "Unnamed Reward"
                }

                val rewardView = TextView(this).apply {
                    text = "$name - ${reward.pointsSpent} Points\nRedeemed on: ${reward.dateRedeemed}"
                    setPadding(0, 8, 0, 8)
                }
                binding.redeemedHistoryLayout.addView(rewardView)
            }
        }
    }


    private fun redeem(reward: Reward) {
        if (points < reward.cost) {
            Toast.makeText(this, getString(R.string.not_enough_points), Toast.LENGTH_SHORT).show()
            return
        }

        val redemption = RedeemedReward(
            redemptionId = 0,
            userId = userId,
            rewardId = reward.rewardId,
            rewardName = reward.name,
            pointsSpent = reward.cost,
            dateRedeemed = LocalDate.now().toString()
        )

        // First: post redemption
        RetrofitClient.instance.postRedeemedReward(redemption).enqueue(object : Callback<RedeemedReward> {
            override fun onResponse(call: Call<RedeemedReward>, response: Response<RedeemedReward>) {
                if (response.isSuccessful) {
                    // Then: deduct points
                    val newPoints = points - reward.cost
                    val updateRequest = mapOf("points" to newPoints)

                    RetrofitClient.instance.updateUserPoints(userId, updateRequest).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ChildRewards,
                                    getString(R.string.redemption_success_format, reward.name),
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadRedeemed()
                            } else {
                                Toast.makeText(this@ChildRewards, "Reward saved, but failed to update points", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(this@ChildRewards, "Reward saved, but failed to update points", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this@ChildRewards, getString(R.string.redemption_failed), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RedeemedReward>, t: Throwable) {
                Toast.makeText(this@ChildRewards, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
