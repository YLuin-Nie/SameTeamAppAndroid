// File Name: ProfileSetup.js

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { updateUser } from '../../api/api';
import "../styles/signup.css";

function ProfileSetup() {
  const navigate = useNavigate();

  const storedUser = localStorage.getItem("loggedInUser");
  const currentUser = storedUser ? JSON.parse(storedUser) : null;

  const [role, setRole] = useState('');
  const [teamName, setTeamName] = useState('');
  const [profileComplete, setProfileComplete] = useState(false);

  // ✅ Handle redirect inside useEffect
  useEffect(() => {
    if (!currentUser) {
      navigate("/signin");
    }
  }, [currentUser, navigate]);

  const handleRoleSelection = (selectedRole) => {
    setRole(selectedRole);
  };

  const handleTeamNameChange = (e) => {
    setTeamName(e.target.value);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (role === 'parent' && !teamName.trim()) {
      alert("Team name required for parents.");
      return;
    }

    try {
      const updatedUser = {
        ...currentUser,
        role: role === 'parent' ? 'Parent' : 'Child',
        teamId: teamName ? 1 : null, // Replace with dynamic team logic if needed
      };

      await updateUser(currentUser.userId, updatedUser);
      localStorage.setItem('loggedInUser', JSON.stringify(updatedUser));
      setProfileComplete(true);
    } catch (err) {
      console.error("Failed to update user profile:", err);
      alert("Could not save profile info.");
    }
  };

  useEffect(() => {
    if (profileComplete) {
      navigate(role === 'parent' ? '/parent-dashboard' : '/child-dashboard');
    }
  }, [profileComplete, role, navigate]);

  // Don’t render until currentUser is confirmed
  if (!currentUser) return null;

  return (
    <div>
      <div className="header">
        <h2>Profile Setup</h2>
      </div>

      <p>Welcome, {currentUser.username}!</p>

      <form onSubmit={handleSubmit}>
        <div>
          <p>Select Role:</p>
          <button
            type="button"
            className={`role-btn ${role === 'parent' ? 'selected' : ''}`}
            onClick={() => handleRoleSelection('parent')}
          >
            Parent
          </button>
          <button
            type="button"
            className={`role-btn ${role === 'child' ? 'selected' : ''}`}
            onClick={() => handleRoleSelection('child')}
          >
            Child
          </button>
        </div>

        {role === 'parent' && (
          <div>
            <label>
              Team Name:
              <input
                type="text"
                value={teamName}
                onChange={handleTeamNameChange}
                placeholder="Enter your team name"
              />
            </label>
          </div>
        )}

        <button type="submit">Submit</button>
      </form>
    </div>
  );
}

export default ProfileSetup;
