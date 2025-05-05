using System;
using System.Collections.Generic;

namespace SameTeamAPI.Models;

public partial class Chore
{
    public int ChoreId { get; set; }

    public string ChoreText { get; set; } = null!;

    public int Points { get; set; }

    public int? AssignedTo { get; set; }

    public DateOnly DateAssigned { get; set; }

    public bool? Completed { get; set; }

    public virtual User? AssignedToNavigation { get; set; }

    public virtual ICollection<CompletedChore> CompletedChores { get; set; } = new List<CompletedChore>();
}
