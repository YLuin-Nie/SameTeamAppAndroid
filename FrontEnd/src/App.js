// File Name: App.js

// CIS435 Class Project - SameTeamAppFull
// Team:  Bob Nie, Bansari Patel
// Date: 2025-04-03

import './App.css';
import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, useNavigate, useLocation } from 'react-router-dom';
import SignUp from './components/pages/SignUp';
import SignIn from './components/pages/SignIn';
import ProfileSetup from './components/pages/ProfileSetup';
import ParentDashboard from './components/pages/ParentDashboard';
import ChildDashboard from './components/pages/ChildDashboard';
import ChildRewards from './components/pages/ChildRewards';
import ParentRewards from './components/pages/ParentRewards';
import HomePage from './components/pages/HomePage';
import AddChore from './components/pages/AddChore';
import NavBar from './components/pages/NavBar';
import ChoresList from './components/pages/ChoresList';
import { loginUser } from './api/api';

function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [darkMode, setDarkMode] = useState(false);

  useEffect(() => {
    const storedUser = localStorage.getItem("loggedInUser");
    if (storedUser) {
      try {
        setCurrentUser(JSON.parse(storedUser));
      } catch (err) {
        console.error("Invalid JSON in loggedInUser:", err);
        localStorage.removeItem("loggedInUser");
      }
    }

    const storedDarkMode = localStorage.getItem("darkMode") === "true";
    setDarkMode(storedDarkMode);
    document.body.classList.toggle("dark-mode", storedDarkMode);
  }, []);

  const toggleDarkMode = () => {
    setDarkMode(prevMode => {
      const newMode = !prevMode;
      localStorage.setItem("darkMode", newMode);
      document.body.classList.toggle("dark-mode", newMode);
      return newMode;
    });
  };

  function AppRoutes() {
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
      const storedUser = localStorage.getItem("loggedInUser");
      if (storedUser && location.pathname === "/") {
        try {
          const user = JSON.parse(storedUser);
          if (user.role === "Parent") {
            navigate("/parent-dashboard");
          } else {
            navigate("/child-dashboard");
          }
        } catch (err) {
          console.error("Failed to parse stored user:", err);
          localStorage.removeItem("loggedInUser");
          navigate("/signin");
        }
      }
    }, [navigate, location.pathname]);

    const handleSignInSuccess = async (email, password) => {
      if (!email || !password) {
        alert("Please enter both email and password.");
        return;
      }

      try {
        const response = await loginUser(email, password);
        const { token, user } = response;

        if (token) localStorage.setItem("token", token);
        if (user) {
          localStorage.setItem("loggedInUser", JSON.stringify(user));
          setCurrentUser(user);
          navigate(user.role === "Parent" ? "/parent-dashboard" : "/child-dashboard");
        }
      } catch (err) {
        console.error("Login failed:", err);
        alert("Invalid email or password. Please try again.");
      }
    };

    const hideNavBarRoutes = ["/", "/signin", "/signup", "/profile-setup"];

    return (
      <div className="flex">
        {!hideNavBarRoutes.includes(location.pathname) && (
          <NavBar currentUser={currentUser} setCurrentUser={setCurrentUser} />
        )}

        <div className={`flex-1 ${hideNavBarRoutes.includes(location.pathname) ? "w-full" : "ml-64"}`}>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/signin" element={<SignIn onSignInSuccess={handleSignInSuccess} />} />
            <Route path="/signup" element={<SignUp />} />
            <Route path="/profile-setup" element={<ProfileSetup />} />
            <Route path="/parent-dashboard" element={<ParentDashboard />} />
            <Route path="/child-dashboard" element={<ChildDashboard />} />
            <Route path="/child-rewards" element={<ChildRewards />} />
            <Route path="/parent-rewards" element={<ParentRewards />} />
            <Route path="/add-chores" element={<AddChore />} />
            <Route path="/chores-list" element={<ChoresList />} />
          </Routes>
        </div>
      </div>
    );
  }

  return (
    <Router>
      <div className={`App ${darkMode ? "dark" : "light"}`}>
        <div className="toggle-container">
          <div className={`toggle ${darkMode ? 'night' : ''}`} onClick={toggleDarkMode}>
            <div className="notch">
              <div className="crater"></div>
              <div className="crater"></div>
            </div>
            <div className="shape sm"></div>
            <div className="shape md"></div>
            <div className="shape lg"></div>
          </div>
        </div>

        <AppRoutes />
      </div>
    </Router>
  );
}

export default App;
