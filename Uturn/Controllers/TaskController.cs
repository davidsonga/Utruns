using System;
using Uturn.Models;
using Uturn.ViewModels;
using Firebase.Database;
using Firebase.Database.Query;
using FirebaseAdmin.Auth;
using Microsoft.AspNetCore.Mvc;
using Microsoft.CodeAnalysis;
using NuGet.Packaging.Signing;

namespace Uturn.Controllers
{
	public class TaskController : Controller
    {
        private readonly ILogger<TaskController> _logger;
        private FirebaseClient _firebaseClient;

        public TaskController(ILogger<TaskController> logger) {
            _logger = logger;
            _firebaseClient = new FirebaseClient("https://utrun-8aee7-default-rtdb.firebaseio.com");
        }

        public async Task<IActionResult> Index() {
            // Fetch the tasks from Firebase
            var tasks = await _firebaseClient.Child("tasks").OnceAsync<Models.Task>();

            // Extract unique user UIDs, location IDs, and vehicle IDs
            var employeeUids = tasks.Select(t => t.Object.employeeUid).Distinct().ToList();
            var locationIds = tasks.Select(t => t.Object.dropoffLocationId).Distinct().ToList();
            var vehicleIds = tasks.Select(t => t.Object.vehicleId).Distinct().ToList();
            var ratingIds = tasks.Select(t => t.Object.ratingId).Distinct().ToList();

            Dictionary<string, Models.Employee> employeeLookup = new Dictionary<string, Models.Employee>();

            // Fetch all employees at once.
            var employees = await _firebaseClient.Child("login").Child("email").OnceAsync<Models.Employee>();

            // Filter and populate the dictionary.
            foreach (var employeeObj in employees) {
                if (employeeUids.Contains(employeeObj.Key)) {
                    employeeLookup[employeeObj.Key] = employeeObj.Object;
                }
            }

            Dictionary<string, Models.Location> locationLookup = new Dictionary<string, Models.Location>();

            // Fetch all locations at once.
            var locations = await _firebaseClient.Child("locations").OnceAsync<Models.Location>();

            // Filter and populate the dictionary.
            foreach (var locationObj in locations) {
                if (locationIds.Contains(locationObj.Key)) {
                    locationLookup[locationObj.Key] = locationObj.Object;
                }
            }

            Dictionary<string, Models.Vehicle> vehicleLookup = new Dictionary<string, Models.Vehicle>();

            // Fetch all vehicles at once.
            var vehicles = await _firebaseClient.Child("vehicles").OnceAsync<Models.Vehicle>();

            // Filter and populate the dictionary.
            foreach (var vehicleObj in vehicles) {
                if (vehicleIds.Contains(vehicleObj.Key)) {
                    vehicleLookup[vehicleObj.Key] = vehicleObj.Object;
                }
            }

            Dictionary<string, Models.Rating> ratingLookup = new Dictionary<string, Models.Rating>();

            // Fetch all ratings at once.
            var ratings = await _firebaseClient.Child("ratings").OnceAsync<Models.Rating>();

            // Filter and populate the dictionary.
            foreach (var ratingObj in ratings)
            {
                if (ratingIds.Contains(ratingObj.Key))
                {
                    ratingLookup[ratingObj.Key] = ratingObj.Object;
                }
            }

            // Now for each task, I can retrieve the user, location, and vehicle data
            List<TaskViewModel> taskViewModels = new List<TaskViewModel>();

            foreach (var task in tasks)
            {
                // Safe retrieval of employee
                Models.Employee employee = null;
                if (!string.IsNullOrEmpty(task.Object.employeeUid) && employeeLookup.TryGetValue(task.Object.employeeUid, out var foundEmployee))
                {
                    employee = foundEmployee;
                }

                // Assuming that `dropoffLocationId` and `vehicleId` can also be potentially null or empty, handle them in a similar manner
                Models.Location location = null;
                if (!string.IsNullOrEmpty(task.Object.dropoffLocationId) && locationLookup.TryGetValue(task.Object.dropoffLocationId, out var foundLocation))
                {
                    location = foundLocation;
                }

                Models.Vehicle vehicle = null;
                if (!string.IsNullOrEmpty(task.Object.vehicleId) && vehicleLookup.TryGetValue(task.Object.vehicleId, out var foundVehicle))
                {
                    vehicle = foundVehicle;
                }

                Models.Rating rating = null;
                if (!string.IsNullOrEmpty(task.Object.ratingId) && ratingLookup.TryGetValue(task.Object.ratingId, out var foundRating))
                {
                    rating = foundRating;
                }

                var taskViewModel = new TaskViewModel
                {
                    key = task.Key,
                    employee = employee == null ? "N/A" : $"{employee.name} {employee.surname}",
                    vehicle = vehicle == null ? "N/A" : $"{vehicle.brand} - {vehicle.model} - {vehicle.numberPlate}",
                    rating = rating == null ? "N/A" : rating.rating, 
                    type = task.Object.type,
                    typeOfGoods = task.Object.typeOfGoods,
                    pickupLocation = task.Object.pickupLocation,
                    dropoffLocation = location == null ? "N/A" : $"{location.name} - {location.address}",
                    status = task.Object.status,
                    uploadedTimestamp = (DateTimeOffset.FromUnixTimeMilliseconds(task.Object.uploadedTimestamp).DateTime).ToString(),
                    assignedTimestamp = task.Object.assignedTimestamp == 0 ? "" : (DateTimeOffset.FromUnixTimeMilliseconds(task.Object.assignedTimestamp).DateTime).ToString(),
                    completedTimestamp = task.Object.completedTimestamp == 0 ? "" : (DateTimeOffset.FromUnixTimeMilliseconds(task.Object.completedTimestamp).DateTime).ToString()
                };

                taskViewModels.Add(taskViewModel);
            }

            return View(taskViewModels);
        }

