// File: NavBar.js

import React from 'react';
import { Link } from 'react-router-dom';
import '../styles/dashboard.css';

const NavBar = () => {
    return (
        <nav className="navbar">
            <h2>Same Team</h2>
            <ul>
                <li><Link to="/">Home</Link></li>
                <li><Link to="/login">Login</Link></li>
                <li><Link to="/signup">Sign Up</Link></li>
                <li><Link to="/profile-setup">Profile Setup</Link></li>
                <li><Link to="/parent-dashboard">Parent Dashboard</Link></li>
                <li><Link to="/child-dashboard">Child Dashboard</Link></li>
            </ul>
        </nav>
    );
};

export default NavBar;

