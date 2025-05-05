import bcrypt from "bcryptjs";

//  Function to initialize default users and data in localStorage
export const initializeLocalStorage = () => {
    const defaultUsers = [
        { username: "Andrew", password: bcrypt.hashSync("Andrew123", 10), email: "Andrew@yahoo.com", role: "Parent", team: "ABY" },
        { username: "Bansari", password: bcrypt.hashSync("Bansari123", 10), email: "Bansari@yahoo.com", role: "Parent", team: "ABY" },
        { username: "Yen", password: bcrypt.hashSync("Bob123", 10), email: "Yen@yahoo.com", role: "Parent", team: "ABY" },
        { username: "Bob", password: bcrypt.hashSync("Bob123", 10), email: "bob@yahoo.com", role: "Child", team: "ABY" },
        { username: "Luna", password: bcrypt.hashSync("Luna123", 10), email: "Luna@yahoo.com", role: "Child", team: "ABY" }
    ];
    
        if (!localStorage.getItem("users")) {
            localStorage.setItem("users", JSON.stringify(defaultUsers));
        }
    
        if (!localStorage.getItem("teamName")) {
            localStorage.setItem("teamName", "ABY");
        }
    
        if (!localStorage.getItem("chores")) {
            localStorage.setItem("chores", JSON.stringify([]));
        }
    };
    
    // Retrieve users from localStorage
    export const getUsers = () => JSON.parse(localStorage.getItem("users")) || [];
    
    // Save updated users to localStorage
    export const saveUsers = (users) => {
        localStorage.setItem("users", JSON.stringify(users));
    };
    
    // Authenticate user login using email and password
    export const authenticateUser = (email, password) => {
        const users = getUsers();
        const user = users.find(user => user.email.toLowerCase() === email.toLowerCase());
    
        if (user && bcrypt.compareSync(password, user.password)) {
            localStorage.setItem("loggedInUser", JSON.stringify(user)); // Store logged-in user
            return user;
        }
    
        return null; // Return null if authentication fails
    };
    
    // Get the currently logged-in user
    export const getCurrentUser = () => {
        return JSON.parse(localStorage.getItem("loggedInUser")) || null;
    };
    
    // Log out the user by removing session data
    export const logoutUser = () => {
        localStorage.removeItem("loggedInUser");
    };
    
    // Retrieve chores from localStorage
    export const getChores = () => {
        const chores = JSON.parse(localStorage.getItem("chores")) || [];
        return chores.map(chore => ({
            ...chore,
            points: chore.points || 5 // Default points to 5 if not set
        }));
    };
    
    // Save updated chores to localStorage
    export const saveChores = (chores) => {
        localStorage.setItem("chores", JSON.stringify(chores));
    };
    
    // Add a new chore to storage
    export const addChoreToStorage = (chore) => {
        const chores = getChores();
        const newChore = {
            ...chore,
            points: chore.points || 5,
            date: new Date(chore.date).toISOString()
        };
        chores.push(newChore);
        saveChores(chores);
    };
    
    // ✅ Restore `getUserPoints`
    export const getUserPoints = (username) => {
        const users = getUsers();
        const user = users.find(user => user.username === username);
        return user ? user.points || 0 : 0;
    };
    
    // Update points for a specific user
    export const updateUserPoints = (username, points) => {
        const users = getUsers();
        const userIndex = users.findIndex(user => user.username === username);
    
        if (userIndex !== -1) {
            const user = users[userIndex];
    
            user.totalPoints = (user.totalPoints || 0) + (points - (user.points || 0)); // Track total earned points
            user.points = points; // Unspent points
    
            saveUsers(users);
        }
    };
    
    // Retrieve total points earned since account creation
    export const getTotalPoints = (username) => {
        const users = getUsers();
        const user = users.find(user => user.username === username);
        return user ? user.totalPoints || 0 : 0;
    };
    
    // Retrieve unspent points (for redeeming rewards)
    export const getUnspentPoints = (username) => {
        const users = getUsers();
        const user = users.find(user => user.username === username);
        return user ? user.points || 0 : 0;
    };
    
    // Leveling system based on total earned points
    export const getUserLevel = (totalPoints) => {
        if (totalPoints >= 1000) return { level: 5, name: "Master", color: "red" };
        if (totalPoints >= 600) return { level: 4, name: "Elite", color: "orange" };
        if (totalPoints >= 400) return { level: 3, name: "Challenger", color: "green" };
        if (totalPoints >= 200) return { level: 2, name: "Apprentice", color: "blue" };
        return { level: 1, name: "Beginner", color: "brown" };
    };
    
    // Retrieve user level from total points
    export const getUserLevelFromStorage = (username) => {
        const users = getUsers();
        const user = users.find(user => user.username === username);
        return user ? getUserLevel(user.totalPoints || 0) : { level: 1, name: "Beginner", color: "brown" };
    };
    
    // Mark chore as completed in storage
    export const completeChoreInStorage = (choreId) => {
        const chores = getChores();
        const updatedChores = chores.map(chore =>
            chore.id === choreId ? { ...chore, completed: true } : chore
        );
    
        saveChores(updatedChores);
    };
    
    // Function to store rewarded points as a completed chore
    export const addRewardAsCompletedChore = (username, rewardName, rewardPoints) => {
        const chores = getChores();
        const newChore = {
            id: Date.now(),
            text: `Reward: ${rewardName}`,
            completed: true,
            points: rewardPoints,
            assignedTo: username,
            date: new Date().toISOString(), // Store with the current date
        };
        chores.push(newChore);
        saveChores(chores);
    };

    // Function to store redeemed rewards in localStorage
    export const storeRedeemedReward = (username, rewardName, rewardPoints) => {
        const redeemedRewards = JSON.parse(localStorage.getItem("redeemedRewards")) || [];
        
        const newRedemption = {
            id: Date.now(),
            username,
            rewardName,
            pointsSpent: rewardPoints,
            date: new Date().toISOString(),
        };

        redeemedRewards.push(newRedemption);
        localStorage.setItem("redeemedRewards", JSON.stringify(redeemedRewards));
    };

    // Retrieve redeemed rewards
    export const getRedeemedRewards = () => {
        return JSON.parse(localStorage.getItem("redeemedRewards")) || [];
    };

    // Function to store rewards in localStorage
    export const saveRewards = (rewards) => {
        localStorage.setItem("rewards", JSON.stringify(rewards));
    };

    // Function to get rewards from localStorage
    export const getRewards = () => {
        return JSON.parse(localStorage.getItem("rewards")) || [
            { id: 1, name: "Go out and eat", cost: 50 },
            { id: 2, name: "No Chores Day", cost: 75 },
            { id: 3, name: "Go to the Movies", cost: 100 }
        ];
    };
