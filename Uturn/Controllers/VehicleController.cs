using System;
using Firebase.Database;
using Firebase.Database.Query;
using FirebaseAdmin.Auth;
using Microsoft.AspNetCore.Mvc;

namespace Uturn.Controllers
{
	public class VehicleController : Controller
	{
        private readonly ILogger<VehicleController> _logger;
        private FirebaseClient _firebaseClient;

        public VehicleController(ILogger<VehicleController> logger)
        {
            _logger = logger;
            _firebaseClient = new FirebaseClient("https://utrun-8aee7-default-rtdb.firebaseio.com");
        }

        public async Task<IActionResult> Index()
        {
            // Fetch the tasks from Firebase
            var vehiclesFromFirebase = await _firebaseClient.Child("vehicles").OnceAsync<Models.Vehicle>();

            // Convert the fetched data to a list of vehicle model objects
            List<Models.Vehicle> vehicles = vehiclesFromFirebase.Select(vehicle => new Models.Vehicle
            {
                key = vehicle.Key,
                model = vehicle.Object.model,
                brand = vehicle.Object.brand,
                type = vehicle.Object.type,
                numberPlate = vehicle.Object.numberPlate,
                currentLocation = vehicle.Object.currentLocation,
                isAvailable = vehicle.Object.isAvailable
            }).ToList();

            return View(vehicles);
        }

        // GET: Vehicle/Create
        public IActionResult Create()
        {
            return View();
        }

        [HttpPost]
        public async Task<IActionResult> Create(Models.Vehicle Input)
        {
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
                try
                {
                    await _firebaseClient.Child("vehicles").PostAsync(Input);
                }
                catch (FirebaseAuthException ex)
                {
                    _logger.LogError($"Error creating Firebase user: {ex.Message}");
                    return View(Input);
                }
            }
            return RedirectToAction("Index");
        }

        // GET: Vehicle/Edit
        public async Task<IActionResult> Edit(string key)
        {
            if (string.IsNullOrEmpty(key))
            {
                return NotFound();
            }

            // Fetch the data from Firebase
            var vehicle = await _firebaseClient
                .Child("vehicles")
                .Child(key)
                .OnceSingleAsync<Models.Vehicle>();

            if (vehicle == null)
            {
                return NotFound();
            }

            return View(vehicle);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(Models.Vehicle Input)
        {
            if (string.IsNullOrEmpty(Input.key))
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                await _firebaseClient
                    .Child("vehicles")
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

            // Delete the data from Firebase
            await _firebaseClient
                .Child("vehicles")
                .Child(key)
                .DeleteAsync();

            return RedirectToAction("Index");
        }
    }
}

