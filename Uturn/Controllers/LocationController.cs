using System;
using Uturn.ViewModels;
using Firebase.Database;
using Firebase.Database.Query;
using FirebaseAdmin.Auth;
using Microsoft.AspNetCore.Mvc;

namespace Uturn.Controllers
{
	public class LocationController : Controller
	{
        private readonly ILogger<LocationController> _logger;
        private FirebaseClient _firebaseClient;

        public LocationController(ILogger<LocationController> logger)
        {
            _logger = logger;
            _firebaseClient = new FirebaseClient("https://utrun-8aee7-default-rtdb.firebaseio.com");
        }

        public async Task<IActionResult> Index()
        {
            // Fetch the tasks from Firebase
            var locationsFromFirebase = await _firebaseClient.Child("locations").OnceAsync<Models.Location>();

            // Convert the fetched data to a list of Location model objects
            List<Models.Location> locations = locationsFromFirebase.Select(location => new Models.Location
            {
                key = location.Key,
                name = location.Object.name,
                type = location.Object.type,
                address = location.Object.address
            }).ToList();

            return View(locations);
        }

        // GET: Location/Create
        public IActionResult Create()
        {
            return View();
        }

        [HttpPost]
        public async Task<IActionResult> Create(Models.Location Input)
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
                    await _firebaseClient.Child("locations").PostAsync(Input);
                }
                catch (FirebaseAuthException ex)
                {
                    _logger.LogError($"Error creating Firebase user: {ex.Message}");
                    return View(Input);
                }
            }
            return RedirectToAction("Index");
        }

        // GET: Location/Edit
        public async Task<IActionResult> Edit(string key)
        {
            if (string.IsNullOrEmpty(key))
            {
                return NotFound();
            }

            // Fetch the data from Firebase
            var location = await _firebaseClient
                .Child("locations")
                .Child(key)
                .OnceSingleAsync<Models.Location>();

            if (location == null)
            {
                return NotFound();
            }

            return View(location);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(Models.Location Input)
        {
            if (string.IsNullOrEmpty(Input.key))
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                await _firebaseClient
                    .Child("locations")
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
                .Child("locations")
                .Child(key)
                .DeleteAsync();

            return RedirectToAction("Index");
        }
    }
}

