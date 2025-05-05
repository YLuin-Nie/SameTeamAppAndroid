/********************************************************
* This script creates the database  
*********************************************************/
-- Drop the database if it already exists
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'SameTeamDB')
    DROP DATABASE SameTeamDB;
GO

-- Create a new database
CREATE DATABASE SameTeamDB;
GO

-- Use the new database
USE SameTeamDB;
GO

/********************************************************
* This section creates the Tables
*********************************************************/
-- Teams Table
CREATE TABLE Teams (
    TeamID INT IDENTITY(1,1) PRIMARY KEY,
    TeamName NVARCHAR(100) NOT NULL UNIQUE
);
GO

-- Users Table
CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    Username NVARCHAR(50) NOT NULL,
    Email NVARCHAR(100) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    Role NVARCHAR(10) CHECK (Role IN ('Parent', 'Child')) NOT NULL,
    Points INT DEFAULT 0,
    TotalPoints INT DEFAULT 0,
    TeamID INT FOREIGN KEY REFERENCES Teams(TeamID)
);
GO

-- Chores Table
CREATE TABLE Chores (
    ChoreID INT IDENTITY(1,1) PRIMARY KEY,
    ChoreText NVARCHAR(255) NOT NULL,
    Points INT NOT NULL DEFAULT 5,
    AssignedTo INT FOREIGN KEY REFERENCES Users(UserID),
    DateAssigned DATE NOT NULL,
    Completed BIT DEFAULT 0
);
GO

-- Rewards Table
CREATE TABLE Rewards (
    RewardID INT IDENTITY(1,1) PRIMARY KEY,
    Name NVARCHAR(100) NOT NULL,
    Cost INT NOT NULL
);
GO

-- RedeemedRewards Table
CREATE TABLE RedeemedRewards (
    RedemptionID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT FOREIGN KEY REFERENCES Users(UserID),
    RewardID INT FOREIGN KEY REFERENCES Rewards(RewardID),
    PointsSpent INT NOT NULL,
    DateRedeemed DATETIME DEFAULT GETDATE()
);
GO

-- Optional: CompletedChores Log Table
CREATE TABLE CompletedChores (
    CompletedID INT IDENTITY(1,1) PRIMARY KEY,
    ChoreID INT FOREIGN KEY REFERENCES Chores(ChoreID),
    UserID INT FOREIGN KEY REFERENCES Users(UserID),
    CompletionDate DATETIME DEFAULT GETDATE()
);
GO

/********************************************************
* This section populates the Tables
*********************************************************/
SET IDENTITY_INSERT Teams ON;
INSERT INTO Teams (TeamID, TeamName)
VALUES
(1, 'Avengers'),
(2, 'Justice'),
(3, 'Supers');
SET IDENTITY_INSERT Teams OFF;

SET IDENTITY_INSERT Rewards ON;
INSERT INTO Rewards (RewardID, Name, Cost)
VALUES 
    (1, 'Ice Cream', 10),
    (2, 'Movie Night', 25),
    (3, 'Extra Screen Time', 15);
SET IDENTITY_INSERT Rewards OFF;
	
SET IDENTITY_INSERT Users ON;
INSERT INTO Users (userId, username, email, passwordHash, role, points, totalPoints, teamId)
VALUES 
    (1, 'Bansari', 'Bansari@example.com', 'Ban123', 'Parent', 0, 0, 1),
    (2, 'Yen', 'Yen@example.com', 'Bob123', 'Parent', 0, 0, 1),
    (3, 'Luna', 'Luna@example.com', 'Bob123', 'Child', 0, 0, 1),
	(4, 'Bob', 'Bob@example.com', 'Bob123', 'Child', 0, 0, 1);
SET IDENTITY_INSERT Users OFF;

