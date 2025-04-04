// File Nme: RedeemedRewardsController.cs

using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SameTeamAPI.Models;

namespace SameTeamAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class RedeemedRewardsController : ControllerBase
    {
        private readonly SameTeamDbContext _context;

        public RedeemedRewardsController(SameTeamDbContext context)
        {
            _context = context;
        }

        // GET: api/RedeemedRewards
        [HttpGet]
        public async Task<ActionResult<IEnumerable<RedeemedReward>>> GetAllRedeemedRewards()
        {
            return await _context.RedeemedRewards
                .Include(r => r.User)
                .Include(r => r.Reward)
                .ToListAsync();
        }

        // GET: api/RedeemedRewards/5
        [HttpGet("{userId}")]
        public async Task<ActionResult<IEnumerable<RedeemedReward>>> GetRedeemedRewardsByUser(int userId)
        {
            var userRewards = await _context.RedeemedRewards
                .Include(r => r.Reward)
                .Where(r => r.UserId == userId)
                .OrderByDescending(r => r.DateRedeemed)
                .ToListAsync();

            if (userRewards == null || userRewards.Count == 0)
            {
                return NotFound();
            }

            return userRewards;
        }

        // POST: api/RedeemedRewards
        [HttpPost]
        public async Task<ActionResult<RedeemedReward>> PostRedeemedReward(RedeemedReward reward)
        {
            reward.DateRedeemed = DateTime.UtcNow;

            _context.RedeemedRewards.Add(reward);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetRedeemedRewardsByUser), new { userId = reward.UserId }, reward);
        }
    }
}
