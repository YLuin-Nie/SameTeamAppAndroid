-- Create Database Only If It Doesn't Exist  sss
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'SameTeamDB')
BEGIN
    CREATE DATABASE SameTeamDB;
END;
GO

USE SameTeamDB;
GO

-- Drop Tables in Correct Order (To Avoid FK Issues)
DROP TABLE IF EXISTS ChoreRepeat;
DROP TABLE IF EXISTS DayOfWeek;
DROP TABLE IF EXISTS UserChoreLog;
DROP TABLE IF EXISTS UserChoreStatus;
DROP TABLE IF EXISTS UserChore;
DROP TABLE IF EXISTS Chore;
DROP TABLE IF EXISTS UserReward;
DROP TABLE IF EXISTS Reward;
DROP TABLE IF EXISTS UserTeam;
DROP TABLE IF EXISTS AppUser;
DROP TABLE IF EXISTS Team;
DROP TABLE IF EXISTS Level;
DROP TABLE IF EXISTS LevelSystem;
DROP TABLE IF EXISTS LevelTemplate;
DROP TABLE IF EXISTS LevelSystemTemplate;
DROP TABLE IF EXISTS Role;

-- Create Tables

CREATE TABLE Role (
    RoleID INT PRIMARY KEY IDENTITY(1,1),
    RoleName VARCHAR(50)
);

CREATE TABLE LevelSystemTemplate (
    LevelSystemTemplateID INT PRIMARY KEY IDENTITY(1,1),
    TemplateName VARCHAR(50)
);

CREATE TABLE LevelTemplate (
    LevelTemplateID INT PRIMARY KEY IDENTITY(1,1),
    LevelName VARCHAR(50),
    LevelSystemTemplateID INT,
    FOREIGN KEY (LevelSystemTemplateID) REFERENCES LevelSystemTemplate(LevelSystemTemplateID)
);

CREATE TABLE LevelSystem (
    LevelSystemID INT PRIMARY KEY IDENTITY(1,1),
    LevelSystemTemplateID INT,
    FOREIGN KEY (LevelSystemTemplateID) REFERENCES LevelSystemTemplate(LevelSystemTemplateID)
);

CREATE TABLE Level (
    LevelID INT PRIMARY KEY IDENTITY(1,1),
    LevelName VARCHAR(50),
    LevelSystemID INT,
    Points INT,
    FOREIGN KEY (LevelSystemID) REFERENCES LevelSystem(LevelSystemID)
);

CREATE TABLE Team (
    TeamID INT PRIMARY KEY IDENTITY(1,1),
    TeamName VARCHAR(100),
    DefaultLevelSystemID INT,
    FOREIGN KEY (DefaultLevelSystemID) REFERENCES LevelSystem(LevelSystemID)
);

CREATE TABLE AppUser (  -- Renamed from "User"
    UserID INT PRIMARY KEY IDENTITY(1,1),
    FirstName VARCHAR(50),
    LastName VARCHAR(50),
    Nickname VARCHAR(50),
    Email VARCHAR(100) UNIQUE,
    Password VARCHAR(255),
    RoleID INT,
    TeamID INT,
    FOREIGN KEY (RoleID) REFERENCES Role(RoleID),
    FOREIGN KEY (TeamID) REFERENCES Team(TeamID)
);

CREATE TABLE UserTeam (
    UserTeamID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT,
    TeamID INT,
    LevelSystemID INT,
    FOREIGN KEY (UserID) REFERENCES AppUser(UserID),
    FOREIGN KEY (TeamID) REFERENCES Team(TeamID),
    FOREIGN KEY (LevelSystemID) REFERENCES LevelSystem(LevelSystemID)
);

CREATE TABLE Reward (
    RewardID INT PRIMARY KEY IDENTITY(1,1),
    RewardName VARCHAR(50),
    PointsRequired INT
);

CREATE TABLE UserReward (
    UserRewardID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT,
    RewardID INT,
    DateEarned DATE,
    FOREIGN KEY (UserID) REFERENCES AppUser(UserID),
    FOREIGN KEY (RewardID) REFERENCES Reward(RewardID)
);

CREATE TABLE Chore (
    ChoreID INT PRIMARY KEY IDENTITY(1,1),
    ChoreName VARCHAR (50),
    ChorePoints INT,
    Description VARCHAR(250),
    IsRepeating BIT DEFAULT(0),
    DueDate DATE
);

CREATE TABLE UserChore (
    UserChoreID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT,
    ChoreID INT,
    FOREIGN KEY (UserID) REFERENCES AppUser(UserID),
    FOREIGN KEY (ChoreID) REFERENCES Chore(ChoreID)
);

CREATE TABLE UserChoreStatus (
    StatusID INT PRIMARY KEY IDENTITY(1,1),
    StatusName VARCHAR(20)
);

CREATE TABLE UserChoreLog (
    UserChoreLogID INT PRIMARY KEY IDENTITY(1,1),
    UserChoreID INT,
    CompletionDate DATE,
    StatusID INT,
    FOREIGN KEY (UserChoreID) REFERENCES UserChore(UserChoreID),
    FOREIGN KEY (StatusID) REFERENCES UserChoreStatus(StatusID)
);

CREATE TABLE DayOfWeek (
    DayOfWeekCode INT PRIMARY KEY IDENTITY(1,1),
    DayName VARCHAR(20)
);

CREATE TABLE ChoreRepeat (
    ChoreRepeatID INT PRIMARY KEY IDENTITY(1,1),
    ChoreID INT,
    DayOfWeekCode INT,
    FOREIGN KEY (ChoreID) REFERENCES Chore(ChoreID),
    FOREIGN KEY (DayOfWeekCode) REFERENCES DayOfWeek(DayOfWeekCode)
);