        // GET: Task/Create
        public async Task<IActionResult> Create() {
            var locationsFromFirebase = await _firebaseClient
                .Child("locations")
                .OnceAsync<Models.Location>();

            // Convert the fetched data to a list of Location model objects
            List<Models.Location> locations = locationsFromFirebase.Select(location => new Models.Location {
                key = location.Key,
                name = location.Object.name,
                type = location.Object.type,
                address = location.Object.address
            }).ToList();

            TaskCreateViewModel viewModel = new TaskCreateViewModel
            {
                Locations = locations
            };

            return View(viewModel);
        }

        [HttpPost]
        public async Task<IActionResult> Create(TaskCreateViewModel Input) {
            ModelState.Remove("Locations");
            foreach (var modelStateKey in ModelState.Keys) {
                var modelStateVal = ModelState[modelStateKey];
                foreach (var error in modelStateVal.Errors) {
                    _logger.LogError($"Key: {modelStateKey}, Error: {error.ErrorMessage}");
                }
            }

            if (ModelState.IsValid) {
                try {
                    var task = new Models.Task{
                        type = Input.type,
                        typeOfGoods = Input.typeOfGoods,
                        pickupLocation = Input.pickupLocation,
                        dropoffLocationId = Input.selectedDropoffLocationKey,
                        uploadedTimestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
                    };

                    await _firebaseClient.Child("tasks").PostAsync(task);
                }
                catch (FirebaseAuthException ex) {
                    _logger.LogError($"Error creating Firebase user: {ex.Message}");
                    return View(Input);
                }
            }
            return RedirectToAction("Index");
        }

        // GET: Task/Edit
        public async Task<IActionResult> Edit(string key)
        {
            if (string.IsNullOrEmpty(key))
            {
                return NotFound();
            }

            // Fetch the data from Firebase
            var task = await _firebaseClient
                .Child("tasks")
                .Child(key)
                .OnceSingleAsync<Models.Task>();

            var locationsFromFirebase = await _firebaseClient
                .Child("locations")
                .OnceAsync<Models.Location>();

            // Convert the fetched data to a list of Location model objects
            List<Models.Location> locations = locationsFromFirebase.Select(location => new Models.Location
            {
                key = location.Key,
                name = location.Object.name,
                type = location.Object.type,
                address = location.Object.address
            }).ToList();

            TaskCreateViewModel viewModel = new TaskCreateViewModel
            {
                key = key,
                Locations = locations,
                selectedDropoffLocationKey = task.dropoffLocationId,
                type = task.type,
                typeOfGoods = task.typeOfGoods,
                pickupLocation = task.pickupLocation
            };

            return View(viewModel);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(ViewModels.TaskCreateViewModel Input)
        {
            ModelState.Remove("Locations");
            if (string.IsNullOrEmpty(Input.key))
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                await _firebaseClient
                    .Child("tasks")
                    .Child(Input.key)
                    .PatchAsync(new {
                        type = Input.type,
                        typeOfGoods = Input.typeOfGoods,
                        pickupLocation = Input.pickupLocation,
                        dropoffLocationId = Input.selectedDropoffLocationKey
                    });

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
                .Child("tasks")
                .Child(key)
                .DeleteAsync();

            return RedirectToAction("Index");
        }
    }
}

