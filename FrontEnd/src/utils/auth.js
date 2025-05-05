// src/utils/auth.js
export function getCurrentUser() {
    const user = localStorage.getItem("loggedInUser");
    return user ? JSON.parse(user) : null;
  }
  