using System;
using System.Collections.Generic;

namespace SameTeamAPI.Models;

public partial class RedeemedReward
{
    public int RedemptionId { get; set; }

    public int? UserId { get; set; }

    public int? RewardId { get; set; }

    public int PointsSpent { get; set; }

    public DateTime? DateRedeemed { get; set; }

    public virtual Reward? Reward { get; set; }

    public virtual User? User { get; set; }
}
