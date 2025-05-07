<<<<<<< HEAD
# SameTeamAppAndroid

A Kotlin-based Android version of the SameTeamApp, originally built in React with an ASP.NET Core backend. This mobile app helps parents and children manage chores, track rewards, and promote teamwork at home.

## 📱 Description

The SameTeamAppAndroid is a mobile productivity and parenting tool where parents can assign chores, children can complete them, and everyone can stay on the same page. It is built to connect to an existing .NET Core API backend using JWT authentication.

## 🚀 Technologies Used

- **Kotlin** – Main programming language
- **Android Studio** – IDE for Android development
- **Jetpack Compose & XML Layouts** – UI structure
- **Retrofit** – For making API calls to the backend
- **LiveData / ViewModel** – For UI state management
- **.NET Core Web API** – Backend service for user authentication, chores, and rewards

## 🔒 Features

- User Authentication (Sign In / Sign Up with Team creation)
- Role-based access (Parent / Child)
- Parent Dashboard: Add, assign, and track chores and rewards
- Child Dashboard: View and complete assigned chores
- Reward System: Earn points and redeem them
- Real-time sync planned for future versions (SignalR or Firebase)

## 🌐 Backend API

The app communicates with an ASP.NET Core backend hosted locally.
Key Endpoints:
- `POST /Auth/register` – User Registration
- `POST /Auth/login` – User Login
- `GET /Chores`, `POST /Chores` – Chore Management
- `GET /Rewards`, `POST /Rewards` – Reward System
=======
# SameTeamAppAndroid

A Kotlin-based Android version of the SameTeamApp, originally built in React with an ASP.NET Core backend. This mobile app helps parents and children manage chores, track rewards, and promote teamwork at home.

## 📱 Description

The SameTeamAppAndroid is a mobile productivity and parenting tool where parents can assign chores, children can complete them, and everyone can stay on the same page. It is built to connect to an existing .NET Core API backend using JWT authentication.

## 🚀 Technologies Used

- **Kotlin** – Main programming language
- **Android Studio** – IDE for Android development
- **Jetpack Compose & XML Layouts** – UI structure
- **Retrofit** – For making API calls to the backend
- **LiveData / ViewModel** – For UI state management (optional, if used)
- **.NET Core Web API** – Backend service for user authentication, chores, and rewards

## 🔒 Features

- User Authentication (Sign In / Sign Up with Team creation)
- Role-based access (Parent / Child)
- Parent Dashboard: Add, assign, and track chores and rewards
- Child Dashboard: View and complete assigned chores
- Reward System: Earn points and redeem them
- Real-time sync planned for future versions (SignalR or Firebase)

## 🌐 Backend API

The app communicates with an ASP.NET Core backend hosted locally.
Key Endpoints:
- `POST /Auth/register` – User Registration
- `POST /Auth/login` – User Login
- `GET /Chores`, `POST /Chores` – Chore Management
- `GET /Rewards`, `POST /Rewards` – Reward System
>>>>>>> 89204d1bc7b85387030a41a9edcb442e8670a02c
