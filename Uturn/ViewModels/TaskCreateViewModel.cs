using System;
using System.ComponentModel.DataAnnotations;
using Uturn.Models;

namespace Uturn.ViewModels
{
	public class TaskCreateViewModel
	{
        public string key { get; set; } = "";

        public List<Location> Locations { get; set; }

        [Required]
        public string selectedDropoffLocationKey { get; set; } = "";

        [Required]
        public string type { get; set; } = "";

        [Required]
        public string typeOfGoods { get; set; } = "";

        [Required]
        public string pickupLocation { get; set; } = "";
    }
}

