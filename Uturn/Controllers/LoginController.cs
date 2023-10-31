using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using Microsoft.Extensions.Logging;
using Firebase;
using Firebase.Database;
using Firebase.Database.Query;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Uturn.Controllers
{
    [AllowAnonymous]
    public class LoginController : Controller
    {
        private readonly ILogger<LoginController> _logger;

        public LoginController(ILogger<LoginController> logger)
        {
            _logger = logger;
        }

        public IActionResult Index()
        {
            return View();
        }

        [HttpPost]
        public async Task<IActionResult> Index(string Email, string Password, bool RememberMe)
        {
            var firebaseClient = new FirebaseClient("https://utrun-8aee7-default-rtdb.firebaseio.com");

            var snapshot = await firebaseClient.Child("login").Child("email").OnceAsync<object>();
            _logger.LogInformation($"User count: {snapshot.Count}");

            var user = snapshot.Select(s => s.Object as JObject)
                          .FirstOrDefault(u => string.Equals(u?["Email"]?.ToString().Trim(), Email, StringComparison.OrdinalIgnoreCase));

            if (user != null && user["password"]?.ToString() == Password)
            {
                if (user["Role"]?.ToString() == "Admin")
                {
                    _logger.LogInformation("User logged in.");
                    return RedirectToAction("Index", "Task");
                }
                else
                {
                    _logger.LogInformation("Unauthorized.");
                    ModelState.AddModelError(string.Empty, "You don't have admin privileges.");
                    return View();
                }
            }
            else
            {
                _logger.LogInformation("Invalid login attempt.");
                ModelState.AddModelError(string.Empty, "Invalid login attempt.");
                return View();
            }
        }
    }
}
