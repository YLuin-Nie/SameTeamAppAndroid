// File Name: SignIn.js

import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/signup.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faEye, faEyeSlash, faArrowLeft } from "@fortawesome/free-solid-svg-icons";
import { loginUser, fetchUsers } from "../../api/api";

const SignIn = ({ onSignInSuccess }) => {
  const [formData, setFormData] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [allUsers, setAllUsers] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const loadUsers = async () => {
      try {
        const users = await fetchUsers();
        setAllUsers(users);
      } catch (err) {
        console.error("Failed to fetch users:", err);
        setError("Unable to connect to server.");
      }
    };
    loadUsers();
  }, []);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");
  
    const trimmedEmail = formData.email.trim();
    if (!trimmedEmail || !formData.password) {
      setError("Email and password are required.");
      return;
    }
  
    try {
      const result = await loginUser(trimmedEmail, formData.password);
      const { token, user } = result;
  
      localStorage.setItem("token", token);
      localStorage.setItem("loggedInUser", JSON.stringify(user));
  
      if (onSignInSuccess) {
        onSignInSuccess(trimmedEmail, formData.password);
      } else {
        const destination = user.role === "Child" ? "/child-dashboard" : "/parent-dashboard";
        navigate(destination);
      }
    } catch (err) {
      console.error("Login failed:", err);
      setError("Invalid email or password.");
    }
  };
  

  return (
    <div className="signup-container">
      <div className="header">
        <FontAwesomeIcon icon={faArrowLeft} onClick={() => navigate(-1)} className="back-arrow" />
        <h2>Sign In</h2>
      </div>
      {error && <p style={{ color: "red" }}>{error}</p>}

      <form onSubmit={handleLogin}>
        <input
          type="email"
          placeholder="Email"
          value={formData.email}
          onChange={(e) => setFormData({ ...formData, email: e.target.value.trim() })}
          required
        />

        <div className="password-container">
          <input
            type={showPassword ? "text" : "password"}
            placeholder="Password"
            value={formData.password}
            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            required
          />
          <span
            onClick={() => setShowPassword(!showPassword)}
            className={`eye-icon ${showPassword ? "open" : "closed"}`}
          >
            <FontAwesomeIcon icon={showPassword ? faEye : faEyeSlash} />
          </span>
        </div>

        <button type="submit" className="auth-button">Sign In</button>
      </form>

      <div className="signup-section">
        <p>Don't have an account?</p>
        <button onClick={() => navigate("/signup")} className="auth-button">
          Sign Up
        </button>
      </div>
    </div>
  );
};

export default SignIn;
