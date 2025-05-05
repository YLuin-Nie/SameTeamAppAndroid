using Microsoft.AspNetCore.Mvc;

namespace SameTeamAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class TestController : ControllerBase
    {
        [HttpGet]
        public IActionResult Get()
        {
            return Ok("Hello from TestController!");
        }
    }
}
