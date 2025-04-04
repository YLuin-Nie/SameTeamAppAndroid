// File Name: ParentDashboard.js

import React, { useState, useEffect } from 'react';
import Calendar from 'react-calendar';
import '../Calendar.css';
import { getCurrentUser } from '../../utils/auth';
import { fetchChores, fetchUsers } from '../../api/api';

function ParentDashboard() {
  const [selectedDate, setSelectedDate] = useState(null);
  const [displayAllTasks, setDisplayAllTasks] = useState(true);
  const [tasksForSelectedDate, setTasksForSelectedDate] = useState([]);
  const [tasksForNext7Days, setTasksForNext7Days] = useState([]);
  const [children, setChildren] = useState([]);
  const currentUser = getCurrentUser();

  // Define levels
  const levels = [
    { min: 0, max: 200, name: "Beginner", color: "#ccc" },
    { min: 200, max: 400, name: "Rising Star", color: "#aaf" },
    { min: 400, max: 600, name: "Helper Pro", color: "#8f8" },
    { min: 600, max: 1000, name: "Superstar", color: "#ffa" },
    { min: 1000, max: 10000, name: "Legend", color: "#fc8" },
  ];

  useEffect(() => {
    const loadChoresAndUsers = async () => {
      try {
        const [users, allChores] = await Promise.all([fetchUsers(), fetchChores()]);
        const childrenOnly = users.filter(u => u.role === 'Child');

        const enhancedChildren = childrenOnly.map(child => {
          const childChores = allChores.filter(c => c.assignedTo === child.userId && c.completed);
          const points = childChores.reduce((sum, c) => sum + c.points, 0);

          const level = levels.find(l => points < l.max) || levels[levels.length - 1];

          return {
            ...child,
            points,
            level
          };
        });

        setChildren(enhancedChildren);

        // Show upcoming chores
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const next7Days = Array.from({ length: 7 }, (_, i) => {
          const d = new Date(today);
          d.setDate(today.getDate() + i);
          return d.toISOString().split('T')[0];
        });

        const upcomingChores = allChores.filter(c => {
          if (!c.dateAssigned || c.completed) return false;
          const date = new Date(c.dateAssigned).toISOString().split('T')[0];
          return next7Days.includes(date);
        }).sort((a, b) => new Date(a.dateAssigned) - new Date(b.dateAssigned));

        setTasksForNext7Days(upcomingChores);
      } catch (err) {
        console.error("Error loading dashboard:", err);
      }
    };

    loadChoresAndUsers();
  }, []);

  const handleDateSelect = async (date) => {
    setSelectedDate(date);
    setDisplayAllTasks(false);

    try {
      const allChores = await fetchChores();
      const selectedDateStr = date.toISOString().split('T')[0];

      const filtered = allChores.filter(c => {
        if (!c.dateAssigned) return false;
        const d = new Date(c.dateAssigned).toISOString().split('T')[0];
        return d === selectedDateStr;
      });

      setTasksForSelectedDate(filtered.sort((a, b) => new Date(a.dateAssigned) - new Date(b.dateAssigned)));
    } catch (err) {
      console.error("Error loading chores:", err);
    }
  };

  return (
    <div className="dashboard parent-dashboard">
      <h1>Parent Dashboard</h1>
      <p>Welcome, {currentUser?.username || "Parent"}! Manage your family's progress here.</p>

      <h3>Children's Levels</h3>
      <ul>
        {children.map(child => (
          <li key={child.userId} className="level-badge-container">
            <span>{child.username} - </span>
            <span className="level-badge" style={{ backgroundColor: child.level.color }}>
              Level {levels.indexOf(child.level) + 1} - {child.level.name}
            </span>
            <span> ({child.points} pts)</span>
          </li>
        ))}
      </ul>

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
                      Assigned to: <span style={{ color: "#FFD700" }}>{task.assignedTo}</span>
                      <br />
                      Due: {new Date(task.dateAssigned).toDateString()}
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
                      {task.choreText} - Assigned to: {task.assignedTo}
                      <br />
                      Due: {new Date(task.dateAssigned).toDateString()}
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

export default ParentDashboard;
