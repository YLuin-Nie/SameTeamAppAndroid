// File Name: ChildDashboard.js

import React, { useState, useEffect } from 'react';
import Calendar from 'react-calendar';
import '../Calendar.css';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser } from '../../utils/auth';
import { fetchChores, completeChore } from '../../api/api';

function ChildDashboard() {
  const [selectedDate, setSelectedDate] = useState(null);
  const [displayAllTasks, setDisplayAllTasks] = useState(true);
  const [tasksForSelectedDate, setTasksForSelectedDate] = useState([]);
  const [tasksForNext7Days, setTasksForNext7Days] = useState([]);
  const [points, setPoints] = useState(0);
  const [totalPoints, setTotalPoints] = useState(0);
  const [level, setLevel] = useState({});
  const [nextLevelThreshold, setNextLevelThreshold] = useState(0);

  const currentUser = getCurrentUser();
  const navigate = useNavigate();

  const loadChores = async () => {
    try {
      const allChores = await fetchChores();
      const userChores = allChores.filter(c => c.assignedTo === currentUser.userId);

      const earnedPoints = userChores.filter(c => c.completed).reduce((sum, c) => sum + c.points, 0);
      setPoints(earnedPoints);
      setTotalPoints(earnedPoints); // Same for now, or split later

      // Levels (you can centralize this later)
      const levels = [
        { min: 0, max: 200 },
        { min: 200, max: 400 },
        { min: 400, max: 600 },
        { min: 600, max: 1000 },
        { min: 1000, max: 10000 },
      ];
      const currentLevel = levels.find(l => earnedPoints < l.max);
      const levelIndex = levels.indexOf(currentLevel);
      setLevel({ level: levelIndex + 1, name: `Level ${levelIndex + 1}`, color: "#4CAF50" });
      setNextLevelThreshold(currentLevel?.max || 1000);

      // Chores for next 7 days
      const today = new Date();
      const next7Days = Array.from({ length: 7 }, (_, i) => {
        const date = new Date(today);
        date.setDate(today.getDate() + i);
        return date.toISOString().split('T')[0];
      });

      const upcoming = userChores.filter(c => {
        if (!c.dateAssigned || c.completed) return false;
        const d = new Date(c.dateAssigned);
        return next7Days.includes(d.toISOString().split('T')[0]);
      }).sort((a, b) => new Date(a.dateAssigned) - new Date(b.dateAssigned));

      setTasksForNext7Days(upcoming);
    } catch (err) {
      console.error("Failed to load chores:", err);
    }
  };

  useEffect(() => {
    if (currentUser) {
      loadChores();
    }
  }, [currentUser]);

  const handleDateSelect = async (date) => {
    setSelectedDate(date);
    setDisplayAllTasks(false);

    try {
      const allChores = await fetchChores();
      const userChores = allChores.filter(c => c.assignedTo === currentUser.userId);

      const selectedDateStr = date.toISOString().split('T')[0];
      const filtered = userChores.filter(c => {
        const choreDate = new Date(c.dateAssigned);
        return choreDate.toISOString().split('T')[0] === selectedDateStr;
      });

      setTasksForSelectedDate(filtered.sort((a, b) => new Date(a.dateAssigned) - new Date(b.dateAssigned)));
    } catch (err) {
      console.error("Failed to filter chores:", err);
    }
  };

  const markChoreComplete = async (chore) => {
    const updated = { ...chore, completed: true };
    try {
      await completeChore(chore.choreId, updated);
      await loadChores();
    } catch (err) {
      console.error("Failed to complete chore:", err);
      alert("Could not mark chore as completed.");
    }
  };

  return (
    <div className="dashboard child-dashboard">
      <h1>Child Dashboard</h1>
      <p className="welcome-message">Welcome, {currentUser ? currentUser.username : "Child"}!</p>
      <p><strong>Total Points Earned:</strong> {totalPoints}</p>
      <p><strong>Unspent Points:</strong> {points}</p>

      <div className="level-badge" style={{ backgroundColor: level.color }}>
        {level.name}
      </div>

      <p>Next Level Progress: {totalPoints - (nextLevelThreshold - 200)} / 200</p>
      <progress value={totalPoints - (nextLevelThreshold - 200)} max="200"></progress>

      <div className="calendar-tasks-container">
        <div className="calendar-section">
          <Calendar onChange={handleDateSelect} value={selectedDate} />
        </div>

        <div className="tasks-section">
          {displayAllTasks ? (
            <>
              <h3>Upcoming Chores (Next 7 Days)</h3>
              {tasksForNext7Days.length === 0 ? (
                <p>No upcoming chores.</p>
              ) : (
                <ul>
                  {tasksForNext7Days.map(task => (
                    <li key={task.choreId}>
                      <strong>{task.choreText}</strong>
                      <br />
                      Due: {new Date(task.dateAssigned).toDateString()}
                      <br />
                      Points: {task.points} pts
                      <br />
                      <button onClick={() => markChoreComplete(task)}>✔ Complete</button>
                    </li>
                  ))}
                </ul>
              )}
            </>
          ) : (
            <>
              <h3>Chores for {selectedDate ? selectedDate.toDateString() : ''}</h3>
              {tasksForSelectedDate.length === 0 ? (
                <p>No chores for this date.</p>
              ) : (
                <ul>
                  {tasksForSelectedDate.map(task => (
                    <li key={task.choreId}>
                      {task.choreText}
                      <br />
                      Due: {new Date(task.dateAssigned).toDateString()}
                      <br />
                      Points: {task.points} pts
                      <br />
                      <button onClick={() => markChoreComplete(task)}>✔ Complete</button>
                    </li>
                  ))}
                </ul>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default ChildDashboard;
