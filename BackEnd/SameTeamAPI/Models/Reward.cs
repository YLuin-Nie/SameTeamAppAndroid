using System;
using System.Collections.Generic;

namespace SameTeamAPI.Models;

public partial class Reward
{
    public int RewardId { get; set; }

    public string Name { get; set; } = null!;

    public int Cost { get; set; }

    public virtual ICollection<RedeemedReward> RedeemedRewards { get; set; } = new List<RedeemedReward>();
}
