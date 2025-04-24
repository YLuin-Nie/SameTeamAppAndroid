package com.example.sameteamappandroid

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sameteamappandroid.databinding.ActivityChildRewardsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class ChildRewardsActivity : AppCompatActivity() {

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
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
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
                    calculatePoints()
                    displayRewards()
                    displayRedeemed()
                }
            }

            override fun onFailure(call: Call<List<RedeemedReward>>, t: Throwable) {}
        })
    }

    private fun calculatePoints() {
        // Simulate earned points = 100 for demo, subtract redeemed
        val totalEarned = 100
        val spent = redeemedRewards.sumOf { it.pointsSpent }
        points = totalEarned - spent
        binding.pointsTextView.text = getString(R.string.unspent_points) + " $points"
    }

    private fun displayRewards() {
        binding.availableRewardsLayout.removeAllViews()

        if (rewards.isEmpty()) {
            val empty = TextView(this).apply { text = getString(R.string.no_rewards_available) }
            binding.availableRewardsLayout.addView(empty)
        } else {
            for (reward in rewards) {
                val view = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 8, 0, 8)
                }

                val text = TextView(this).apply {
                    text = "${reward.name} - ${reward.cost} Points"
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val button = Button(this).apply {
                    text = getString(R.string.redeem_button)
                    setOnClickListener { redeem(reward) }
                }

                view.addView(text)
                view.addView(button)
                binding.availableRewardsLayout.addView(view)
            }
        }
    }

    private fun displayRedeemed() {
        binding.redeemedHistoryLayout.removeAllViews()

        if (redeemedRewards.isEmpty()) {
            val empty = TextView(this).apply { text = getString(R.string.no_redemptions) }
            binding.redeemedHistoryLayout.addView(empty)
        } else {
            for (reward in redeemedRewards) {
                val text = TextView(this).apply {
                    text = "${reward.rewardName} - ${reward.pointsSpent} Points\n${getString(R.string.redeemed_on)} ${reward.dateRedeemed}"
                }
                binding.redeemedHistoryLayout.addView(text)
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
            rewardId = reward.id,
            rewardName = reward.name,
            pointsSpent = reward.cost,
            dateRedeemed = LocalDate.now().toString()
        )

        RetrofitClient.instance.postRedeemedReward(redemption).enqueue(object : Callback<RedeemedReward> {
            override fun onResponse(call: Call<RedeemedReward>, response: Response<RedeemedReward>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ChildRewardsActivity, "${getString(R.string.redemption_success)} ${reward.name}", Toast.LENGTH_SHORT).show()
                    loadRedeemed()
                } else {
                    Toast.makeText(this@ChildRewardsActivity, getString(R.string.redemption_failed), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RedeemedReward>, t: Throwable) {
                Toast.makeText(this@ChildRewardsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
