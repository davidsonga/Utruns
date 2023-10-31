using Microsoft.AspNetCore.Mvc;
using Uturn.Models;
using Firebase.Database;
using Firebase.Database.Query;
using FirebaseAdmin.Auth;

namespace Uturn.Controllers
{
    public class EmployeeController : Controller
    {
        private readonly ILogger<EmployeeController> _logger;
        private FirebaseClient _firebaseClient;

        public EmployeeController(ILogger<EmployeeController> logger)
        {
            _logger = logger;
            _firebaseClient = new FirebaseClient("https://utrun-8aee7-default-rtdb.firebaseio.com");
        }

        public async Task<IActionResult> Index()
        {
            // Fetch data from Firebase
            var employeesFromFirebase = await _firebaseClient
                .Child("login")
                .Child("email")
                .OnceAsync<Employee>();

            // Convert the fetched data to a list of Employee model objects
            List<Employee> employees = employeesFromFirebase.Select(item => new Employee
            {
                key = item.Key,
                Email = item.Object.Email,
                Role = item.Object.Role,
                name = item.Object.name,
                state = item.Object.state,
                surname = item.Object.surname
            }).ToList();

            return View(employees);
        }

        // GET: Employee/Create
        public IActionResult Create()
        {
            return View();
        }

        [HttpPost]
        public async Task<IActionResult> Create(Employee Input)
        {
            _logger.LogInformation($"User email: {Input.Email}");
            foreach (var modelStateKey in ModelState.Keys)
            {
                var modelStateVal = ModelState[modelStateKey];
                foreach (var error in modelStateVal.Errors)
                {
                    _logger.LogError($"Key: {modelStateKey}, Error: {error.ErrorMessage}");
                }
            }
            if (ModelState.IsValid)
            {
                // Create user in Firebase Authentication
                var userArgs = new UserRecordArgs()
                {
                    Email = Input.Email,
                    EmailVerified = false,
                    Password = "12345678",
                    Disabled = false
                };

                UserRecord userRecord = null;
                try
                {
                    userRecord = await FirebaseAuth.DefaultInstance.CreateUserAsync(userArgs);
                    string uid = userRecord.Uid;

                    var employee = new Employee
                    {
                        Email = Input.Email,
                        FCMToken = "",
                        Picture = "",
                        Role = "User",
                        name = Input.name,
                        password = "12345678",
                        state = "",
                        surname = Input.surname,
                        typing = false
                    };

                    await _firebaseClient.Child("login").Child("email").Child(uid).PutAsync(employee);
                }
                catch (FirebaseAuthException ex)
                {
                    _logger.LogError($"Error creating Firebase user: {ex.Message}");
                    return View(Input);
                }
            }
            return RedirectToAction("Index");
        }

        // GET: Employee/Edit
        public async Task<IActionResult> Edit(string key)
        {
            if (string.IsNullOrEmpty(key))
            {
                return NotFound();
            }

            // Fetch the data from Firebase
            var employee = await _firebaseClient
                .Child("login")
                .Child("email")
                .Child(key)
                .OnceSingleAsync<Models.Employee>();

            if (employee == null)
            {
                return NotFound();
            }

            return View(employee);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(Models.Employee Input)
        {
            if (string.IsNullOrEmpty(Input.key))
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                await _firebaseClient
                    .Child("login")
                    .Child("email")
                    .Child(Input.key)
                    .PutAsync(Input);

                return RedirectToAction("Index");
            }

            return View(Input);
        }

        public async Task<IActionResult> Delete(string key)
        {
            if (string.IsNullOrEmpty(key))
            {
                return NotFound();
            }

            await FirebaseAuth.DefaultInstance.DeleteUserAsync(key);

            // Delete the data from Firebase
            await _firebaseClient
                .Child("login")
                .Child("email")
                .Child(key)
                .DeleteAsync();

            return RedirectToAction("Index");
        }
    }
}
