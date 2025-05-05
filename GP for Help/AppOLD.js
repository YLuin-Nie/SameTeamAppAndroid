// File Name: App.js

// SDC445 Class Project - SameTeamApp
// Team 6:  Andrew Fisher, Bob Nie, Bansari Patel
// Date: 2025-02-27

import './App.css';
import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, useNavigate, useLocation } from 'react-router-dom';
import { initializeLocalStorage, getCurrentUser, authenticateUser } from "./components/utils/localStorageUtils";
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

function App() {
    const [currentUser, setCurrentUser] = useState(null);
    const [darkMode, setDarkMode] = useState(false);
    const [chores, setChores] = useState([]);

    useEffect(() => {
        initializeLocalStorage();
        const user = getCurrentUser();
        if (user) {
            console.log("Auto-login detected for:", user);
            setCurrentUser(user);
        } else {
            console.log("No user is logged in.");
        }

        const storedDarkMode = localStorage.getItem("darkMode") === "true";
        setDarkMode(storedDarkMode);
        document.body.classList.toggle("dark-mode", storedDarkMode);

        const storedChores = JSON.parse(localStorage.getItem("chores")) || [];
        setChores(storedChores);
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
            const user = getCurrentUser();
            if (user && location.pathname === "/") {
                if (user.role === "Parent") {
                    navigate("/parent-dashboard");
                } else {
                    navigate("/child-dashboard");
                }
            }
        }, [navigate]);

        const handleSignInSuccess = (email, password) => {
            if (!email || !password) {
                alert("Please enter both email and password.");
                return;
            }

            const user = authenticateUser(email, password);
            if (user) {
                console.log("User signed in successfully:", user);
                setCurrentUser(user);

                if (user.role === "Parent") {
                    navigate("/parent-dashboard");
                } else {
                    navigate("/child-dashboard");
                }
            } else {
                alert("Invalid email or password. Please try again.");
            }
        };

        // Routes where NavBar should be hidden
        const hideNavBarRoutes = ["/", "/signin", "/signup", "/profile-setup"];

        return (
            <div className="flex">
                {/* Show NavBar on all routes EXCEPT those in `hideNavBarRoutes` */}
                {!hideNavBarRoutes.includes(location.pathname) && <NavBar currentUser={currentUser} setCurrentUser={setCurrentUser} />}
                
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
                        <Route 
                            path="/chores-list" 
                            element={<ChoresList chores={chores} toggleChoreCompletion={toggleChoreCompletion} />} 
                        />
                    </Routes>
                </div>
            </div>
        );
    }

    const addNewChore = (chore) => {
        const updatedChores = [...chores, { ...chore, id: Date.now(), completed: false }];
        setChores(updatedChores);
        localStorage.setItem("chores", JSON.stringify(updatedChores));
    };

    const toggleChoreCompletion = (id) => {
        const updatedChores = chores.map(chore =>
            chore.id === id ? { ...chore, completed: !chore.completed } : chore
        );
        setChores(updatedChores);
        localStorage.setItem("chores", JSON.stringify(updatedChores));
    };

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
