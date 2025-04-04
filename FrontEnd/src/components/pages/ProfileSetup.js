// File Name: ProfileSetup.js

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser } from '../../utils/auth';
import { fetchUsers, updateUser } from '../../api/api';
import "../styles/signup.css";

function ProfileSetup() {
  const navigate = useNavigate();
  const currentUser = getCurrentUser();

  const [role, setRole] = useState('');
  const [teamName, setTeamName] = useState('');
  const [profileComplete, setProfileComplete] = useState(false);

  const handleRoleSelection = (selectedRole) => {
    setRole(selectedRole);
  };

  const handleTeamNameChange = (e) => {
    setTeamName(e.target.value);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!currentUser) {
      alert("No user logged in.");
      return;
    }

    if (role === 'parent' && !teamName.trim()) {
      alert("Team name required for parents.");
      return;
    }

    try {
      const updatedUser = {
        ...currentUser,
        role: role === 'parent' ? 'Parent' : 'Child',
        teamId: teamName ? 1 : null // hardcoded team ID unless you're handling dynamic creation
      };

      await updateUser(currentUser.userId, updatedUser);
      localStorage.setItem('loggedInUser', JSON.stringify(updatedUser));
      setProfileComplete(true);
    } catch (err) {
      console.error("Failed to update user profile:", err);
      alert("Could not save profile info.");
    }
  };

  if (profileComplete) {
    return role === 'parent'
      ? navigate('/parent-dashboard')
      : navigate('/child-dashboard');
  }

  return (
    <div>
      <div className="header">
        <h2>Profile Setup</h2>
      </div>

      <p>Welcome, {currentUser ? currentUser.username : 'User'}!</p>

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
