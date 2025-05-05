// File Name: ChoresList.js

import React, { useState, useEffect } from 'react';
import {
  fetchChores,
  completeChore as completeChoreAPI,
} from '../../api/api';

const ChoresList = () => {
  const [chores, setChores] = useState({ pendingChores: [], completedChores: [] });
  const [points, setPoints] = useState(0);
  const [completionPercentage, setCompletionPercentage] = useState(0);

  const currentUser = JSON.parse(localStorage.getItem('loggedInUser'));

  const today = new Date();
  const sevenDaysAgo = new Date();
  sevenDaysAgo.setDate(today.getDate() - 7);

  const loadChores = async () => {
    try {
      const allChores = await fetchChores();
      const userChores = allChores.filter(
        (chore) => chore.assignedTo === currentUser.userId
      );

      const pending = userChores.filter((c) => !c.completed);
      const completed = userChores.filter(
        (c) => c.completed && new Date(c.dateAssigned) >= sevenDaysAgo
      );

      setChores({ pendingChores: pending, completedChores: completed });

      const totalPoints = completed.reduce((sum, c) => sum + c.points, 0);
      setPoints(totalPoints);
      setCompletionPercentage(
        userChores.length > 0
          ? Math.round((completed.length / userChores.length) * 100)
          : 0
      );
    } catch (err) {
      console.error('Failed to load chores:', err);
    }
  };

  useEffect(() => {
    if (currentUser) loadChores();
  }, [currentUser]);

  const toggleChoreCompletion = async (chore) => {
    const updated = { ...chore, completed: !chore.completed };
    try {
      await completeChoreAPI(chore.choreId, updated);
      await loadChores();
    } catch (err) {
      console.error("Failed to update chore status:", err);
      alert("Unable to update chore. Try again.");
    }
  };

  return (
    <div className="chores-list-container">
      <h2>Your Chores</h2>
      <p><strong>Your Points:</strong> {points}</p>

      <p><strong>Task Completion Progress:</strong> {completionPercentage}%</p>
      <progress value={completionPercentage} max="100"></progress>

      {/* Pending */}
      <h3>Pending Chores</h3>
      {chores.pendingChores.length === 0 ? (
        <p>No pending chores assigned.</p>
      ) : (
        <ul>
          {chores.pendingChores.map((chore) => (
            <li key={chore.choreId}>
              {chore.choreText} ({chore.points} pts)
              <br />
              <small>Due: {new Date(chore.dateAssigned).toDateString()}</small>
              <br />
              <button onClick={() => toggleChoreCompletion(chore)}>✔️ Complete</button>
            </li>
          ))}
        </ul>
      )}

      {/* Completed */}
      <h3>Completed Chores (Last 7 Days)</h3>
      {chores.completedChores.length === 0 ? (
        <p>No completed chores in the last 7 days.</p>
      ) : (
        <ul>
          {chores.completedChores.map((chore) => (
            <li key={chore.choreId} style={{ textDecoration: 'line-through' }}>
              {chore.choreText} ({chore.points} pts)
              <br />
              <small>Completed on: {new Date(chore.dateAssigned).toDateString()}</small>
              <br />
              <button onClick={() => toggleChoreCompletion(chore)}>🔄 Undo</button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default ChoresList;
