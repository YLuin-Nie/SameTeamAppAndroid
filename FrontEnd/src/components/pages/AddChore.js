// File Name: AddChore.js

import React, { useState, useEffect } from 'react';
import {
  fetchChores,
  postChore,
  fetchUsers,
  completeChore,
  deleteChore
} from "../../api/api";

const AddChore = () => {
  const [newChore, setNewChore] = useState('');
  const [assignedTo, setAssignedTo] = useState('');
  const [chorePoints, setChorePoints] = useState(10);
  const [choreDate, setChoreDate] = useState('');
  const [chores, setChores] = useState([]);
  const [childUsers, setChildUsers] = useState([]);
  const [editingChoreId, setEditingChoreId] = useState(null);
  const [editedChore, setEditedChore] = useState({});

  useEffect(() => {
    const loadChoresAndUsers = async () => {
      try {
        const choresFromBackend = await fetchChores();
        setChores(choresFromBackend);
        const users = await fetchUsers();
        const children = users.filter(user => user.role === "Child");
        setChildUsers(children);
      } catch (err) {
        console.error("Failed to load chores or users:", err);
      }
    };
    loadChoresAndUsers();
  }, []);

  const addChore = async () => {
    if (newChore.trim() && assignedTo && choreDate) {
      const newChoreObj = {
        choreText: newChore,
        points: chorePoints,
        assignedTo: parseInt(assignedTo),
        dateAssigned: choreDate,
        completed: false,
      };

      try {
        const savedChore = await postChore(newChoreObj);
        setChores([...chores, savedChore]);
        setNewChore('');
        setChorePoints(10);
        setAssignedTo('');
        setChoreDate('');
      } catch (error) {
        console.error("Error adding chore:", error);
        alert("Could not add chore.");
      }
    }
  };

  const toggleCompletion = async (chore) => {
    const updated = { ...chore, completed: !chore.completed };
    try {
      await completeChore(chore.choreId, updated);
      setChores(chores.map(c => (c.choreId === chore.choreId ? updated : c)));
    } catch (err) {
      console.error("Toggle complete error:", err);
    }
  };

  const handleDelete = async (choreId) => {
    try {
      await deleteChore(choreId);
      setChores(chores.filter(c => c.choreId !== choreId));
    } catch (err) {
      console.error("Delete error:", err);
    }
  };

  const startEdit = (chore) => {
    setEditingChoreId(chore.choreId);
    setEditedChore({
      choreText: chore.choreText,
      points: chore.points,
      dateAssigned: chore.dateAssigned.split('T')[0],
      assignedTo: chore.assignedTo
    });
  };

  const saveEdit = async (choreId) => {
    const updated = {
      ...editedChore,
      choreId,
      assignedTo: parseInt(editedChore.assignedTo),
      completed: chores.find(c => c.choreId === choreId).completed
    };
    try {
      await completeChore(choreId, updated);
      setChores(chores.map(c => (c.choreId === choreId ? updated : c)));
      setEditingChoreId(null);
    } catch (err) {
      console.error("Edit save error:", err);
    }
  };

  const today = new Date();
  const sevenDaysAgo = new Date();
  sevenDaysAgo.setDate(today.getDate() - 7);

  const pendingChores = chores.filter(c => !c.completed);
  const completedChores = chores.filter(
    c => c.completed && new Date(c.dateAssigned) >= sevenDaysAgo
  );

  return (
    <div>
      <h2>Chore Management</h2>
      <h3>Add a Chore</h3>

      <input type="text" value={newChore} onChange={e => setNewChore(e.target.value)} placeholder="Chore" />
      <input type="number" value={chorePoints} onChange={e => setChorePoints(Number(e.target.value))} placeholder="Points" />
      <input type="date" value={choreDate} onChange={e => setChoreDate(e.target.value)} />
      <select value={assignedTo} onChange={e => setAssignedTo(e.target.value)}>
        <option value="">Assign to...</option>
        {childUsers.map(child => (
          <option key={child.userId} value={child.userId}>{child.username}</option>
        ))}
      </select>
      <button onClick={addChore}>Add Chore</button>

      <h3>Pending Chores</h3>
      <ul>
        {pendingChores.map(chore => (
          <li key={chore.choreId} className="chore-list-item">
            {editingChoreId === chore.choreId ? (
              <>
                <input type="text" value={editedChore.choreText} onChange={e => setEditedChore({ ...editedChore, choreText: e.target.value })} />
                <input type="number" value={editedChore.points} onChange={e => setEditedChore({ ...editedChore, points: Number(e.target.value) })} />
                <input type="date" value={editedChore.dateAssigned} onChange={e => setEditedChore({ ...editedChore, dateAssigned: e.target.value })} />
                <select value={editedChore.assignedTo} onChange={e => setEditedChore({ ...editedChore, assignedTo: e.target.value })}>
                  {childUsers.map(child => (
                    <option key={child.userId} value={child.userId}>{child.username}</option>
                  ))}
                </select>
                <div className="chore-actions">
                  <button onClick={() => saveEdit(chore.choreId)} title="Save">💾</button>
                  <button onClick={() => setEditingChoreId(null)} title="Cancel">❌</button>
                </div>
              </>
            ) : (
              <>
                {chore.choreText} — {chore.points} pts — Assigned to ID: {chore.assignedTo}
                <div className="chore-actions">
                  <button onClick={() => startEdit(chore)} title="Edit">✏️</button>
                  <button onClick={() => toggleCompletion(chore)} title="Complete">✔️</button>
                  <button onClick={() => handleDelete(chore.choreId)} title="Delete">🗑️</button>
                </div>
              </>
            )}
          </li>
        ))}
      </ul>

      <h3>Completed Chores (Last 7 Days)</h3>
      <ul>
        {completedChores.map(chore => (
          <li key={chore.choreId} className="chore-list-item">
            <span style={{ textDecoration: "line-through" }}>
              {chore.choreText} — {chore.points} pts — Assigned to ID: {chore.assignedTo}
            </span>
            <div className="chore-actions">
              <button onClick={() => toggleCompletion(chore)} title="Undo">🔄</button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default AddChore;
