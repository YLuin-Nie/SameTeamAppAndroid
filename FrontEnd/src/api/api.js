import axios from 'axios';

const FORCE_HTTP = true;

const api = axios.create({
  baseURL: FORCE_HTTP
    ? 'http://localhost:5073/api'
    : `${window.location.protocol}//localhost:5073/api`,
});


// Automatically attach token if it exists
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;

export const fetchChores = async () => {
  const response = await api.get('/Chores');
  return response.data;
};

export const postChore = async (chore) => {
  const response = await api.post('/Chores', chore);
  return response.data;
};

export const fetchUsers = async () => {
  try {
    const response = await api.get('/Users');
    return response.data;
  } catch (err) {
    console.error("fetchUsers error:", err.message);
    throw err;
  }
};

export const loginUser = async (email, password) => {
  const response = await api.post('/Auth/login', { email, password });
  return response.data; // Should return a user object and possibly a token
};

export const completeChore = async (choreId, updatedChore) => {
  const response = await api.put(`/Chores/${choreId}`, updatedChore);
  return response.data;
};

// GET all rewards
export const fetchRewards = async () => {
  const res = await api.get('/Rewards');
  return res.data;
};

// POST a new reward
export const postReward = async (reward) => {
  const res = await api.post('/Rewards', reward);
  return res.data;
};

// PUT (edit) reward
export const updateReward = async (rewardId, reward) => {
  const res = await api.put(`/Rewards/${rewardId}`, reward);
  return res.data;
};

// DELETE reward
export const deleteReward = async (rewardId) => {
  await api.delete(`/Rewards/${rewardId}`);
};

// POST completed reward chore
export const rewardAsChore = async (chore) => {
  const res = await api.post('/Chores', chore); // Use chore model to reward
  return res.data;
};

// Fetch redeemed rewards for a specific user
export const fetchRedeemedRewards = async (userId) => {
  const res = await api.get(`/RedeemedRewards/${userId}`);
  return res.data;
};

// Post a new redeemed reward
export const postRedeemedReward = async (data) => {
  const res = await api.post(`/RedeemedRewards`, data);
  return res.data;
};

export const updateUser = async (userId, userData) => {
  const res = await api.put(`/Users/${userId}`, userData);
  return res.data;
};

export const deleteChore = async (choreId) => {
  await api.delete(`/Chores/${choreId}`);
};
