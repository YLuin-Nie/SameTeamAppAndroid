using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SameTeamAPI.Models;

namespace SameTeamAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ChoresController : ControllerBase
{
    private readonly SameTeamDbContext _context;

    public ChoresController(SameTeamDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<Chore>>> GetChores()
    {
        return await _context.Chores.ToListAsync();
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<Chore>> GetChore(int id)
    {
        var chore = await _context.Chores.FindAsync(id);
        if (chore == null) return NotFound();
        return chore;
    }

    [HttpPost]
    public async Task<ActionResult<Chore>> CreateChore(Chore chore)
    {
        _context.Chores.Add(chore);
        await _context.SaveChangesAsync();
        return CreatedAtAction(nameof(GetChore), new { id = chore.ChoreId }, chore);
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateChore(int id, Chore chore)
    {
        if (id != chore.ChoreId) return BadRequest();

        _context.Entry(chore).State = EntityState.Modified;
        await _context.SaveChangesAsync();
        return NoContent();
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteChore(int id)
    {
        var chore = await _context.Chores.FindAsync(id);
        if (chore == null) return NotFound();

        _context.Chores.Remove(chore);
        await _context.SaveChangesAsync();
        return NoContent();
    }
}
