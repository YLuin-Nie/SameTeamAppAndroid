// File Name: ChildRewards.js

import React, { useState, useEffect } from "react";
import {
  fetchRewards,
  rewardAsChore,
  fetchRedeemedRewards,
  postRedeemedReward
} from "../../api/api";
import { getCurrentUser } from "../../utils/auth";

function ChildRewards() {
  const [points, setPoints] = useState(0);
  const [rewards, setRewards] = useState([]);
  const [redeemedRewards, setRedeemedRewards] = useState([]);
  const currentUser = getCurrentUser();

  useEffect(() => {
    if (!currentUser) return;

    const loadData = async () => {
      try {
        const rewardList = await fetchRewards();
        setRewards(rewardList);

        const redeemedList = await fetchRedeemedRewards(currentUser.userId);
        setRedeemedRewards(redeemedList);

        // Fetch chores directly using fetchRewards (or create fetchChores API if preferred)
        const choreRes = await fetch("/api/Chores");
        const allChores = await choreRes.json();

        const userChores = allChores.filter(
          c => c.assignedTo === currentUser.userId && c.completed
        );

        const totalEarned = userChores.reduce((sum, c) => sum + c.points, 0);
        const spent = redeemedList.reduce((sum, r) => sum + r.pointsSpent, 0);
        setPoints(totalEarned - spent);
      } catch (err) {
        console.error("Failed to load rewards or history:", err);
      }
    };

    loadData();
  }, [currentUser]);

  const purchaseReward = async (reward) => {
    if (points < reward.cost) {
      alert("Not enough points.");
      return;
    }

    try {
      // Add reward as a negative chore (optional but useful for audit)
      const rewardChore = {
        choreText: `[Reward] ${reward.name}`,
        points: -reward.cost,
        assignedTo: currentUser.userId,
        dateAssigned: new Date().toISOString(),
        completed: true
      };
      await rewardAsChore(rewardChore);

      // Log to redeemed rewards table
      const newRedemption = {
        userId: currentUser.userId,
        rewardId: reward.id,
        rewardName: reward.name,
        pointsSpent: reward.cost,
        dateRedeemed: new Date().toISOString()
      };
      await postRedeemedReward(newRedemption);

      alert(`You redeemed: ${reward.name}`);
      // Reload history
      const updated = await fetchRedeemedRewards(currentUser.userId);
      setRedeemedRewards(updated);
    } catch (err) {
      console.error("Failed to redeem:", err);
      alert("Could not redeem reward.");
    }
  };

  return (
    <div className="rewards-container">
      <h2>Available Rewards</h2>
      <p><strong>Unspent Points:</strong> {points}</p>

      {rewards.length === 0 ? (
        <p>No rewards available.</p>
      ) : (
        <ul>
          {rewards.map((reward) => (
            <li key={reward.id}>
              {reward.name} - {reward.cost} Points
              <button onClick={() => purchaseReward(reward)}>Redeem</button>
            </li>
          ))}
        </ul>
      )}

      <hr />

      <h3>Redeemed Rewards History</h3>
      {redeemedRewards.length === 0 ? (
        <p>You haven't redeemed any rewards yet.</p>
      ) : (
        <ul>
          {redeemedRewards.map((reward) => (
            <li key={reward.redemptionId}>
              {reward.rewardName} - {reward.pointsSpent} Points
              <br />
              <small>Redeemed on: {new Date(reward.dateRedeemed).toLocaleDateString()}</small>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default ChildRewards;
