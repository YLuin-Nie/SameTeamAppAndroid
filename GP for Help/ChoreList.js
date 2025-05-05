/// File Name: ChoreList.js

import React, { useState, useEffect } from 'react';
import { getCurrentUser, getUsers } from "../utils/localStorageUtils"; //  Import functions from localStorageUtils

//  Function to retrieve chores from localStorage
const getChores = () => JSON.parse(localStorage.getItem("chores")) || [];

//  Function to update chores in localStorage
const updateChoresInStorage = (chores) => {
    localStorage.setItem("chores", JSON.stringify(chores));
};

function ChoreList() {
    const [chores, setChores] = useState([]);
    const currentUser = getCurrentUser(); //  Get the logged-in user

    //  Load chores from localStorage when component mounts
    useEffect(() => {
        setChores(getChores());
    }, []);

    //  Function to toggle chore completion status
    const toggleComplete = (id) => {
        const updatedChores = chores.map(chore =>
            chore.id === id ? { ...chore, completed: !chore.completed } : chore
        );
        setChores(updatedChores);
        updateChoresInStorage(updatedChores); //  Persist to localStorage
    };

    //  Function to delete a chore
    const deleteChore = (id) => {
        const updatedChores = chores.filter(chore => chore.id !== id);
        setChores(updatedChores);
        updateChoresInStorage(updatedChores); //  Remove from localStorage
    };

    return (
        <div>
            <h2>Chore List</h2>
            <p>Welcome, {currentUser ? currentUser.username : "User"}! Here are your assigned chores.</p>
            <ul>
                {chores.map(chore => (
                    <li key={chore.id} style={{ textDecoration: chore.completed ? "line-through" : "none" }}>
                        {chore.text} (Assigned to: {chore.assignedTo})
                        <button onClick={() => toggleComplete(chore.id)}>
                            {chore.completed ? "Undo" : "Complete"}
                        </button>
                        <button onClick={() => deleteChore(chore.id)}>Delete</button>
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default ChoreList;
