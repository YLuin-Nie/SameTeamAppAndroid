using System;
using System.Collections.Generic;

namespace SameTeamAPI.Models;

public partial class Team
{
    public int TeamId { get; set; }

    public string TeamName { get; set; } = null!;

    public virtual ICollection<User> Users { get; set; } = new List<User>();
}
