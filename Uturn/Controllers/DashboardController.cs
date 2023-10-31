using System;
using Microsoft.AspNetCore.Mvc;

namespace Uturn.Controllers
{
	public class DashboardController : Controller
	{
        // GET: Dashboard
        public IActionResult Index()
        {
            return View();
        }
    }
}

