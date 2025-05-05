// File Name: AddChore.js

import React, { useState, useEffect } from 'react';
import { getUsers, addChoreToStorage, getChores, saveChores, getUserPoints, updateUserPoints } from "../utils/localStorageUtils";

const AddChore = () => {
  const [newChore, setNewChore] = useState('');
  const [assignedTo, setAssignedTo] = useState('');
  const [chorePoints, setChorePoints] = useState(10);
  const [choreDate, setChoreDate] = useState('');
  const [chores, setChores] = useState([]);
  const [editingChoreId, setEditingChoreId] = useState(null);
  const [editedChoreText, setEditedChoreText] = useState('');
  const [editedChorePoints, setEditedChorePoints] = useState(10);
  const [editedChoreDate, setEditedChoreDate] = useState('');
  const [editedAssignedTo, setEditedAssignedTo] = useState('');
  const familyMembers = getUsers().filter(user => user.role === "Child");

  useEffect(() => {
    setChores(getChores());
  }, []);

  const today = new Date();
  const sevenDaysAgo = new Date();
  sevenDaysAgo.setDate(today.getDate() - 7);

  const pendingChores = chores.filter(chore => !chore.completed);
  const completedChores = chores.filter(chore => chore.completed && new Date(chore.date) >= sevenDaysAgo);

  const addChore = () => {
    if (newChore.trim() && assignedTo && choreDate) {
      const localDate = new Date(choreDate + 'T00:00:00');
      const newChoreObj = {
        id: Date.now(),
        text: newChore,
        completed: false,
        points: chorePoints,
        assignedTo,
        date: localDate.toISOString(),
      };

      addChoreToStorage(newChoreObj);
      setChores([...chores, newChoreObj]);
      setNewChore('');
      setChorePoints(10);
      setAssignedTo('');
      setChoreDate('');
    }
  };

  const toggleCompletion = (choreId) => {
    const updatedChores = chores.map(chore =>
      chore.id === choreId ? { ...chore, completed: !chore.completed } : chore
    );

    setChores(updatedChores);
    saveChores(updatedChores);

    const chore = chores.find(c => c.id === choreId);
    if (chore) {
      const currentPoints = getUserPoints(chore.assignedTo);
      updateUserPoints(chore.assignedTo, chore.completed ? currentPoints - chore.points : currentPoints + chore.points);
    }
  };

  const deleteChore = (choreId) => {
    setChores(chores.filter(chore => chore.id !== choreId));
    saveChores(chores.filter(chore => chore.id !== choreId));
  };

  const startEdit = (chore) => {
    setEditingChoreId(chore.id);
    setEditedChoreText(chore.text);
    setEditedChorePoints(chore.points);
    setEditedChoreDate(chore.date.split('T')[0]);
    setEditedAssignedTo(chore.assignedTo);
  };

  const saveEdit = (choreId) => {
    const updatedChores = chores.map(chore =>
      chore.id === choreId
        ? {
            ...chore,
            text: editedChoreText,
            points: editedChorePoints,
            date: new Date(editedChoreDate + 'T00:00:00').toISOString(),
            assignedTo: editedAssignedTo,
          }
        : chore
    );

    setChores(updatedChores);
    saveChores(updatedChores);
    setEditingChoreId(null);
  };

  return (
    <div>
      <h2>Chore Management</h2>
      <h3>Add a Chore</h3>
      <input 
        type="text" 
        value={newChore} 
        onChange={(e) => setNewChore(e.target.value)} 
        placeholder="Enter a new chore" 
      />
      <input 
        type="number" 
        value={chorePoints} 
        onChange={(e) => setChorePoints(Number(e.target.value))} 
        placeholder="Points" 
      />
      <input 
        type="date" 
        value={choreDate} 
        onChange={(e) => setChoreDate(e.target.value)} 
        placeholder="Date" 
      />
      <select value={assignedTo} onChange={(e) => setAssignedTo(e.target.value)}>
        <option value="">Assign to...</option>
        {familyMembers.map(member => (
          <option key={member.username} value={member.username}>{member.username}</option>
        ))}
      </select>
      <button onClick={addChore}>Add Chore</button>

      <h3>Pending Chores</h3>
      <ul>
        {pendingChores.map(chore => (
          <li key={chore.id} className="chore-list-item">
            {editingChoreId === chore.id ? (
              <>
                <input type="text" value={editedChoreText} onChange={(e) => setEditedChoreText(e.target.value)} />
                <input type="number" value={editedChorePoints} onChange={(e) => setEditedChorePoints(Number(e.target.value))} />
                <input type="date" value={editedChoreDate} onChange={(e) => setEditedChoreDate(e.target.value)} />
                <select value={editedAssignedTo} onChange={(e) => setEditedAssignedTo(e.target.value)}>
                  {familyMembers.map(member => (
                    <option key={member.username} value={member.username}>{member.username}</option>
                  ))}
                </select>
                <button onClick={() => saveEdit(chore.id)}>💾 Save</button>
                <button onClick={() => setEditingChoreId(null)}>❌ Cancel</button>
              </>
            ) : (
              <>
                <span>{chore.text} (Assigned to: {chore.assignedTo})</span>
                <div className="chore-actions">
                  <button onClick={() => startEdit(chore)}>✏️ Edit</button>
                  <button onClick={() => toggleCompletion(chore.id)}>✔️ Complete</button>
                  <button onClick={() => deleteChore(chore.id)}>🗑️ Delete</button>
                </div>
              </>
            )}
          </li>
        ))}
      </ul>

      <h3>Completed Chores (Last 7 Days)</h3>
      <ul>
        {completedChores.map(chore => (
          <li key={chore.id} className="chore-list-item">
            <span style={{ textDecoration: "line-through" }}>{chore.text} (Assigned to: {chore.assignedTo})</span>
            <div className="chore-actions">
              <button onClick={() => toggleCompletion(chore.id)}>🔄 Undo</button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default AddChore;
