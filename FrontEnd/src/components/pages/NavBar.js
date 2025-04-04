// File Name: NavBar.js

import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

const NavBar = ({ currentUser, setCurrentUser }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('loggedInUser');
    setCurrentUser(null);
    navigate('/'); // or navigate('/signin') if that's your login page
  };

  return (
    <nav className="navbar">
      <ul className="navbar-list">
        {currentUser && currentUser.role === 'Parent' && (
          <>
            <li className="navbar-item">
              <Link to="/parent-dashboard" className="navbar-link">
                <i className="fas fa-tachometer-alt"></i>
              </Link>
            </li>
            <li className="navbar-item">
              <Link to="/add-chores" className="navbar-link">
                <i className="fas fa-tasks"></i>
              </Link>
            </li>
            <li className="navbar-item">
              <Link to="/parent-rewards" className="navbar-link">
                <i className="fas fa-gift"></i>
              </Link>
            </li>
          </>
        )}
        {currentUser && currentUser.role === 'Child' && (
          <>
            <li className="navbar-item">
              <Link to="/child-dashboard" className="navbar-link">
                <i className="fas fa-tachometer-alt"></i>
              </Link>
            </li>
            <li className="navbar-item">
              <Link to="/chores-list" className="navbar-link">
                <i className="fas fa-list"></i>
              </Link>
            </li>
            <li className="navbar-item">
              <Link to="/child-rewards" className="navbar-link">
                <i className="fas fa-gift"></i>
              </Link>
            </li>
          </>
        )}
        {currentUser && (
          <li className="navbar-item">
            <Link to="/" onClick={handleLogout} className="navbar-link">
              <i className="fas fa-sign-out-alt"></i>
            </Link>
          </li>
        )}
      </ul>
    </nav>
  );
};

export default NavBar;
