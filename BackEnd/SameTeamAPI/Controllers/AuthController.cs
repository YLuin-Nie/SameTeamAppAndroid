using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using SameTeamAPI.Models;
using Microsoft.AspNetCore.Identity;

namespace SameTeamAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly SameTeamDbContext _context;
        private readonly IConfiguration _configuration;

        public AuthController(SameTeamDbContext context, IConfiguration configuration)
        {
            _context = context;
            _configuration = configuration;
        }

        [HttpPost("login")]
        public IActionResult Login([FromBody] LoginModel login)
        {
            var user = _context.Users
                .FirstOrDefault(u => u.Email.ToLower() == login.Email.ToLower());

            if (user == null)
                return Unauthorized("User not found");

            // ⚠️ TEMPORARY: Plain-text comparison for testing
            if (user.PasswordHash != login.Password)
                return Unauthorized("Invalid password");

            var token = GenerateJwtToken(user);

            return Ok(new
            {
                token,
                user = new
                {
                    user.UserId,
                    user.Username,
                    user.Email,
                    user.Role,
                    user.Points,
                    user.TeamId
                }
            });
        }


        [HttpPost("register")]
        public IActionResult Register([FromBody] RegisterModel model)
        {
            if (_context.Users.Any(u => u.Email.ToLower() == model.Email.ToLower()))
            {
                return Conflict("Email already exists.");
            }

            var hashedPassword = new PasswordHasher<User>().HashPassword(null, model.Password);

            var user = new User
            {
                Username = model.Username,
                Email = model.Email.ToLower(),
                PasswordHash = hashedPassword,
                Role = model.Role ?? "User",
                Points = 0,
                TotalPoints = 0,
                TeamId = null
            };

            _context.Users.Add(user);
            _context.SaveChanges();

            return Ok("User registered successfully!");
        }

        private string GenerateJwtToken(User user)
        {
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_configuration["Jwt:Key"]));
            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var claims = new[]
            {
                new Claim(JwtRegisteredClaimNames.Sub, user.Email ?? ""),
                new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
                new Claim("UserId", user.UserId.ToString())
            };

            var token = new JwtSecurityToken(
                issuer: _configuration["Jwt:Issuer"],
                audience: _configuration["Jwt:Audience"],
                claims: claims,
                expires: DateTime.Now.AddHours(2),
                signingCredentials: creds);

            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }

    public class LoginModel
    {
        public string Email { get; set; }
        public string Password { get; set; }
    }

    public class RegisterModel
    {
        public string Username { get; set; }
        public string Email { get; set; }
        public string Password { get; set; }
        public string? Role { get; set; } // optional
    }
}
