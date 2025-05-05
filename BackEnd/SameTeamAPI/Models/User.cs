using System;
using System.Collections.Generic;

namespace SameTeamAPI.Models;

public partial class User
{
    public int UserId { get; set; }

    public string Username { get; set; } = null!;

    public string Email { get; set; } = null!;

    public string PasswordHash { get; set; } = null!;

    public string Role { get; set; } = null!;

    public int? Points { get; set; }

    public int? TotalPoints { get; set; }

    public int? TeamId { get; set; }

    public virtual ICollection<Chore> Chores { get; set; } = new List<Chore>();

    public virtual ICollection<CompletedChore> CompletedChores { get; set; } = new List<CompletedChore>();

    public virtual ICollection<RedeemedReward> RedeemedRewards { get; set; } = new List<RedeemedReward>();

    public virtual Team? Team { get; set; }
}
