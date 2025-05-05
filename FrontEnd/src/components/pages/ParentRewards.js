// File Name: ParentRewards.js

import React, { useState, useEffect } from "react";
import {
  fetchUsers,
  fetchRewards,
  postReward,
  updateReward,
  deleteReward as deleteRewardAPI,
  rewardAsChore,
} from "../../api/api";

function ParentRewards() {
  const [children, setChildren] = useState([]);
  const [selectedChild, setSelectedChild] = useState("");
  const [rewardChildName, setRewardChildName] = useState("");
  const [rewardChildPoints, setRewardChildPoints] = useState(10);

  const [newRewardName, setNewRewardName] = useState("");
  const [newRewardPoints, setNewRewardPoints] = useState(10);

  const [rewards, setRewards] = useState([]);
  const [editingReward, setEditingReward] = useState(null);
  const [editedRewardName, setEditedRewardName] = useState("");
  const [editedRewardCost, setEditedRewardCost] = useState(0);

  useEffect(() => {
    const loadData = async () => {
      try {
        const users = await fetchUsers();
        setChildren(users.filter((u) => u.role === "Child"));

        const rewardsData = await fetchRewards();
        setRewards(rewardsData);
      } catch (err) {
        console.error("Error loading users or rewards:", err);
      }
    };
    loadData();
  }, []);

  const rewardChild = async () => {
    if (!selectedChild || !rewardChildName || rewardChildPoints <= 0) {
      alert("Please select a child, enter a reward, and set a valid point amount.");
      return;
    }

    try {
      const rewardChore = {
        choreText: rewardChildName,
        points: rewardChildPoints,
        assignedTo: parseInt(selectedChild),
        dateAssigned: new Date().toISOString(),
        completed: true,
      };

      await rewardAsChore(rewardChore);
      alert(`Rewarded ${rewardChildPoints} points to ${selectedChild} for ${rewardChildName}!`);

      setSelectedChild("");
      setRewardChildName("");
      setRewardChildPoints(10);
    } catch (err) {
      console.error("Error rewarding child:", err);
      alert("Failed to reward. Check backend.");
    }
  };

  const addReward = async () => {
    if (!newRewardName.trim() || newRewardPoints <= 0) {
      alert("Please enter a valid reward name and cost.");
      return;
    }

    try {
      const newReward = {
        name: newRewardName,
        cost: newRewardPoints,
      };
      const saved = await postReward(newReward);
      setRewards([...rewards, saved]);

      setNewRewardName("");
      setNewRewardPoints(10);
    } catch (err) {
      console.error("Error adding reward:", err);
    }
  };

  const saveEdit = async () => {
    try {
      const updated = await updateReward(editingReward, {
        id: editingReward,
        name: editedRewardName,
        cost: editedRewardCost,
      });

      setRewards(rewards.map(r => (r.id === editingReward ? updated : r)));
      setEditingReward(null);
      setEditedRewardName("");
      setEditedRewardCost(0);
    } catch (err) {
      console.error("Failed to save reward:", err);
    }
  };

  const deleteReward = async (id) => {
    try {
      await deleteRewardAPI(id);
      setRewards(rewards.filter(r => r.id !== id));
    } catch (err) {
      console.error("Failed to delete reward:", err);
    }
  };

  return (
    <div className="rewards-container">
      <h2>Parent Rewards Management</h2>

      <h3>Reward A Child (Extra Points)</h3>
      <select value={selectedChild} onChange={(e) => setSelectedChild(e.target.value)}>
        <option value="">Select a child...</option>
        {children.map(child => (
          <option key={child.userId} value={child.userId}>
            {child.username}
          </option>
        ))}
      </select>

      <label>Reward Name:</label>
      <input type="text" value={rewardChildName} onChange={(e) => setRewardChildName(e.target.value)} placeholder="e.g., Extra Playtime" />

      <label>Points:</label>
      <input type="number" value={rewardChildPoints} onChange={(e) => setRewardChildPoints(Number(e.target.value))} min="1" />

      <button onClick={rewardChild}>Reward Points</button>

      <hr />

      <h3>Define New Rewards</h3>
      <input type="text" placeholder="Reward Name" value={newRewardName} onChange={(e) => setNewRewardName(e.target.value)} />
      <input type="number" placeholder="Cost" value={newRewardPoints} onChange={(e) => setNewRewardPoints(Number(e.target.value))} />
      <button onClick={addReward}>Add Reward</button>

      <hr />

      <h3>Manage Rewards</h3>
      <ul className="reward-list">
        {rewards.length === 0 ? <p>No rewards defined yet.</p> :
          rewards.map((reward) => (
            <li key={reward.id} className="reward-item">
              {editingReward === reward.id ? (
                <>
                  <input type="text" value={editedRewardName} onChange={(e) => setEditedRewardName(e.target.value)} />
                  <input type="number" value={editedRewardCost} onChange={(e) => setEditedRewardCost(Number(e.target.value))} />
                  <button onClick={saveEdit}>💾</button>
                  <button onClick={() => setEditingReward(null)}>❌</button>
                </>
              ) : (
                <>
                  <span>{reward.name} - {reward.cost} Points</span>
                  <div className="reward-actions">
                    <button onClick={() => {
                      setEditingReward(reward.id);
                      setEditedRewardName(reward.name);
                      setEditedRewardCost(reward.cost);
                    }}>✏️</button>
                    <button onClick={() => deleteReward(reward.id)}>🗑️</button>
                  </div>
                </>
              )}
            </li>
          ))}
      </ul>
    </div>
  );
}

export default ParentRewards;
