// File: src/components/pages/HomePage.js

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';


const HomePage = () => {
    // Initialize darkMode state based on local storage
    const [darkMode, setDarkMode] = useState(() => {
        return localStorage.getItem('darkMode') === 'true';
    });
    const navigate = useNavigate();

    // Toggle dark mode on button click
    const handleToggleDarkMode = () => {
        setDarkMode(!darkMode);
        if (!darkMode) {
            document.body.classList.add('dark-mode');
        } else {
            document.body.classList.remove('dark-mode');
        }
    };

    // Handling Sign In and Sign Up clicks
    const handleSignInClick = () => {
        navigate("/signin");
    };

    const handleSignUpClick = () => {
        navigate("/signup");
    };

    // Apply dark mode from local storage on page load (if previously set)
    useEffect(() => {
        if (localStorage.getItem('darkMode') === 'true') {
            setDarkMode(true);
            document.body.classList.add('dark-mode');
        }
    }, []);

    // Store dark mode state to local storage
    useEffect(() => {
        localStorage.setItem('darkMode', darkMode);
    }, [darkMode]);

    return (
        <div className="home-container">
            <div className="logo">
                <img 
                    src={darkMode ? 'logodark.png' : 'logo.png'} // Change logo based on dark mode
                    alt="Logo"
                    style={{ width: '250px', height: 'auto' }}
                />
            </div>
            <div className="buttons">
                <button className="button" onClick={handleSignInClick}>Sign In</button>
                <button className="button" onClick={handleSignUpClick}>Sign Up</button>
            </div>
        </div>
    );
};

export default HomePage;
