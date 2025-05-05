using System;
using System.Collections.Generic;

namespace SameTeamAPI.Models;

public partial class CompletedChore
{
    public int CompletedId { get; set; }

    public int? ChoreId { get; set; }

    public int? UserId { get; set; }

    public DateTime? CompletionDate { get; set; }

    public virtual Chore? Chore { get; set; }

    public virtual User? User { get; set; }
}
